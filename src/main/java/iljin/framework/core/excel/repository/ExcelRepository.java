package iljin.framework.core.excel.repository;

import iljin.framework.core.excel.entity.FileEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class ExcelRepository {

    @PersistenceContext
    private EntityManager em;

    public List<FileEntity> findAll() {
        return em.createQuery("select f from FileEntity f", FileEntity.class)
                .setMaxResults(10)
                .getResultList();
    }


}
