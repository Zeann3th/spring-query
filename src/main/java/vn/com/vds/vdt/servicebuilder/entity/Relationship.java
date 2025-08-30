package vn.com.vds.vdt.servicebuilder.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "relationships")
public class Relationship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relationship_id")
    @EqualsAndHashCode.Include
    private Long relationshipId;

    @Column(name = "relationship_type_id", nullable = false)
    private Long relationshipTypeId;

    @Column(name = "from_entity_id", nullable = false)
    private Long fromEntityId;

    @Column(name = "to_entity_id", nullable = false)
    private Long toEntityId;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private Map<String, String> metadata = new HashMap<>();
}
