package iljin.framework.ebid.etc.notice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iljin.framework.ebid.etc.notice.entity.TCoBoardCustCode;
import iljin.framework.ebid.etc.notice.entity.TCoBoardCustID;

@Repository
public interface TCoBoardCustRepository extends JpaRepository<TCoBoardCustCode, TCoBoardCustID> {
	
	List<TCoBoardCustCode> findBybNo(Integer bno);
}
