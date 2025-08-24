package vn.com.vds.vdt.query.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.vds.vdt.query.entity.EntityType;
import vn.com.vds.vdt.query.entity.RelationshipType;

import java.util.Optional;

@Repository
public interface RelationshipTypeRepository extends JpaRepository<RelationshipType, Long> {
    Optional<RelationshipType> findByFromEntityTypeAndToEntityType(EntityType fromEntity, EntityType toEntity);
}
