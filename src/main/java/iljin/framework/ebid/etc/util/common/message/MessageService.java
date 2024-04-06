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
            LocalDateTime currentDate = LocalDateTime.now();
            String [] datetime = currentDate.toString().split("T");
            String date = datetime[0].replaceAll("-","");
            String time = datetime[1].replaceAll(":","").substring(0, 6);
            String rPhone1 = rPhone.substring(0, 3);
            String rPhone2 = rPhone.substring(3, rPhone.length());
            String rPhone3 = rPhone2.length() == 7 ? rPhone2.substring(3, rPhone2.length()) : rPhone2.substring(4, rPhone2.length());
            rPhone2 = rPhone2.length() == 7 ? rPhone2.substring(0, 3) : rPhone2.substring(0, 4);
            Query query = null;
            // 운영에서는 오라클이기에 (스크립토로 정상 확인 
            if (drivername.startsWith("oracle")) {
                query = em.createNativeQuery("INSERT INTO smsdata (seqno, indate, intime, member, sendid, sendname, rphone1, rphone2, rphone3, recvname, sphone1, sphone2, sphone3, msg, rdate, rtime, result, kind, errcode, bi_no) " +
	                    "VALUES (smsdata_seqno.nextval, :indate, :intime, :member, :sendid, :sendname, :rphone1, :rphone2, :rphone3, :recvname, :sphone1, :sphone2, :sphone3, :msg, :rdate, :rtime, :result, :kind, :errcode, :biNo)");
            } else {
                query = em.createNativeQuery("INSERT INTO smsdata (indate, intime, member, sendid, sendname, rphone1, rphone2, rphone3, recvname, sphone1, sphone2, sphone3, msg, rdate, rtime, result, kind, errcode, bi_no) " +
	                    "VALUES (:indate, :intime, :member, :sendid, :sendname, :rphone1, :rphone2, :rphone3, :recvname, :sphone1, :sphone2, :sphone3, :msg, :rdate, :rtime, :result, :kind, :errcode, :biNo)");
            }            
            query.setParameter("indate", date);
            query.setParameter("intime", time);
            query.setParameter("member", "1");
            query.setParameter("sendid", "ebid");
            query.setParameter("sendname", sendName);
            query.setParameter("rphone1", rPhone1);
            query.setParameter("rphone2", rPhone2);
            query.setParameter("rphone3", rPhone3);
            query.setParameter("recvname", recvName);
            query.setParameter("sphone1", "02");
            query.setParameter("sphone2", "707");
            query.setParameter("sphone3", "9319");
            query.setParameter("msg", msg);
            query.setParameter("rdate", "00000000");
            query.setParameter("rtime", "000000");
            query.setParameter("result", "N");
            query.setParameter("kind", "S");
            query.setParameter("errcode", "0");
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
