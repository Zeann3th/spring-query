package vn.com.vds.vdt.query.entity_2;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @ManyToOne
    @JoinColumn(name = "relationship_type_id", nullable = false)
    private RelationshipType relationshipType;

    @ManyToOne
    @JoinColumn(name = "from_entity_id", nullable = false)
    private CommonEntity fromEntity;

    @ManyToOne
    @JoinColumn(name = "to_entity_id", nullable = false)
    private CommonEntity toEntity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "json")
    private Map<String, String> metadata;
}
