package vn.com.vds.vdt.query.service.parser.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.com.vds.vdt.query.common.Utils;
import vn.com.vds.vdt.query.common.enums.DataType;
import vn.com.vds.vdt.query.common.enums.RelationshipCardinality;
import vn.com.vds.vdt.query.controller.dto.parser.ParseSchemaRequest;
import vn.com.vds.vdt.query.controller.dto.query.AttributeInput;
import vn.com.vds.vdt.query.entity.AttributeDefinition;
import vn.com.vds.vdt.query.entity.EntityType;
import vn.com.vds.vdt.query.entity.RelationshipType;
import vn.com.vds.vdt.query.repository.AttributeDefinitionRepository;
import vn.com.vds.vdt.query.repository.EntityTypeRepository;
import vn.com.vds.vdt.query.repository.RelationshipTypeRepository;
import vn.com.vds.vdt.query.service.parser.SchemaParser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class MermaidSchemaParser implements SchemaParser {

    private final EntityTypeRepository entityTypeRepo;
    private final AttributeDefinitionRepository attributeDefinitionRepo;
    private final RelationshipTypeRepository relationshipTypeRepo;

    private static final Pattern ENTITY_PATTERN = Pattern.compile("\\s*(\\w+)\\s*\\{([^}]*)}", Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern FIELD_PATTERN = Pattern.compile("\\s*(\\w+)\\s+(\\w+)\\s*(?:(PK|FK|UK)\\s*)?(?:\"([^\"]*)\")?");
    private static final Pattern RELATION_PATTERN = Pattern.compile("\\s*(\\w+)\\s+(\\|\\|--o\\{|}o--\\|\\||o\\{--}o|\\|\\|--\\|\\|)\\s*(\\w+)\\s*:?\\s*\"?([^\"\\n]*)\"?");

    @Override
    public String getType() {
        return "mermaid-erd";
    }

    @Transactional
    @Override
    public void parse(ParseSchemaRequest request) {
        try {
            String erdContent = Optional.ofNullable(request.getMetadata().getContent())
                    .orElseThrow(() -> new IllegalArgumentException("ERD content is required"));

            erdContent = erdContent.replaceFirst("(?i)\\s*erDiagram\\s*", "");

            Map<String, List<AttributeInput>> entities = parseEntities(erdContent);
            Map<String, EntityType> entityMap = new HashMap<>();

            for (var entry : entities.entrySet()) {
                String entityName = entry.getKey().toLowerCase();
                EntityType entity = entityTypeRepo.findByName(entityName)
                        .orElseGet(() -> entityTypeRepo.save(EntityType.builder()
                                .name(entityName)
                                .displayName(Utils.capitalize(entityName))
                                .isActive(true)
                                .schemaVersion(1L)
                                .build()
                        ));
                entityMap.put(entityName, entity);

                for (AttributeInput attr : entry.getValue()) {
                    AttributeDefinition def = attributeDefinitionRepo.findByNameAndEntityType(attr.getName(), entity)
                            .orElseGet(() -> AttributeDefinition.builder()
                                    .entityType(entity)
                                    .name(attr.getName())
                                    .displayName(Utils.capitalize(attr.getName()))
                                    .dataType(attr.getType())
                                    .isRequired(false)
                                    .validationRules(new HashMap<>())
                                    .build()
                            );
                    def.setDataType(attr.getType());
                    attributeDefinitionRepo.save(def);
                }
                log.info("Created/updated entity: {}", entityName);
            }

            List<Relation> relations = parseRelations(erdContent);
            for (Relation rel : relations) {
                EntityType fromEntity = entityMap.get(rel.getFromEntity().toLowerCase());
                EntityType toEntity = entityMap.get(rel.getToEntity().toLowerCase());

                if (fromEntity == null || toEntity == null) {
                    log.warn("Skipping relation {} -> {} because entity not found", rel.getFromEntity(), rel.getToEntity());
                    continue;
                }

                createOrUpdateRelationship(fromEntity, toEntity, rel, false);

                createOrUpdateRelationship(toEntity, fromEntity, rel, true);

                log.info("Created/updated bidirectional relation: {} {} {}",
                        rel.getFromEntity(), rel.getType(), rel.getToEntity());
            }

        } catch (Exception e) {
            log.error("Error parsing Mermaid ERD", e);
            throw new RuntimeException("Failed to parse Mermaid ERD: " + e.getMessage(), e);
        }
    }

    private void createOrUpdateRelationship(EntityType fromEntity, EntityType toEntity,
                                            Relation relation, boolean isReverse) {
        RelationshipCardinality relationType = mapRelationType(relation.getType());
        String cardinality = isReverse ?
                RelationshipCardinality.reverse(relationType).getValue() :
                relationType.getValue();

        String relationshipName = Utils.generateRelationshipName(fromEntity, toEntity, relation.getDescription(), isReverse);

        RelationshipType relationship = relationshipTypeRepo
                .findByFromEntityTypeAndToEntityType(fromEntity, toEntity)
                .orElseGet(() -> RelationshipType.builder()
                        .name(relationshipName)
                        .fromEntityType(fromEntity)
                        .toEntityType(toEntity)
                        .cardinality(cardinality)
                        .isRequired(!isReverse)
                        .build()
                );

        relationship.setCardinality(cardinality);
        relationship.setName(relationshipName);
        relationshipTypeRepo.save(relationship);

        log.debug("Created/updated {} relationship: {} -> {} ({})",
                isReverse ? "reverse" : "forward",
                fromEntity.getName(),
                toEntity.getName(),
                cardinality);
    }

    private Map<String, List<AttributeInput>> parseEntities(String content) {
        Map<String, List<AttributeInput>> entities = new LinkedHashMap<>();
        Matcher matcher = ENTITY_PATTERN.matcher(content);
        while (matcher.find()) {
            String entityName = matcher.group(1).trim();
            String fields = matcher.group(2).trim();
            entities.put(entityName, parseFields(fields));
        }
        return entities;
    }

    private List<AttributeInput> parseFields(String fieldsContent) {
        List<AttributeInput> attributes = new ArrayList<>();
        for (String line : fieldsContent.split("\\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;
            Matcher fieldMatcher = FIELD_PATTERN.matcher(line);
            if (fieldMatcher.find()) {
                String type = mapMermaidType(fieldMatcher.group(1));
                String name = fieldMatcher.group(2);
                attributes.add(AttributeInput.builder()
                        .name(name)
                        .type(type)
                        .build());
            }
        }
        return attributes;
    }

    private List<Relation> parseRelations(String content) {
        List<Relation> relations = new ArrayList<>();
        Matcher matcher = RELATION_PATTERN.matcher(content);
        while (matcher.find()) {
            relations.add(Relation.builder()
                    .fromEntity(matcher.group(1))
                    .toEntity(matcher.group(3))
                    .type(matcher.group(2))
                    .description(Optional.ofNullable(matcher.group(4)).orElse(""))
                    .build());
        }
        return relations;
    }

    private String mapMermaidType(String type) {
        return switch (type.toLowerCase()) {
            case "int", "integer" -> DataType.INTEGER.getValue();
            case "decimal", "float", "double" -> DataType.DOUBLE.getValue();
            case "boolean", "bool" -> DataType.BOOLEAN.getValue();
            case "long", "bigint" -> DataType.LONG.getValue();
            default -> DataType.STRING.getValue();
        };
    }

    private RelationshipCardinality mapRelationType(String mermaidRelation) {
        return switch (mermaidRelation) {
            case "||--o{" -> RelationshipCardinality.ONE_TO_MANY;
            case "}o--||" -> RelationshipCardinality.MANY_TO_ONE;
            case "||--||" -> RelationshipCardinality.ONE_TO_ONE;
            case "}o--o{", "o{--}o" -> RelationshipCardinality.MANY_TO_MANY;
            default -> RelationshipCardinality.UNKNOWN;
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