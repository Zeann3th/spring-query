package vn.com.vds.vdt.query.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.vds.vdt.query.entity.AttributeEntity;
import vn.com.vds.vdt.query.entity.DynamicEntity;

import java.util.Optional;

@Repository
public interface AttributeRepository extends JpaRepository<AttributeEntity, Long> {
    Optional<AttributeEntity> findByAttributeNameAndDynamicEntity(String attributeName, DynamicEntity dynamicEntity);
}
