package vn.com.vds.vdt.query.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"values", "attributes"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "entities")
public class DynamicEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "entity_name", nullable = false, unique = true)
    private String entityName;

    @OneToMany(mappedBy = "dynamicEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AttributeEntity> attributes = new ArrayList<>();

    @OneToMany(mappedBy = "dynamicEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ValueEntity> values = new ArrayList<>();
}