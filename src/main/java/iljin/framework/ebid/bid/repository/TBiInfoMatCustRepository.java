package iljin.framework.ebid.bid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iljin.framework.ebid.bid.entity.TBiInfoMatCustID;
import iljin.framework.ebid.bid.entity.TBiInfoMatCust;

@Repository
public interface TBiInfoMatCustRepository extends JpaRepository<TBiInfoMatCust, TBiInfoMatCustID> {

}
