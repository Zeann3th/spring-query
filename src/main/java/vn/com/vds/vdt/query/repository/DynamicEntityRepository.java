package vn.com.vds.vdt.query.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.vds.vdt.query.entity.DynamicEntity;

import java.util.Optional;

public interface DynamicEntityRepository extends JpaRepository<DynamicEntity, Long> {
    Optional<DynamicEntity> findByEntityName(String name);
}
