package iljin.framework.ijeas.sm.code;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeHeaderRepository extends JpaRepository<CodeHeader, CodeHeaderKey> {
	
}
