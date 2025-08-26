package vn.com.vds.vdt.servicebuilder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.vds.vdt.servicebuilder.entity.AttributeValue;
import vn.com.vds.vdt.servicebuilder.entity.AttributeDefinition;
import vn.com.vds.vdt.servicebuilder.entity.Instance;

import java.util.Optional;

@Repository
public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    Optional<AttributeValue> findByEntityAndAttributeDefinition(Instance entity,
            AttributeDefinition attributeDefinition);
}
