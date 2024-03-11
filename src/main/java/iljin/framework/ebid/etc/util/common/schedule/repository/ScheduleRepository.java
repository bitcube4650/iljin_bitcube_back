package iljin.framework.ebid.etc.util.common.schedule.repository;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class ScheduleRepository {
    @PersistenceContext
    private EntityManager em;

    public void deleteByIngTag() {
        String deleteByIngTag = "DELETE FROM t_Bi_info_mat " +
                                "WHERE ING_TAG = 'A0'";

        em.createNativeQuery(deleteByIngTag)
                .executeUpdate();
    }
    public void updateIngTagForLast30Days() {
        String updateIngTagForLast30Days = "UPDATE T_BI_INFO_MAT SET ING_TAG = 'A7' " +
                                            "WHERE DATE(BID_OPEN_DATE) = DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
                                            "AND ING_TAG = 'A1'";
        em.createNativeQuery(updateIngTagForLast30Days)
                .executeUpdate();
    }


}
