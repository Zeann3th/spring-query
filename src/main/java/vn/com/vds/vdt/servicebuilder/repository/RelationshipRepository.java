package vn.com.vds.vdt.servicebuilder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.vds.vdt.servicebuilder.entity.Relationship;

@Repository
public interface RelationshipRepository extends JpaRepository<Relationship, Long> {
    @Modifying
    @Query("DELETE FROM Relationship r WHERE r.fromEntityId = :entityId OR r.toEntityId = :entityId")
    void deleteByEntityId(Long entityId);

}
