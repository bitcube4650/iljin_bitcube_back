package iljin.framework.ebid.custom.repository;

import iljin.framework.ebid.custom.entity.TCoCustMaster;
import iljin.framework.ebid.custom.entity.TCoInterrelated;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TCoCustMasterRepository extends JpaRepository<TCoCustMaster, String> {
}