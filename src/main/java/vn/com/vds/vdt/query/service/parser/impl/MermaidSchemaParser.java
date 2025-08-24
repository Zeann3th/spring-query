package vn.com.vds.vdt.query.service.parser.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.com.vds.vdt.query.common.enums.DataType;
import vn.com.vds.vdt.query.controller.dto.parser.ParseSchemaRequest;
import vn.com.vds.vdt.query.controller.dto.query.AttributeInput;
import vn.com.vds.vdt.query.controller.dto.query.CreateEntityInput;
import vn.com.vds.vdt.query.service.parser.SchemaParser;
import vn.com.vds.vdt.query.service.query.QueryService;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class MermaidSchemaParser implements SchemaParser {

    private final QueryService queryService;

    private static final Pattern ENTITY_PATTERN = Pattern.compile(
            "\\s*(\\w+)\\s*\\{([^}]*)}",
            Pattern.MULTILINE | Pattern.DOTALL
    );

    private static final Pattern FIELD_PATTERN = Pattern.compile(
            "\\s*(\\w+)\\s+(\\w+)\\s*(?:(PK|FK|UK)\\s*)?(?:\"([^\"]*)\")?"
    );

    private static final Pattern RELATION_PATTERN = Pattern.compile(
            "\\s*(\\w+)\\s+(\\|\\|--o\\{|}o--\\|\\||o\\{--}o|\\|\\|--\\|\\|)\\s*(\\w+)\\s*:\\s*\"?([^\"\\n]*)\"?"
    );

    @Override
    public String getType() {
        return "mermaid-erd";
    }

    @Override
    public void parse(ParseSchemaRequest request) {
        try {
            ParseSchemaRequest.Metadata metadata = request.getMetadata();
            String erdContent = metadata.getContent();

            if (erdContent == null || erdContent.trim().isEmpty()) {
                throw new IllegalArgumentException("ERD content is required");
            }

            erdContent = erdContent.replaceFirst("(?i)\\s*erDiagram\\s*", "");

            Map<String, List<AttributeInput>> entities = parseEntities(erdContent);

            for (Map.Entry<String, List<AttributeInput>> entry : entities.entrySet()) {
                CreateEntityInput entityInput = CreateEntityInput.builder()
                        .entityName(entry.getKey())
                        .attributes(entry.getValue())
                        .build();

                queryService.createEntity(entityInput);
                log.info("Created entity: {}", entry.getKey());
            }

            List<Relation> relations = parseRelations(erdContent);
            for (Relation relation : relations) {
                log.info("Found relation: {} {} {}",
                        relation.getFromEntity(),
                        relation.getType(),
                        relation.getToEntity());
            }

        } catch (Exception e) {
            log.error("Error parsing Mermaid ERD", e);
            throw new RuntimeException("Failed to parse Mermaid ERD: " + e.getMessage(), e);
        }
    }

    private Map<String, List<AttributeInput>> parseEntities(String content) {
        Map<String, List<AttributeInput>> entities = new LinkedHashMap<>();

        Matcher entityMatcher = ENTITY_PATTERN.matcher(content);

        while (entityMatcher.find()) {
            String entityName = entityMatcher.group(1).trim();
            String fieldsContent = entityMatcher.group(2).trim();

            List<AttributeInput> attributes = parseFields(fieldsContent);
            entities.put(entityName, attributes);
        }

        return entities;
    }

    private List<AttributeInput> parseFields(String fieldsContent) {
        List<AttributeInput> attributes = new ArrayList<>();

        String[] lines = fieldsContent.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            Matcher fieldMatcher = FIELD_PATTERN.matcher(line);
            if (fieldMatcher.find()) {
                String fieldType = fieldMatcher.group(1);
                String fieldName = fieldMatcher.group(2);
                String constraint = fieldMatcher.group(3);

                String mappedType = mapMermaidType(fieldType);

                AttributeInput attribute = AttributeInput.builder()
                        .name(fieldName)
                        .type(mappedType)
                        .build();

                attributes.add(attribute);

                log.debug("Parsed field: {} {} ({})", fieldName, mappedType, constraint);
            }
        }

        return attributes;
    }

    private String mapMermaidType(String mermaidType) {
        return switch (mermaidType.toLowerCase()) {
            case "int", "integer" -> DataType.INTEGER.getValue();
            case "decimal", "float", "double" -> DataType.DOUBLE.getValue();
            case "boolean", "bool" -> DataType.BOOLEAN.getValue();
            case "long", "bigint" -> DataType.LONG.getValue();
            default -> DataType.STRING.getValue();
        };
    }

    private List<Relation> parseRelations(String content) {
        List<Relation> relations = new ArrayList<>();

        Matcher relationMatcher = RELATION_PATTERN.matcher(content);

        while (relationMatcher.find()) {
            String fromEntity = relationMatcher.group(1);
            String relationType = relationMatcher.group(2);
            String toEntity = relationMatcher.group(3);
            String description = relationMatcher.group(4);

            Relation relation = Relation.builder()
                    .fromEntity(fromEntity)
                    .toEntity(toEntity)
                    .type(mapRelationType(relationType))
                    .description(description)
                    .build();

            relations.add(relation);
        }

        return relations;
    }

    private String mapRelationType(String mermaidRelation) {
        return switch (mermaidRelation) {
            case "||--o{" -> "one-to-many";
            case "}o--||" -> "many-to-one";
            case "||--||" -> "one-to-one";
            case "}o--o{" -> "many-to-many";
            default -> "unknown";
        };
    }

    @lombok.Data
    @lombok.Builder
    private static class Relation {
        private String fromEntity;
        private String toEntity;
        private String type;
        private String description;
    }
}