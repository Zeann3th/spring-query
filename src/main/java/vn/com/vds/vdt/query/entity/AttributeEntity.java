package vn.com.vds.vdt.query.entity;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"values", "dynamicEntity"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "attributes")
public class AttributeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "attribute_id")
    private Long attributeId;

    @Column(name = "attribute_name", nullable = false)
    private String attributeName;

    @Column(name = "attribute_type", nullable = false)
    private String attributeType;

    @ManyToOne
    @JoinColumn(name = "entity_id", nullable = false)
    private DynamicEntity dynamicEntity;

    @OneToMany(mappedBy = "attributeEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ValueEntity> values = new ArrayList<>();
}