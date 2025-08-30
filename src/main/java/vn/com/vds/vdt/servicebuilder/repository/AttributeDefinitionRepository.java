package vn.com.vds.vdt.servicebuilder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.vds.vdt.servicebuilder.entity.AttributeDefinition;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, Long> {
    Optional<AttributeDefinition> findByNameAndEntityTypeId(String name, Long entityTypeId);

    List<AttributeDefinition> findByEntityTypeId(Long entityTypeId);
}
