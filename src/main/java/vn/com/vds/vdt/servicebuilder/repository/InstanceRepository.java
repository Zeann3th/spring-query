package vn.com.vds.vdt.servicebuilder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.vds.vdt.servicebuilder.entity.InstanceEntity;

@Repository
public interface InstanceRepository extends JpaRepository<InstanceEntity, Long> {
}
