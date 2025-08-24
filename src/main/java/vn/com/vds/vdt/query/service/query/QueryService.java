package vn.com.vds.vdt.query.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.vds.vdt.query.controller.dto.query.*;
import vn.com.vds.vdt.query.entity.*;
import vn.com.vds.vdt.query.repository.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("all")
public class QueryService {
    private final DynamicEntityRepository dynamicEntityRepo;
    private final AttributeRepository attributeRepo;
    private final InstanceRepository instanceRepo;
    private final ValueRepository valueRepo;

    @Transactional(readOnly = true)
    public QueryResult<DynamicEntityDto> getEntities(FilterInput filter) {
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

        if (filter.getOffset() != null || filter.getLimit() != null) {
            int offset = filter.getOffset() != null ? filter.getOffset() : 0;
            int limit = filter.getLimit() != null ? filter.getLimit() : entities.size();

            entities = entities.stream()
                    .skip(offset)
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        List<DynamicEntityDto> entityDtos = entities.stream()
                .map(this::convertToEntityDto)
                .collect(Collectors.toList());

        boolean hasNext = filter.getLimit() != null &&
                filter.getOffset() != null &&
                (filter.getOffset() + filter.getLimit()) < totalCount;

        return QueryResult.<DynamicEntityDto>builder()
                .items(entityDtos)
                .totalCount(totalCount)
                .hasNext(hasNext)
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<DynamicEntityDto> getEntityById(Long entityId) {
        return dynamicEntityRepo.findById(entityId)
                .map(this::convertToEntityDto);
    }

    @Transactional(readOnly = true)
    public Optional<DynamicEntityDto> getEntityByName(String entityName) {
        return dynamicEntityRepo.findByEntityName(entityName)
                .map(this::convertToEntityDto);
    }

    @Transactional
    public DynamicEntityDto createEntity(CreateEntityInput input) {
        try {
            DynamicEntity entity = DynamicEntity.builder()
                    .entityName(input.getEntityName())
                    .build();

            entity = dynamicEntityRepo.save(entity);

            if (input.getAttributes() != null) {
                for (AttributeInput attrDef : input.getAttributes()) {
                    AttributeEntity attribute = AttributeEntity.builder()
                            .attributeName(attrDef.getName())
                            .attributeType(attrDef.getType())
                            .dynamicEntity(entity)
                            .build();

                    attribute = attributeRepo.save(attribute);
                    entity.getAttributes().add(attribute);
                }
            }

            return convertToEntityDto(entity);
        } catch (Exception e) {
            log.error("Error creating entity", e);
            throw e;
        }
    }

    @Transactional
    public DynamicEntityDto updateEntity(UpdateEntityInput input) {
        DynamicEntity entity = dynamicEntityRepo.findById(input.getEntityId())
                .orElseThrow(() -> new RuntimeException("Entity not found with id: " + input.getEntityId()));

        if (input.getEntityName() != null) {
            entity.setEntityName(input.getEntityName());
        }

        entity = dynamicEntityRepo.save(entity);
        return convertToEntityDto(entity);
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
    public DynamicEntityDto addAttributeToEntity(Long entityId, AttributeInput attributeDefinition) {
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
                .build();

        attribute = attributeRepo.save(attribute);
        entity.getAttributes().add(attribute);

        return convertToEntityDto(entity);
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

        return convertToEntityDto(entity);
    }

    @Transactional(readOnly = true)
    public QueryResult<EntityInstanceDto> getEntityInstances(String entityName, FilterInput filter) {
        DynamicEntity entity = dynamicEntityRepo.findByEntityName(entityName)
                .orElseThrow(() -> new RuntimeException("Entity not found: " + entityName));

        List<InstanceEntity> instances = instanceRepo.findByDynamicEntity(entity);

        if (filter != null && filter.getAttributeFilters() != null && !filter.getAttributeFilters().isEmpty()) {
            instances = filterInstancesByAttributes(instances, filter.getAttributeFilters());
        }

        int totalCount = instances.size();

        if (filter != null && (filter.getOffset() != null || filter.getLimit() != null)) {
            int offset = filter.getOffset() != null ? filter.getOffset() : 0;
            int limit = filter.getLimit() != null ? filter.getLimit() : instances.size();

            instances = instances.stream()
                    .skip(offset)
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        List<EntityInstanceDto> instanceDtos = instances.stream()
                .map(instance -> convertToInstanceDto(instance, entityName))
                .collect(Collectors.toList());

        boolean hasNext = false;
        if (filter != null && filter.getLimit() != null && filter.getOffset() != null) {
            hasNext = (filter.getOffset() + filter.getLimit()) < totalCount;
        }

        return QueryResult.<EntityInstanceDto>builder()
                .items(instanceDtos)
                .totalCount(totalCount)
                .hasNext(hasNext)
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<EntityInstanceDto> getEntityInstance(String entityName, Long instanceId) {
        DynamicEntity entity = dynamicEntityRepo.findByEntityName(entityName)
                .orElseThrow(() -> new RuntimeException("Entity not found: " + entityName));

        return instanceRepo.findByInstanceIdAndDynamicEntity(instanceId, entity)
                .map(instance -> convertToInstanceDto(instance, entityName));
    }

    @Transactional
    public EntityInstanceDto createEntityInstance(InstanceInput input) {
        DynamicEntity entity = dynamicEntityRepo.findByEntityName(input.getEntityName())
                .orElseThrow(() -> new RuntimeException("Entity not found: " + input.getEntityName()));

        // Create new instance
        InstanceEntity instance = InstanceEntity.builder()
                .dynamicEntity(entity)
                .build();
        instance = instanceRepo.save(instance);

        // Create values for the instance
        if (input.getValues() != null) {
            createInstanceValues(instance, input.getValues());
        }

        return convertToInstanceDto(instance, input.getEntityName());
    }

    @Transactional
    public EntityInstanceDto updateEntityInstance(InstanceInput input) {
        DynamicEntity entity = dynamicEntityRepo.findByEntityName(input.getEntityName())
                .orElseThrow(() -> new RuntimeException("Entity not found: " + input.getEntityName()));

        InstanceEntity instance = instanceRepo.findByInstanceIdAndDynamicEntity(input.getInstanceId(), entity)
                .orElseThrow(() -> new RuntimeException("Instance not found: " + input.getInstanceId()));

        // Delete existing values
        valueRepo.deleteByInstanceEntity(instance);

        // Force flush to ensure deletes are committed before inserts
        valueRepo.flush();

        if (input.getValues() != null) {
            createInstanceValues(instance, input.getValues());
        }

        return convertToInstanceDto(instance, input.getEntityName());
    }

    @Transactional
    public Boolean deleteEntityInstance(String entityName, Long instanceId) {
        DynamicEntity entity = dynamicEntityRepo.findByEntityName(entityName)
                .orElseThrow(() -> new RuntimeException("Entity not found: " + entityName));

        Optional<InstanceEntity> instanceOpt = instanceRepo.findByInstanceIdAndDynamicEntity(instanceId, entity);
        if (instanceOpt.isEmpty()) {
            return false;
        }

        instanceRepo.delete(instanceOpt.get());
        return true;
    }

    private void createInstanceValues(InstanceEntity instance, Map<String, Object> values) {
        DynamicEntity entity = instance.getDynamicEntity();

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String attributeName = entry.getKey();
            Object value = entry.getValue();

            AttributeEntity attribute = entity.getAttributes().stream()
                    .filter(attr -> attr.getAttributeName().equals(attributeName))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Attribute not found: " + attributeName));

            // Check if value already exists to prevent duplicates
            Optional<ValueEntity> existingValue = valueRepo.findByInstanceEntityAndAttributeEntity(instance, attribute);

            if (existingValue.isPresent()) {
                // Update existing value instead of creating new one
                ValueEntity valueEntity = existingValue.get();
                valueEntity.setValue(value != null ? value.toString() : null);
                valueRepo.save(valueEntity);
            } else {
                // Create new value
                ValueEntity valueEntity = ValueEntity.builder()
                        .instanceEntity(instance)
                        .attributeEntity(attribute)
                        .value(value != null ? value.toString() : null)
                        .build();

                valueRepo.save(valueEntity);
            }
        }
    }

    private List<InstanceEntity> filterInstancesByAttributes(List<InstanceEntity> instances,
                                                             Map<String, Object> attributeFilters) {
        return instances.stream()
                .filter(instance -> matchesAttributeFilters(instance, attributeFilters))
                .collect(Collectors.toList());
    }

    private boolean matchesAttributeFilters(InstanceEntity instance, Map<String, Object> attributeFilters) {
        for (Map.Entry<String, Object> filter : attributeFilters.entrySet()) {
            String attributeName = filter.getKey();
            Object filterValue = filter.getValue();

            Optional<ValueEntity> valueOpt = instance.getValues().stream()
                    .filter(val -> val.getAttributeEntity().getAttributeName().equals(attributeName))
                    .findFirst();

            if (valueOpt.isEmpty()) {
                return false;
            }

            if (!matchesFilterCriteria(valueOpt.get().getValue(), filterValue)) {
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
                    case "eq":
                    case "equals":
                        if (!Objects.equals(actualValue, operationValue.toString())) {
                            return false;
                        }
                        break;
                    case "gt":
                        if (!compareNumeric(actualValue, operationValue, ">")) {
                            return false;
                        }
                        break;
                    case "lt":
                        if (!compareNumeric(actualValue, operationValue, "<")) {
                            return false;
                        }
                        break;
                    case "in":
                        if (operationValue instanceof List) {
                            List<?> values = (List<?>) operationValue;
                            if (!values.stream().anyMatch(v -> Objects.equals(actualValue, v.toString()))) {
                                return false;
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
            return actualValue.compareTo(compareValue.toString()) > 0;
        }
    }

    private DynamicEntityDto convertToEntityDto(DynamicEntity entity) {
        List<AttributeDto> attributeDtos = entity.getAttributes().stream()
                .map(attr -> AttributeDto.builder()
                        .attributeId(attr.getAttributeId())
                        .attributeName(attr.getAttributeName())
                        .attributeType(attr.getAttributeType())
                        .build())
                .collect(Collectors.toList());

        return DynamicEntityDto.builder()
                .entityId(entity.getEntityId())
                .entityName(entity.getEntityName())
                .attributes(attributeDtos)
                .build();
    }

    private EntityInstanceDto convertToInstanceDto(InstanceEntity instance, String entityName) {
        Map<String, Object> values = instance.getValues().stream()
                .collect(Collectors.toMap(
                        val -> val.getAttributeEntity().getAttributeName(),
                        val -> convertValueBasedOnType(val.getValue(), val.getAttributeEntity().getAttributeType()),
                        (existing, replacement) -> replacement
                ));

        return EntityInstanceDto.builder()
                .instanceId(instance.getInstanceId())
                .entityName(entityName)
                .values(values)
                .createdAt(instance.getCreatedAt() != null ?
                        instance.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .updatedAt(instance.getUpdatedAt() != null ?
                        instance.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
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