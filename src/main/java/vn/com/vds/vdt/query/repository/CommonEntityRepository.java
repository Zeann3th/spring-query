package vn.com.vds.vdt.query.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.vds.vdt.query.entity.InstanceEntity;

@Repository
public interface CommonEntityRepository extends JpaRepository<InstanceEntity, Long> {
}
