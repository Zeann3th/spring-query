package vn.com.vds.vdt.query.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.vds.vdt.query.entity.AttributeEntity;
import vn.com.vds.vdt.query.entity.InstanceEntity;
import vn.com.vds.vdt.query.entity.ValueEntity;

import java.util.Optional;

@Repository
public interface ValueRepository extends JpaRepository<ValueEntity, Long> {
    @Modifying
    @Query("DELETE FROM ValueEntity v WHERE v.attributeEntity = :attributeEntity")
    void deleteByAttributeEntity(@Param("attributeEntity") AttributeEntity attributeEntity);

    Optional<ValueEntity> findByInstanceEntityAndAttributeEntity(InstanceEntity instanceEntity, AttributeEntity attributeEntity);

    void deleteByInstanceEntity(InstanceEntity instance);
}
