package vn.com.vds.vdt.servicebuilder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.vds.vdt.servicebuilder.entity.AttributeValue;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    Optional<AttributeValue> findByEntityIdAndAttributeDefinitionId(Long entityId, Long attributeDefinitionId);

    @Modifying
    @Query("DELETE FROM AttributeValue av WHERE av.entityId = :entityId")
    void deleteByEntityId(Long entityId);

    List<AttributeValue> findByEntityId(Long entityId);
}
