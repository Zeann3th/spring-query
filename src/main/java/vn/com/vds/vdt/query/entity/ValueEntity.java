package vn.com.vds.vdt.query.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "attribute_values",
        uniqueConstraints = @UniqueConstraint(columnNames = {"entity_id", "attribute_id"})
)
public class ValueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "value_id")
    private Long valueId;

    @Column(name = "value")
    private String value;

    @ManyToOne
    @JoinColumn(name = "entity_id", nullable = false)
    private DynamicEntity dynamicEntity;

    @ManyToOne
    @JoinColumn(name = "attribute_id", nullable = false)
    private AttributeEntity attributeEntity;
}