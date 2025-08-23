package vn.com.vds.vdt.query.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.vds.vdt.query.controller.dto.query.*;
import vn.com.vds.vdt.query.entity.AttributeEntity;
import vn.com.vds.vdt.query.entity.DynamicEntity;
import vn.com.vds.vdt.query.entity.ValueEntity;
import vn.com.vds.vdt.query.repository.AttributeRepository;
import vn.com.vds.vdt.query.repository.DynamicEntityRepository;
import vn.com.vds.vdt.query.repository.ValueRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("all")
public class QueryService {
    private final DynamicEntityRepository dynamicEntityRepo;
    private final AttributeRepository attributeRepo;
    private final ValueRepository valueRepo;

    @Transactional(readOnly = true)
    public EntityQueryResult getEntities(EntityFilter filter) {
        List<DynamicEntity> entities;
        Integer totalCount = 0;

        if (filter.getEntityName() != null) {
            Optional<DynamicEntity> entity = dynamicEntityRepo.findByEntityName(filter.getEntityName());
            if (entity.isPresent()) {
                entities = Collections.singletonList(entity.get());
                totalCount = 1;
            } else {
                entities = Collections.emptyList();
            }
        } else {
            entities = dynamicEntityRepo.findAll();
            totalCount = entities.size();
        }

        if (filter.getAttributeFilters() != null && !filter.getAttributeFilters().isEmpty()) {
            entities = filterEntitiesByAttributes(entities, filter.getAttributeFilters());
            totalCount = entities.size();
        }

        if (filter.getOffset() != null || filter.getLimit() != null) {
            int offset = filter.getOffset() != null ? filter.getOffset() : 0;
            int limit = filter.getLimit() != null ? filter.getLimit() : entities.size();

            entities = entities.stream()
                    .skip(offset)
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        List<DynamicEntityDto> entityDtos = entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        boolean hasNext = filter.getLimit() != null &&
                filter.getOffset() != null &&
                (filter.getOffset() + filter.getLimit()) < totalCount;

        return EntityQueryResult.builder()
                .entities(entityDtos)
                .totalCount(totalCount)
                .hasNext(hasNext)
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<DynamicEntityDto> getEntityById(Long entityId) {
        return dynamicEntityRepo.findById(entityId)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Optional<DynamicEntityDto> getEntityByName(String entityName) {
        return dynamicEntityRepo.findByEntityName(entityName)
                .map(this::convertToDto);
    }

    @Transactional
    public DynamicEntityDto createEntity(CreateEntityInput input) {
        DynamicEntity entity = DynamicEntity.builder()
                .entityName(input.getEntityName())
                .attributes(new ArrayList<>())
                .values(new ArrayList<>())
                .build();

        entity = dynamicEntityRepo.save(entity);

        if (input.getAttributes() != null) {
            for (AttributeDefinition attrDef : input.getAttributes()) {
                AttributeEntity attribute = AttributeEntity.builder()
                        .attributeName(attrDef.getName())
                        .attributeType(attrDef.getType())
                        .dynamicEntity(entity)
                        .values(new ArrayList<>())
                        .build();

                attribute = attributeRepo.save(attribute);
                entity.getAttributes().add(attribute);
            }
        }

        if (input.getValues() != null) {
            createOrUpdateValues(entity, input.getValues());
        }

        return convertToDto(entity);
    }

    @Transactional
    public DynamicEntityDto updateEntity(UpdateEntityInput input) {
        DynamicEntity entity = dynamicEntityRepo.findById(input.getEntityId())
                .orElseThrow(() -> new RuntimeException("Entity not found with id: " + input.getEntityId()));

        if (input.getEntityName() != null) {
            entity.setEntityName(input.getEntityName());
        }

        if (input.getValues() != null) {
            createOrUpdateValues(entity, input.getValues());
        }

        entity = dynamicEntityRepo.save(entity);
        return convertToDto(entity);
    }

    @Transactional
    public Boolean deleteEntity(Long entityId) {
        if (!dynamicEntityRepo.existsById(entityId)) {
            return false;
        }

        dynamicEntityRepo.deleteById(entityId);
        return true;
    }

    @Transactional
    public DynamicEntityDto addAttributeToEntity(Long entityId, AttributeDefinition attributeDefinition) {
        DynamicEntity entity = dynamicEntityRepo.findById(entityId)
                .orElseThrow(() -> new RuntimeException("Entity not found with id: " + entityId));

        boolean attributeExists = entity.getAttributes().stream()
                .anyMatch(attr -> attr.getAttributeName().equals(attributeDefinition.getName()));

        if (attributeExists) {
            throw new RuntimeException("Attribute already exists: " + attributeDefinition.getName());
        }

        AttributeEntity attribute = AttributeEntity.builder()
                .attributeName(attributeDefinition.getName())
                .attributeType(attributeDefinition.getType())
                .dynamicEntity(entity)
                .values(new ArrayList<>())
                .build();

        attribute = attributeRepo.save(attribute);
        entity.getAttributes().add(attribute);

        return convertToDto(entity);
    }

    @Transactional
    public DynamicEntityDto removeAttributeFromEntity(Long entityId, String attributeName) {
        DynamicEntity entity = dynamicEntityRepo.findById(entityId)
                .orElseThrow(() -> new RuntimeException("Entity not found with id: " + entityId));

        AttributeEntity attributeToRemove = entity.getAttributes().stream()
                .filter(attr -> attr.getAttributeName().equals(attributeName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Attribute not found: " + attributeName));

        valueRepo.deleteByAttributeEntity(attributeToRemove);

        entity.getAttributes().remove(attributeToRemove);
        attributeRepo.delete(attributeToRemove);

        return convertToDto(entity);
    }

    private void createOrUpdateValues(DynamicEntity entity, Map<String, Object> values) {
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String attributeName = entry.getKey();
            Object value = entry.getValue();

            AttributeEntity attribute = entity.getAttributes().stream()
                    .filter(attr -> attr.getAttributeName().equals(attributeName))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Attribute not found: " + attributeName));

            ValueEntity valueEntity = entity.getValues().stream()
                    .filter(val -> val.getAttributeEntity().getAttributeId().equals(attribute.getAttributeId()))
                    .findFirst()
                    .orElse(null);

            if (valueEntity == null) {
                valueEntity = ValueEntity.builder()
                        .dynamicEntity(entity)
                        .attributeEntity(attribute)
                        .build();
                entity.getValues().add(valueEntity);
                attribute.getValues().add(valueEntity);
            }

            valueEntity.setValue(value != null ? value.toString() : null);
            valueRepo.save(valueEntity);
        }
    }

    private List<DynamicEntity> filterEntitiesByAttributes(List<DynamicEntity> entities,
                                                           Map<String, Object> attributeFilters) {
        return entities.stream()
                .filter(entity -> matchesAttributeFilters(entity, attributeFilters))
                .collect(Collectors.toList());
    }

    private boolean matchesAttributeFilters(DynamicEntity entity, Map<String, Object> attributeFilters) {
        for (Map.Entry<String, Object> filter : attributeFilters.entrySet()) {
            String attributeName = filter.getKey();
            Object filterValue = filter.getValue();

            boolean hasMatchingValue = entity.getValues().stream()
                    .anyMatch(valueEntity ->
                            valueEntity.getAttributeEntity().getAttributeName().equals(attributeName) &&
                                    matchesFilterCriteria(valueEntity.getValue(), filterValue));

            if (!hasMatchingValue) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean matchesFilterCriteria(String actualValue, Object filterValue) {
        if (actualValue == null) {
            return filterValue == null;
        }

        if (filterValue instanceof String || filterValue instanceof Number || filterValue instanceof Boolean) {
            return Objects.equals(actualValue, filterValue.toString());
        }

        if (filterValue instanceof Map) {
            Map<String, Object> filterMap = (Map<String, Object>) filterValue;

            for (Map.Entry<String, Object> entry : filterMap.entrySet()) {
                String operation = entry.getKey();
                Object operationValue = entry.getValue();

                switch (operation.toLowerCase()) {
                    case "contains":
                        if (!actualValue.toLowerCase().contains(operationValue.toString().toLowerCase())) {
                            return false;
                        }
                        break;

                    case "startswith":
                        if (!actualValue.toLowerCase().startsWith(operationValue.toString().toLowerCase())) {
                            return false;
                        }
                        break;

                    case "endswith":
                        if (!actualValue.toLowerCase().endsWith(operationValue.toString().toLowerCase())) {
                            return false;
                        }
                        break;

                    case "eq":
                    case "equals":
                        if (!Objects.equals(actualValue, operationValue.toString())) {
                            return false;
                        }
                        break;

                    case "ne":
                    case "notequals":
                        if (Objects.equals(actualValue, operationValue.toString())) {
                            return false;
                        }
                        break;

                    case "gt":
                    case "greaterthan":
                        if (!compareNumeric(actualValue, operationValue, ">")) {
                            return false;
                        }
                        break;

                    case "gte":
                    case "greaterthanorequal":
                        if (!compareNumeric(actualValue, operationValue, ">=")) {
                            return false;
                        }
                        break;

                    case "lt":
                    case "lessthan":
                        if (!compareNumeric(actualValue, operationValue, "<")) {
                            return false;
                        }
                        break;

                    case "lte":
                    case "lessthanorequal":
                        if (!compareNumeric(actualValue, operationValue, "<=")) {
                            return false;
                        }
                        break;

                    case "in":
                        if (operationValue instanceof List) {
                            List<?> values = (List<?>) operationValue;
                            if (!values.contains(actualValue)) {
                                return false;
                            }
                        }
                        break;

                    case "between":
                        if (operationValue instanceof List) {
                            List<?> range = (List<?>) operationValue;
                            if (range.size() == 2) {
                                if (!compareNumeric(actualValue, range.get(0), ">=") ||
                                        !compareNumeric(actualValue, range.get(1), "<=")) {
                                    return false;
                                }
                            }
                        }
                        break;

                    default:
                        if (!Objects.equals(actualValue, operationValue.toString())) {
                            return false;
                        }
                        break;
                }
            }
            return true;
        }

        return Objects.equals(actualValue, filterValue.toString());
    }

    private boolean compareNumeric(String actualValue, Object compareValue, String operator) {
        try {
            double actual = Double.parseDouble(actualValue);
            double compare = Double.parseDouble(compareValue.toString());

            switch (operator) {
                case ">":
                    return actual > compare;
                case ">=":
                    return actual >= compare;
                case "<":
                    return actual < compare;
                case "<=":
                    return actual <= compare;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            // If not numeric, fall back to string comparison
            return actualValue.compareTo(compareValue.toString()) > 0;
        }
    }

    private DynamicEntityDto convertToDto(DynamicEntity entity) {
        List<AttributeDto> attributeDtos = entity.getAttributes().stream()
                .map(attr -> AttributeDto.builder()
                        .attributeId(attr.getAttributeId())
                        .attributeName(attr.getAttributeName())
                        .attributeType(attr.getAttributeType())
                        .build())
                .collect(Collectors.toList());

        Map<String, Object> values = entity.getValues().stream()
                .collect(Collectors.toMap(
                        val -> val.getAttributeEntity().getAttributeName(),
                        val -> convertValueBasedOnType(val.getValue(), val.getAttributeEntity().getAttributeType()),
                        (existing, replacement) -> replacement
                ));

        return DynamicEntityDto.builder()
                .entityId(entity.getEntityId())
                .entityName(entity.getEntityName())
                .attributes(attributeDtos)
                .values(values)
                .build();
    }

    private Object convertValueBasedOnType(String value, String type) {
        if (value == null) return null;

        try {
            switch (type.toLowerCase()) {
                case "integer":
                case "int":
                    return Integer.parseInt(value);
                case "long":
                    return Long.parseLong(value);
                case "double":
                    return Double.parseDouble(value);
                case "float":
                    return Float.parseFloat(value);
                case "boolean":
                    return Boolean.parseBoolean(value);
                case "string":
                case "text":
                default:
                    return value;
            }
        } catch (NumberFormatException e) {
            return value;
        }
    }
}
