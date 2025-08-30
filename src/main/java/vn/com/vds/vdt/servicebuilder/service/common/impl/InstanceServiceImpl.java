package vn.com.vds.vdt.servicebuilder.service.common.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.com.vds.vdt.servicebuilder.common.constants.ErrorCodes;
import vn.com.vds.vdt.servicebuilder.common.enums.DataType;
import vn.com.vds.vdt.servicebuilder.entity.AttributeDefinition;
import vn.com.vds.vdt.servicebuilder.entity.AttributeValue;
import vn.com.vds.vdt.servicebuilder.entity.EntityType;
import vn.com.vds.vdt.servicebuilder.entity.Instance;
import vn.com.vds.vdt.servicebuilder.exception.CommandExceptionBuilder;
import vn.com.vds.vdt.servicebuilder.repository.*;
import vn.com.vds.vdt.servicebuilder.service.common.InstanceService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("all")
public class InstanceServiceImpl implements InstanceService {

    private final EntityTypeRepository entityTypeRepository;
    private final InstanceRepository instanceRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final RelationshipRepository relationshipRepository;

    @Override
    public Page<Instance> getInstances(String entityTypeName, Pageable pageable) {
        Page<Instance> page = instanceRepository.findByEntityTypeName(entityTypeName, pageable);
        page.forEach(this::populateAttributes);
        return page;
    }

    @Override
    public Instance getInstanceById(Long entityId) {
        Instance instance = instanceRepository.findById(entityId)
                .orElseThrow(() -> CommandExceptionBuilder
                        .exception(ErrorCodes.QS00004, "Instance not found: " + entityId)
                );
        populateAttributes(instance);
        return instance;
    }

    @Transactional
    @Override
    public Long createInstance(String entityTypeName, Map<String, Object> attributes) {
        EntityType entityType = entityTypeRepository.findByName(entityTypeName.toLowerCase())
                .orElseThrow(() -> CommandExceptionBuilder
                        .exception(ErrorCodes.QS00004,"Unknown entity type: " + entityTypeName)
                );

        Instance instance = Instance.builder()
                .entityTypeId(entityType.getEntityTypeId())
                .build();
        instance = instanceRepository.save(instance);

        upsertAttributeValues(instance, attributes);
        return instance.getEntityId();
    }

    @Transactional
    @Override
    public void updateInstance(Long entityId, Map<String, Object> attributes) {
        Instance instance = instanceRepository.findById(entityId)
                .orElseThrow(() -> CommandExceptionBuilder
                        .exception(ErrorCodes.QS00004,"Instance not found: " + entityId)
                );
        upsertAttributeValues(instance, attributes);
    }

    @Transactional
    @Override
    public void deleteInstance(Long entityId) {
        Instance instance = instanceRepository.findById(entityId)
                .orElseThrow(() -> CommandExceptionBuilder
                        .exception(ErrorCodes.QS00004,"Instance not found: " + entityId)
                );

        attributeValueRepository.deleteByEntityId(entityId);
        relationshipRepository.deleteByEntityId(entityId);
        instanceRepository.delete(instance);
    }

    private void upsertAttributeValues(Instance instance, Map<String, Object> attributes) {
        if (attributes == null)
            return;
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String attrName = entry.getKey().toLowerCase();
            Object value = entry.getValue();

            AttributeDefinition def = attributeDefinitionRepository
                    .findByNameAndEntityTypeId(attrName, instance.getEntityTypeId())
                    .orElseThrow(() -> CommandExceptionBuilder
                            .exception(ErrorCodes.QS00004,"Unknown attribute '" + attrName
                            + "' for entity type id '" + instance.getEntityTypeId() + "'"));

            AttributeValue attrVal = attributeValueRepository
                    .findByEntityIdAndAttributeDefinitionId(
                            instance.getEntityId(),
                            def.getAttributeDefinitionId())
                    .orElseGet(() -> AttributeValue.builder()
                            .entityId(instance.getEntityId())
                            .attributeDefinitionId(def.getAttributeDefinitionId())
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

    private void populateAttributes(Instance instance) {
        Map<Long, AttributeDefinition> attrDefMap = attributeDefinitionRepository
                .findByEntityTypeId(instance.getEntityTypeId())
                .stream()
                .collect(Collectors.toMap(AttributeDefinition::getAttributeDefinitionId, ad -> ad));

        Map<String, Object> attributes = attributeValueRepository
                .findByEntityId(instance.getEntityId())
                .stream()
                .collect(Collectors.toMap(
                        av -> {
                            AttributeDefinition def = attrDefMap.get(av.getAttributeDefinitionId());
                            return def != null ? def.getName() : "unknown";
                        },
                        av -> {
                            AttributeDefinition def = attrDefMap.get(av.getAttributeDefinitionId());
                            if (def == null) return null;

                            DataType type = DataType.valueOf(def.getDataType());
                            return switch (type) {
                                case STRING, UUID -> av.getStringValue();
                                case INTEGER -> av.getNumberValue() != null ? av.getNumberValue().intValue() : null;
                                case LONG -> av.getNumberValue() != null ? av.getNumberValue().longValue() : null;
                                case DOUBLE -> av.getNumberValue();
                                case BOOLEAN -> av.getBooleanValue();
                                case DATE, DATETIME -> av.getDateValue();
                                default -> av.getStringValue();
                            };
                        }
                ));

        instance.setAttributes(attributes);
    }

}
