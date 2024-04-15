package iljin.framework.ebid.etc.util.common.schedule.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ScheduleUserInfoService {
	
	//EHR DB 접속정보
	@Value("${oracle.ehr.datasource.url}")
	private String ehrUrl;
	@Value("${oracle.ehr.datasource.username}")
	private String ehrUsername;
	@Value("${oracle.ehr.datasource.password}")
	private String ehrPassword;
	@Value("${oracle.ehr.datasource.driver-class-name}")
	private String ehrDrivername;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	/**
	 * 미사용 사용자 업데이트
	 * @throws Exception
	 */
	@Transactional
	public void updateUserUseYn() throws Exception{
		
		List<String[]> list = this.ehrDbData("Select EMP_ID from iljinehr.EBID_EMP_V where stat_yn = 'N'");
		
		if(list.size() != 0) {
			StringBuilder sbQuery = new StringBuilder(
				  "UPDATE T_CO_USER "
				+ "SET	USE_YN = 'N' "
				+ ",	UPDATE_USER='SYSTEM' "
				+ ",	UPDATE_DATE = SYSDATE() "
				+ "WHERE USER_ID IN ( :userId ) "
				+ "AND USE_YN = 'Y' "
			);
			
			Query query = entityManager.createNativeQuery(sbQuery.toString());
			query.setParameter("userId", list);
			query.executeUpdate();
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	public List<String[]> ehrDbData(String strQuery) {
		Map<String, String> persistenceMap = new HashMap<String, String>();
		persistenceMap.put("javax.persistence.jdbc.url", ehrUrl);
		persistenceMap.put("javax.persistence.jdbc.user", ehrUsername);
		persistenceMap.put("javax.persistence.jdbc.password", ehrPassword);
		persistenceMap.put("javax.persistence.jdbc.driver", ehrDrivername);
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("oracle", persistenceMap);
		EntityManager em = emf.createEntityManager();
		
		List<String[]> list = null;
		try {
			list = em.createNativeQuery(strQuery).getResultList();
		} catch (Exception e) {
			list = new ArrayList<String[]>();
			log.error("ehr db error : {}", e);
		} finally {
			em.close();
			emf.close();
		}
		
		return list;
	}
}
