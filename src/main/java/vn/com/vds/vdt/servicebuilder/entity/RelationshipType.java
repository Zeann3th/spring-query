package vn.com.vds.vdt.servicebuilder.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "relationship_types")
public class RelationshipType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relationship_type_id")
    @EqualsAndHashCode.Include
    private Long relationshipTypeId;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "from_entity_type_id", nullable = false)
    private EntityType fromEntityType;

    @ManyToOne
    @JoinColumn(name = "to_entity_type_id", nullable = false)
    private EntityType toEntityType;

    @Column(name = "cardinality", nullable = false)
    private String cardinality;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;
}
