package vn.com.vds.vdt.query.service.parser.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.com.vds.vdt.query.common.ErrorCodes;
import vn.com.vds.vdt.query.common.enums.DataType;
import vn.com.vds.vdt.query.common.mappers.DataTypeMapper;
import vn.com.vds.vdt.query.dto.ParseSchemaRequest;
import vn.com.vds.vdt.query.entity.AttributeEntity;
import vn.com.vds.vdt.query.entity.DynamicEntity;
import vn.com.vds.vdt.query.exception.CommandException;
import vn.com.vds.vdt.query.repository.AttributeRepository;
import vn.com.vds.vdt.query.repository.DynamicEntityRepository;
import vn.com.vds.vdt.query.service.parser.SchemaParser;

import java.sql.*;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("all")
public class JdbcSchemaParser implements SchemaParser {

    private final DynamicEntityRepository dynamicEntityRepo;
    private final AttributeRepository attributeRepo;

    @Override
    public String getType() {
        return "jdbc";
    }

    @Transactional
    @Override
    public void parse(ParseSchemaRequest request) {
        try (Connection conn = DriverManager.getConnection(
                request.getMetadata().getUrl(),
                request.getMetadata().getUsername(),
                request.getMetadata().getPassword())) {

            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String table = tables.getString("TABLE_NAME").toLowerCase();
                    log.info("Parsing table '{}'", table);

                    DynamicEntity entity = dynamicEntityRepo
                            .findByEntityName(table)
                            .orElseGet(() -> dynamicEntityRepo.save(DynamicEntity.builder()
                                    .entityName(table)
                                    .build()));

                    parseColumns(meta, entity, table);
                }
            }
        } catch (SQLException e) {
            throw new CommandException(ErrorCodes.QS30001,
                    String.format("Failed to read schema from '%s': %s",
                            request.getMetadata().getUrl(), e.getMessage()));
        }
    }

    private void parseColumns(DatabaseMetaData meta, DynamicEntity entity, String table) throws SQLException {
        try (ResultSet columns = meta.getColumns(null, null, table, "%")) {
            while (columns.next()) {
                String column = columns.getString("COLUMN_NAME").toLowerCase();
                String typeName = columns.getString("TYPE_NAME");

                String mappedType = mapType(typeName);

                AttributeEntity attr = attributeRepo.findByDynamicEntityAndAttributeName(entity, column)
                        .orElseGet(() -> attributeRepo.save(AttributeEntity.builder()
                                .attributeName(column)
                                .attributeType(mappedType)
                                .dynamicEntity(entity)
                                .build()));

                entity.getAttributes().add(attr);
            }
        }
    }

    private String mapType(String typeName) {
        try {
            return DataTypeMapper.map(typeName);
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown SQL type '{}', defaulting to STRING", typeName);
            return DataType.STRING.getValue();
        }
    }
}