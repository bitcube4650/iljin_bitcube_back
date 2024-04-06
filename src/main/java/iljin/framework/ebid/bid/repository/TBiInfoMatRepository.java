package iljin.framework.ebid.bid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iljin.framework.ebid.bid.entity.TBiInfoMat;
import iljin.framework.ebid.bid.entity.TBiInfoMatCustTemp;
import iljin.framework.ebid.bid.entity.TBiInfoMatCustTempID;

@Repository
public interface TBiInfoMatRepository extends JpaRepository<TBiInfoMat, String> {

}
