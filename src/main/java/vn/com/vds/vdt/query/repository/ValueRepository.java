package vn.com.vds.vdt.query.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.vds.vdt.query.entity.ValueEntity;

public interface ValueRepository extends JpaRepository<ValueEntity, Long> {
}
