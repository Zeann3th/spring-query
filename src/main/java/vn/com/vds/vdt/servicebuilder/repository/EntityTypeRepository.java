package vn.com.vds.vdt.servicebuilder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.vds.vdt.servicebuilder.entity.EntityType;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntityTypeRepository extends JpaRepository<EntityType, Long> {
    Optional<EntityType> findByName(String name);
}
