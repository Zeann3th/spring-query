package vn.com.vds.vdt.servicebuilder.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
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
@Table(name = "attribute_values")
public class AttributeValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "value_id")
    @EqualsAndHashCode.Include
    private Long valueId;

    @ManyToOne
    @JoinColumn(name = "entity_id", nullable = false)
    private InstanceEntity entity;

    @ManyToOne
    @JoinColumn(name = "attribute_definition_id", nullable = false)
    private AttributeDefinition attributeDefinition;

    @Column(name = "string_value")
    private String stringValue;

    @Column(name = "number_value")
    private Double numberValue;

    @Column(name = "date_value")
    private LocalDateTime dateValue;

    @Column(name = "boolean_value")
    private Boolean booleanValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json_value")
    private Map<String, String> jsonValue = new HashMap<>();
}
