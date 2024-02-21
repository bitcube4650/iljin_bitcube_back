package iljin.framework.ebid.custom.repository;

import iljin.framework.ebid.custom.entity.TCoItem;
import iljin.framework.ebid.custom.entity.TCoItemGrp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TCoItemRepository extends JpaRepository<TCoItem, String> {

    Page findAllByItemNameOrderByItemCodeDesc(String itemName, Pageable pageable);

    Page findAll(Specification<TCoItem> tCoItemSpecification, Pageable pageable);
}