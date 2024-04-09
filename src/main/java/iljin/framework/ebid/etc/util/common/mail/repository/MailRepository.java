package iljin.framework.ebid.etc.util.common.mail.repository;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;;

@Repository
public class MailRepository {
    @PersistenceContext
    private EntityManager em;

    public void saveMailInfo(String title, String content, String userEmail) {
        String saveMailInfo =  "INSERT INTO t_email (title, conts, send_flag, create_date, receives) VALUES " +
                "(:title, :content, '0', CURRENT_TIMESTAMP, :userEmail)";

        em.createNativeQuery(saveMailInfo)
                .setParameter("title", title)
                .setParameter("content", content)
                .setParameter("userEmail", userEmail)
                .executeUpdate();
    }


}
