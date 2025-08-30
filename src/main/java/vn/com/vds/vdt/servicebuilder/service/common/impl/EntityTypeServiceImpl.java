package vn.com.vds.vdt.servicebuilder.service.common.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import vn.com.vds.vdt.servicebuilder.controller.dto.entityType.CreateEntityTypeRequest;
import vn.com.vds.vdt.servicebuilder.entity.AttributeDefinition;
import vn.com.vds.vdt.servicebuilder.entity.EntityType;
import vn.com.vds.vdt.servicebuilder.repository.AttributeDefinitionRepository;
import vn.com.vds.vdt.servicebuilder.repository.EntityTypeRepository;
import vn.com.vds.vdt.servicebuilder.service.common.EntityTypeService;

import java.util.HashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("all")
public class EntityTypeServiceImpl implements EntityTypeService {

    private final EntityTypeRepository entityTypeRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;

    @Transactional
    @Override
    public EntityType createOrUpdate(CreateEntityTypeRequest request) {
        String name = request.getName().toLowerCase();
        Optional<EntityType> optionalEntityType = entityTypeRepository.findByName(name);
        EntityType entityType = null;

        if (optionalEntityType.isPresent()) {
            entityType = optionalEntityType.get();
            entityType.setSchemaVersion(entityType.getSchemaVersion() + 1L);
        } else {
            entityType = EntityType.builder()
                    .name(name)
                    .displayName(request.getDisplayName())
                    .isActive(Boolean.TRUE.equals(request.getIsActive()))
                    .schemaVersion(1L)
                    .build();
        }

        entityType.setDisplayName(request.getDisplayName());
        entityType.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        final EntityType savedEntityType = entityTypeRepository.save(entityType);

        if (request.getAttributes() != null) {
            for (var attr : request.getAttributes()) {
                String attrName = attr.getName().toLowerCase();
                AttributeDefinition def = attributeDefinitionRepository
                        .findByNameAndEntityTypeId(attrName, savedEntityType.getEntityTypeId())
                        .orElseGet(() -> AttributeDefinition.builder()
                                .entityTypeId(savedEntityType.getEntityTypeId())
                                .name(attrName)
                                .displayName(attr.getDisplayName())
                                .dataType(attr.getDataType())
                                .isRequired(Boolean.TRUE.equals(attr.getIsRequired()))
                                .defaultValue(attr.getDefaultValue())
                                .validationRules(
                                        attr.getValidationRules() != null ? attr.getValidationRules() : new HashMap<>())
                                .build());

                def.setDisplayName(attr.getDisplayName());
                def.setDataType(attr.getDataType());
                def.setIsRequired(Boolean.TRUE.equals(attr.getIsRequired()));
                def.setDefaultValue(attr.getDefaultValue());
                def.setValidationRules(attr.getValidationRules() != null ? attr.getValidationRules() : new HashMap<>());
                attributeDefinitionRepository.save(def);
            }
        }

        return savedEntityType;
    }
}
