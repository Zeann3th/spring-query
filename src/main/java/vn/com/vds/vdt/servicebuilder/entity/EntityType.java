package vn.com.vds.vdt.servicebuilder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "entity_types")
public class EntityType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "entity_type_id")
    private Long entityTypeId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "schema_version")
    private Long schemaVersion;

    @OneToMany(mappedBy = "entityType", fetch = FetchType.LAZY)
    private List<AttributeDefinition> attributeDefinitions;

    @OneToMany(mappedBy = "fromEntityType", fetch = FetchType.LAZY)
    private List<RelationshipType> relationshipTypes;
}
