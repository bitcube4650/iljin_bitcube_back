package iljin.framework.ebid.custom.repository;

import iljin.framework.ebid.custom.entity.TCoItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TCoItemRepository extends JpaRepository<TCoItem, String> {
    Page<TCoItem> findAllByItemGrpCd(String itemGrpCd, Pageable pageable);
}