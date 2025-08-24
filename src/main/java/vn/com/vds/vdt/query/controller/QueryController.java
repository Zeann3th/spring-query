package vn.com.vds.vdt.query.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import vn.com.vds.vdt.query.controller.dto.query.*;
import vn.com.vds.vdt.query.service.query.QueryService;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@SuppressWarnings("all")
public class QueryController {
    private final QueryService queryService;

    @QueryMapping
    public QueryResult<DynamicEntityDto> getEntities(@Argument FilterInput filter) {
        if (filter == null) {
            filter = FilterInput.builder().build();
        }
        return queryService.getEntities(filter);
    }

    @QueryMapping
    public Optional<DynamicEntityDto> getEntityById(@Argument Long entityId) {
        return queryService.getEntityById(entityId);
    }

    @QueryMapping
    public Optional<DynamicEntityDto> getEntityByName(@Argument String entityName) {
        return queryService.getEntityByName(entityName);
    }

    @QueryMapping
    public QueryResult<EntityInstanceDto> getEntityInstances(@Argument String entityName,
                                                             @Argument FilterInput filter) {
        return queryService.getEntityInstances(entityName, filter);
    }

    @QueryMapping
    public Optional<EntityInstanceDto> getEntityInstance(@Argument String entityName,
                                                         @Argument Long instanceId) {
        return queryService.getEntityInstance(entityName, instanceId);
    }

    @MutationMapping
    public DynamicEntityDto createEntity(@Argument CreateEntityInput input) {
        return queryService.createEntity(input);
    }

    @MutationMapping
    public DynamicEntityDto updateEntity(@Argument UpdateEntityInput input) {
        return queryService.updateEntity(input);
    }

    @MutationMapping
    public Boolean deleteEntity(@Argument Long entityId) {
        return queryService.deleteEntity(entityId);
    }

    @MutationMapping
    public DynamicEntityDto addAttributeToEntity(@Argument Long entityId,
                                                 @Argument AttributeInput attribute) {
        return queryService.addAttributeToEntity(entityId, attribute);
    }

    @MutationMapping
    public DynamicEntityDto removeAttributeFromEntity(@Argument Long entityId,
                                                      @Argument String attributeName) {
        return queryService.removeAttributeFromEntity(entityId, attributeName);
    }

    @MutationMapping
    public EntityInstanceDto createEntityInstance(@Argument InstanceInput input) {
        return queryService.createEntityInstance(input);
    }

    @MutationMapping
    public EntityInstanceDto updateEntityInstance(@Argument InstanceInput input) {
        return queryService.updateEntityInstance(input);
    }

    @MutationMapping
    public Boolean deleteEntityInstance(@Argument String entityName, @Argument Long instanceId) {
        return queryService.deleteEntityInstance(entityName, instanceId);
    }
}