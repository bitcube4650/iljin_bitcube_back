package iljin.framework.ebid.etc.util.common.schedule.repository;

import iljin.framework.ebid.etc.util.common.schedule.entity.MailEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.time.LocalDate;
import java.util.List;

@Repository
public class ScheduleRepository {
    @PersistenceContext
    private EntityManager em;

    public List<String> selectByDeleteByIngTag() {
        String selectByDeleteByIngTag = "SELECT BI_NO " +
                                        "FROM t_Bi_info_mat " +
                                        "WHERE EST_CLOSE_DATE < NOW() " +
                                        "AND ING_TAG = 'A0'";

        return em.createNativeQuery(selectByDeleteByIngTag)
                .getResultList();
    }

    public void deleteByIngTag() {
        /*
    	String deleteByIngTag = "DELETE FROM t_Bi_info_mat " +
                                "WHERE EST_CLOSE_DATE < NOW() " +
                                "AND ING_TAG = 'A0'";

         */
        String deleteByIngTag = "UPDATE T_BI_INFO_MAT SET ING_TAG = 'D', " +
                                "UPDATE_DATE = NOW(), " +
                                "UPDATE_USER = :updateUser " +
                                "WHERE EST_CLOSE_DATE < NOW() " +
                                "AND ING_TAG = 'A0'";

        em.createNativeQuery(deleteByIngTag)
                .setParameter("updateUser", "system")
                .executeUpdate();
    }

    public List<String> selectBiNoForLast30Days() {
        String selectBiNoForLast30Days = "SELECT BI_NO " +
                                           "FROM T_BI_INFO_MAT " +
                                           "WHERE BID_OPEN_DATE < DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                                           "AND ING_TAG IN ('A1','A2','A3')";
                return em.createNativeQuery(selectBiNoForLast30Days)
                        .getResultList();
    }



    public void updateIngTagForLast30Days() {
        String updateIngTagForLast30Days = "UPDATE T_BI_INFO_MAT SET ING_TAG = 'A7', " +
                                            "UPDATE_DATE = NOW(), " +
                                            "UPDATE_USER = :updateUser " +
                                            "WHERE BID_OPEN_DATE < DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                                            "AND ING_TAG IN ('A1', 'A2', 'A3')";
        em.createNativeQuery(updateIngTagForLast30Days)
                .setParameter("updateUser", "system")
                .executeUpdate();
    }


    public MailEntity findOneMailInfo(String id) {
        return em.find(MailEntity.class, id);
    }

    public List<MailEntity> findAllMailInfo() {
    	LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        return em.createQuery("select m from MailEntity m where m.sendFlag = '0' and m.createDate >= :sevenDaysAgo", MailEntity.class)
                .setParameter("sevenDaysAgo", sevenDaysAgo)
                .getResultList();

    }


}
