package vn.com.vds.vdt.servicebuilder.service.cms.parser.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.com.vds.vdt.servicebuilder.common.constants.ErrorCodes;
import vn.com.vds.vdt.servicebuilder.common.enums.DataType;
import vn.com.vds.vdt.servicebuilder.common.enums.RelationshipCardinality;
import vn.com.vds.vdt.servicebuilder.controller.dto.parser.ParseSchemaRequest;
import vn.com.vds.vdt.servicebuilder.entity.AttributeDefinition;
import vn.com.vds.vdt.servicebuilder.entity.EntityType;
import vn.com.vds.vdt.servicebuilder.entity.RelationshipType;
import vn.com.vds.vdt.servicebuilder.exception.CommandExceptionBuilder;
import vn.com.vds.vdt.servicebuilder.repository.AttributeDefinitionRepository;
import vn.com.vds.vdt.servicebuilder.repository.EntityTypeRepository;
import vn.com.vds.vdt.servicebuilder.repository.RelationshipTypeRepository;
import vn.com.vds.vdt.servicebuilder.service.cms.parser.SchemaParser;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("all")
public class JdbcSchemaParser implements SchemaParser {

    private final EntityTypeRepository entityTypeRepo;
    private final AttributeDefinitionRepository attributeDefinitionRepo;
    private final RelationshipTypeRepository relationshipTypeRepo;

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
                    String tableName = tables.getString("TABLE_NAME").toLowerCase();
                    EntityType entityType = createEntityIfNotExists(tableName);
                    createColumnsIfNotExists(meta, entityType, tableName);
                    log.info("Processed entity: {}", tableName);
                }
            }

            try (ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME").toLowerCase();
                    entityTypeRepo.findByName(tableName)
                            .ifPresent(entityType -> {
                                try {
                                    createRelationshipsIfNotExists(meta, entityType, tableName);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                }
            }

        } catch (SQLException e) {
            throw CommandExceptionBuilder.exception(ErrorCodes.QS30001,
                    String.format("Failed to read schema from '%s': %s",
                            request.getMetadata().getUrl(), e.getMessage()));
        }
    }

    private EntityType createEntityIfNotExists(String tableName) {
        return entityTypeRepo.findByName(tableName)
                .orElseGet(() -> {
                    EntityType newEntity = EntityType.builder()
                            .name(tableName)
                            .displayName(ParserUtils.capitalize(tableName))
                            .isActive(true)
                            .schemaVersion(1L)
                            .build();
                    return entityTypeRepo.save(newEntity);
                });
    }

    private void createColumnsIfNotExists(DatabaseMetaData meta, EntityType entityType, String tableName) throws SQLException {
        try (ResultSet columns = meta.getColumns(null, null, tableName, "%")) {
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME").toLowerCase();

                boolean exists = attributeDefinitionRepo
                        .findByNameAndEntityTypeId(columnName, entityType.getEntityTypeId())
                        .isPresent();

                if (!exists) {
                    String sqlTypeName = columns.getString("TYPE_NAME");
                    int nullable = columns.getInt("NULLABLE");
                    String defaultValue = columns.getString("COLUMN_DEF");
                    int columnSize = columns.getInt("COLUMN_SIZE");
                    int decimalDigits = columns.getInt("DECIMAL_DIGITS");
                    String mappedDataType = mapSQLType(sqlTypeName);
                    boolean isRequired = (nullable == DatabaseMetaData.columnNoNulls);

                    AttributeDefinition attr = AttributeDefinition.builder()
                            .entityTypeId(entityType.getEntityTypeId())
                            .name(columnName)
                            .displayName(ParserUtils.capitalize(columnName))
                            .dataType(mappedDataType)
                            .isRequired(isRequired)
                            .defaultValue(defaultValue)
                            .validationRules(createValidationRules(sqlTypeName, columnSize, decimalDigits))
                            .build();

                    attributeDefinitionRepo.save(attr);
                    log.info("Created new attribute '{}' for entity '{}'", columnName, entityType.getName());
                }
            }
        }
    }

    private void createRelationshipsIfNotExists(DatabaseMetaData meta, EntityType fromEntity, String tableName) throws SQLException {
        try (ResultSet foreignKeys = meta.getImportedKeys(null, null, tableName)) {
            while (foreignKeys.next()) {
                String pkTableName = foreignKeys.getString("PKTABLE_NAME").toLowerCase();
                String fkName = foreignKeys.getString("FK_NAME");
                String fkColumn = foreignKeys.getString("FKCOLUMN_NAME");

                EntityType toEntity = createEntityIfNotExists(pkTableName);

                RelationshipCardinality forwardCardinality = isUnique(meta, tableName, fkColumn)
                        ? RelationshipCardinality.ONE_TO_ONE
                        : RelationshipCardinality.MANY_TO_ONE;

                createRelationshipIfNotExists(fromEntity, toEntity, fkName, forwardCardinality, false);
                createRelationshipIfNotExists(toEntity, fromEntity, fkName, RelationshipCardinality.reverse(forwardCardinality), true);

                log.info("Created bidirectional relationship: {} {} {}", fromEntity.getName(), forwardCardinality, toEntity.getName());
            }
        }
    }

    private void createRelationshipIfNotExists(EntityType fromEntity, EntityType toEntity,
                                               String fkName, RelationshipCardinality cardinality,
                                               boolean isReverse) {
        boolean exists = relationshipTypeRepo
                .findByFromEntityTypeIdAndToEntityTypeId(
                        fromEntity.getEntityTypeId(),
                        toEntity.getEntityTypeId())
                .isPresent();

        if (!exists) {
            String relationshipName = ParserUtils.generateRelationshipName(fromEntity, toEntity, fkName, isReverse);

            RelationshipType relationship = RelationshipType.builder()
                    .name(relationshipName)
                    .fromEntityTypeId(fromEntity.getEntityTypeId())
                    .toEntityTypeId(toEntity.getEntityTypeId())
                    .cardinality(cardinality.getValue())
                    .isRequired(!isReverse)
                    .build();

            relationshipTypeRepo.save(relationship);
            log.info("Created {} relationship: {} -> {} ({})",
                    isReverse ? "reverse" : "forward",
                    fromEntity.getName(),
                    toEntity.getName(),
                    cardinality);
        }
    }

    private boolean isUnique(DatabaseMetaData meta, String tableName, String columnName) throws SQLException {
        try (ResultSet indexes = meta.getIndexInfo(null, null, tableName, true, false)) {
            while (indexes.next()) {
                String colName = indexes.getString("COLUMN_NAME");
                if (colName != null && colName.equalsIgnoreCase(columnName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, String> createValidationRules(String sqlTypeName, int columnSize, int decimalDigits) {
        Map<String, String> validationRules = new HashMap<>();
        if (sqlTypeName != null) {
            String upperType = sqlTypeName.toUpperCase();
            if (upperType.contains("VARCHAR") || upperType.contains("CHAR")) {
                if (columnSize > 0) validationRules.put("maxLength", String.valueOf(columnSize));
            } else if (upperType.contains("DECIMAL") || upperType.contains("NUMERIC")) {
                if (columnSize > 0) validationRules.put("precision", String.valueOf(columnSize));
                if (decimalDigits > 0) validationRules.put("scale", String.valueOf(decimalDigits));
            } else if (upperType.contains("INT")) {
                validationRules.put("type", "integer");
            }
        }
        return validationRules;
    }

    private String mapSQLType(String sqlType) {
        String normalized = sqlType.toUpperCase();

        if (normalized.contains("CHAR") || normalized.contains("TEXT") || normalized.contains("CLOB")) {
            return DataType.STRING.getValue();
        } else if (normalized.contains("INT")) {
            return DataType.INTEGER.getValue();
        } else if (normalized.contains("BIGINT") || normalized.contains("LONG")) {
            return DataType.LONG.getValue();
        } else if (normalized.contains("DOUBLE") || normalized.contains("FLOAT")
                || normalized.contains("DECIMAL") || normalized.contains("NUMERIC")) {
            return DataType.DOUBLE.getValue();
        } else if (normalized.contains("BOOL")) {
            return DataType.BOOLEAN.getValue();
        } else if (normalized.contains("DATE") || normalized.contains("TIME")) {
            return normalized.contains("TIME") ? DataType.DATETIME.getValue() : DataType.DATE.getValue();
        } else if (normalized.contains("UUID")) {
            return DataType.UUID.getValue();
        }

        return DataType.STRING.getValue();
    }
}