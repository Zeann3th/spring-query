package vn.com.vds.vdt.servicebuilder.service.common.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.com.vds.vdt.servicebuilder.common.enums.DataType;
import vn.com.vds.vdt.servicebuilder.entity.*;
import vn.com.vds.vdt.servicebuilder.repository.*;
import vn.com.vds.vdt.servicebuilder.service.common.InstanceService;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@SuppressWarnings("all")
public class InstanceServiceImpl implements InstanceService {

    private final EntityTypeRepository entityTypeRepository;
    private final InstanceRepository instanceRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final AttributeValueRepository attributeValueRepository;

    @Transactional
    @Override
    public Instance getInstance(Long entityId) {
        return instanceRepository.findById(entityId)
                .orElseThrow(() -> new IllegalArgumentException("Instance not found: " + entityId));
    }

    @Transactional
    @Override
    public Instance createInstance(String entityTypeName, Map<String, Object> attributes) {
        EntityType entityType = entityTypeRepository.findByName(entityTypeName.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Unknown entity type: " + entityTypeName));

        Instance instance = Instance.builder()
                .entityType(entityType)
                .build();
        instance = instanceRepository.save(instance);

        upsertAttributeValues(instance, attributes);
        return instance;
    }

    @Transactional
    @Override
    public Instance updateInstance(Long entityId, Map<String, Object> attributes) {
        Instance instance = instanceRepository.findById(entityId)
                .orElseThrow(() -> new IllegalArgumentException("Instance not found: " + entityId));
        instance.setUpdatedAt(LocalDateTime.now());
        instanceRepository.save(instance);

        upsertAttributeValues(instance, attributes);
        return instance;
    }

    private void upsertAttributeValues(Instance instance, Map<String, Object> attributes) {
        if (attributes == null)
            return;
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String attrName = entry.getKey().toLowerCase();
            Object value = entry.getValue();

            AttributeDefinition def = attributeDefinitionRepository
                    .findByNameAndEntityType(attrName, instance.getEntityType())
                    .orElseThrow(() -> new IllegalArgumentException("Unknown attribute '" + attrName
                            + "' for entity type '" + instance.getEntityType().getName() + "'"));

            AttributeValue attrVal = attributeValueRepository
                    .findByEntityAndAttributeDefinition(instance, def)
                    .orElseGet(() -> AttributeValue.builder()
                            .entity(instance)
                            .attributeDefinition(def)
                            .build());

            applyTypedValue(attrVal, def.getDataType(), value);
            attributeValueRepository.save(attrVal);
        }
    }

    private void applyTypedValue(AttributeValue target, String dataTypeValue, Object raw) {
        // reset all typed columns
        target.setStringValue(null);
        target.setNumberValue(null);
        target.setDateValue(null);
        target.setBooleanValue(null);
        if (raw == null)
            return;

        DataType dataType = DataType.valueOf(dataTypeValue);
        switch (dataType) {
            case STRING, UUID -> target.setStringValue(String.valueOf(raw));
            case INTEGER -> target.setNumberValue(Double.valueOf(parseNumber(raw).intValue()));
            case LONG -> target.setNumberValue(Double.valueOf(parseNumber(raw).longValue()));
            case DOUBLE -> target.setNumberValue(parseNumber(raw).doubleValue());
            case BOOLEAN -> target.setBooleanValue(parseBoolean(raw));
            case DATE, DATETIME -> target.setDateValue(parseDateTime(raw));
            default -> target.setStringValue(String.valueOf(raw));
        }
    }

    private Number parseNumber(Object raw) {
        if (raw instanceof Number n)
            return n;
        return Double.valueOf(String.valueOf(raw));
    }

    private Boolean parseBoolean(Object raw) {
        if (raw instanceof Boolean b)
            return b;
        return Boolean.valueOf(String.valueOf(raw));
    }

    private LocalDateTime parseDateTime(Object raw) {
        if (raw instanceof LocalDateTime dt)
            return dt;
        return LocalDateTime.parse(String.valueOf(raw));
    }

}
