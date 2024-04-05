package iljin.framework.ebid.etc.util.common.message;

import com.database.mariadb.TaxMonitor;
import iljin.framework.ebid.etc.util.Constances;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class MessageService {

    @Value("${oracle.datasource.url}")
    private String url;
    @Value("${oracle.datasource.username}")
    private String username;
    @Value("${oracle.datasource.password}")
    private String password;
    @Value("${oracle.datasource.driver-class-name}")
    private String drivername;

    public void send(String sendName, String rPhone,  String recvName, String msg) {
        send(sendName, rPhone, recvName, msg, null);
    }

    /**
     * SMS 메시지 전송
     *
     * @param sendName 보낸사람(비밀번호/아이디 찾기 등 시스템에서 보낼 경우 일진그룹)
     * @param rPhone 수신자 번호
     * @param recvName 수신자명
     * @param msg 메시지 내용
     * @param biNo 입찰과 관련된 문자일 경우 입찰번호 그외엔 NULL
     */
    public void send(String sendName, String rPhone,  String recvName, String msg, String biNo) {
        Map<String, String> persistenceMap = new HashMap();
        persistenceMap.put("javax.persistence.jdbc.url", url);
        persistenceMap.put("javax.persistence.jdbc.user", username);
        persistenceMap.put("javax.persistence.jdbc.password", password);
        persistenceMap.put("javax.persistence.jdbc.driver", drivername);
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("oracle", persistenceMap);
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            Query query = em.createNativeQuery("INSERT INTO smsdata (send_name, r_phone, recv_name, msg, bi_no) VALUES (:sendName, :rPhone, :recvName, :msg, :biNo)");
            query.setParameter("sendName", sendName);
            query.setParameter("rPhone", rPhone);
            query.setParameter("recvName", recvName);
            query.setParameter("msg", msg);
            query.setParameter("biNo", biNo);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        tx.commit();
        em.close();
        emf.close();
    }
}
