package vn.com.vds.vdt.query.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"instanceEntity", "attributeEntity"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "attribute_values",
        uniqueConstraints = @UniqueConstraint(columnNames = {"instance_id", "attribute_id"}))
public class ValueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "value_id")
    private Long valueId;

    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

    @ManyToOne
    @JoinColumn(name = "instance_id", nullable = false)
    private InstanceEntity instanceEntity;

    @ManyToOne
    @JoinColumn(name = "attribute_id", nullable = false)
    private AttributeEntity attributeEntity;
}