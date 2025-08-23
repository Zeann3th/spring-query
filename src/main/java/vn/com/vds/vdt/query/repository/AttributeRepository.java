package vn.com.vds.vdt.query.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.vds.vdt.query.entity.AttributeEntity;
import vn.com.vds.vdt.query.entity.DynamicEntity;

import java.util.Optional;

public interface AttributeRepository extends JpaRepository<AttributeEntity, Long> {
    Optional<AttributeEntity> findByDynamicEntityAndAttributeName(DynamicEntity entity, String name);
}
