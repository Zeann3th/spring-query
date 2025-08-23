package vn.com.vds.vdt.query.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.vds.vdt.query.entity.DynamicEntity;

import java.util.Optional;

@Repository
public interface DynamicEntityRepository extends JpaRepository<DynamicEntity, Long> {
    Optional<DynamicEntity> findByEntityName(String entityName);
}
