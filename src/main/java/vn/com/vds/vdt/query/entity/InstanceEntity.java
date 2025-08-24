package vn.com.vds.vdt.query.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("all")
@ToString(exclude = {"values", "dynamicEntity"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "instances")
public class InstanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "instance_id")
    private Long instanceId;

    @ManyToOne
    @JoinColumn(name = "entity_id", nullable = false)
    private DynamicEntity dynamicEntity;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "instanceEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ValueEntity> values = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
