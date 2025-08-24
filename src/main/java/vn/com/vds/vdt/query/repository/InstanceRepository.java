package vn.com.vds.vdt.query.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.vds.vdt.query.entity.DynamicEntity;
import vn.com.vds.vdt.query.entity.InstanceEntity;

import java.util.List;
import java.util.Optional;

public interface InstanceRepository extends JpaRepository<InstanceEntity, Long> {
    Optional<InstanceEntity> findByInstanceIdAndDynamicEntity(Long instanceId, DynamicEntity dynamicEntity);

    List<InstanceEntity> findByDynamicEntity(DynamicEntity dynamicEntity);
}
