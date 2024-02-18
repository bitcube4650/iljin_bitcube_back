package iljin.framework.ebid.custom.repository;

import iljin.framework.ebid.custom.entity.TCoCustUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TCoCustUserRepository extends JpaRepository<TCoCustUser, String> {
    Page<TCoCustUser> findAllByCustCode(String custCode, Pageable pageable);
}