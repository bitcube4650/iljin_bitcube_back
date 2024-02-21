package iljin.framework.ebid.custom.repository;

import iljin.framework.ebid.custom.entity.TCoItemGrp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TCoItemGrpRepository extends JpaRepository<TCoItemGrp, String> {
}