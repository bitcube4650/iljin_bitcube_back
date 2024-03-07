package iljin.framework.ebid.etc.notice.service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.notice.dto.FaqDto;
import iljin.framework.ebid.etc.notice.entity.TCoBoardCustCode;
import iljin.framework.ebid.etc.notice.entity.TCoBoardNotice;
import iljin.framework.ebid.etc.notice.entity.TFaq;
import iljin.framework.ebid.etc.util.PagaUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FaqService {
	
	@Autowired
    private TCoUserRepository tCoUserRepository;
	
	@PersistenceContext
    private EntityManager entityManager;

	//faq 목록 조회
	@Transactional
	public Page faqList(Map<String, Object> params) {

		try {
			StringBuilder sbCount = new StringBuilder(" select count(1) "
												    + " from t_faq "
												    + " where 1 = 1 ");
			
			StringBuilder sbList = new StringBuilder(" select faq_id , "
												   		  + " faq_type , "
												   		  + " CASE "
													   		  + " WHEN faq_type = 1 THEN '가입관련' "
													   		  + " WHEN faq_type = 2 THEN '입찰관련' "
													   		  + " WHEN faq_type = 3 THEN '인증서관련' "
													   		  + " ELSE '기타' "
												   		  + " END AS faq_type_description , "
												   		  + " title , "
												   		  + " answer , "
												   		  + " create_user , "
												   		  + " DATE_FORMAT( create_date , '%Y-%m-%d %H:%i') as createDate "
												   + " from t_faq "
												   + " where 1 = 1 ");
			
			StringBuilder sbWhere = new StringBuilder();
			String adminYn = (String) params.get("admin");
			
			if (!StringUtils.isEmpty(params.get("title"))) {
				sbWhere.append(" and title like concat('%',:title,'%') ");
			}
			if (!StringUtils.isEmpty(params.get("faqType"))) {
				sbWhere.append(" and faq_type = :faqType ");
			}

			sbList.append(sbWhere);
			sbList.append(" order by create_date desc ");
			
			Query queryList = entityManager.createNativeQuery(sbList.toString());
			sbCount.append(sbWhere);
			Query queryTotal = entityManager.createNativeQuery(sbCount.toString());
			
			if (!StringUtils.isEmpty(params.get("title"))) {
				queryList.setParameter("title", params.get("title"));
	            queryTotal.setParameter("title", params.get("title"));
			}
			if (!StringUtils.isEmpty(params.get("faqType"))) {
				queryList.setParameter("faqType", params.get("faqType"));
	            queryTotal.setParameter("faqType", params.get("faqType"));
			}
			
			Pageable pageable = PagaUtils.pageable(params);
			
			if(adminYn.equals("Y")) {//관리자 faq 화면은 페이징 처리하여 불러오고 일반 유저 faq 화면은 한번 모든 faq 불러옴
				queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();
			}
		
			List list = new JpaResultMapper().list(queryList, FaqDto.class);

			BigInteger count = (BigInteger) queryTotal.getSingleResult();
			return new PageImpl(list, pageable, count.intValue());
		}catch(Exception e) {
			e.printStackTrace(); 
			return new PageImpl(new ArrayList<>());
		}
		
	}

	//faq 저장
	@Transactional
	public ResultBody save(Map<String, Object> params) {

		ResultBody resultBody = new ResultBody();
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TCoUser user = tCoUserRepository.findById(principal.getUsername()).get();//로그인한 유저정보

		
		String title = (String) params.get("title");
		String answer = (String) params.get("answer");
		String faqType = (String) params.get("faqType");
		String updateInsert = (String) params.get("updateInsert");
		String userId = user.getUserId();
		LocalDateTime currentDate = LocalDateTime.now();
		
		//faq update
		if(updateInsert.equals("update")) {//수정하는 경우
			System.out.println("수정들어옴");
			int faqId = (int) params.get("faqId");
			TFaq faq = entityManager.find(TFaq.class, faqId);
			
			if(faq != null) {
				//파라미터 set
				faq.setTitle(title);
				faq.setAnswer(answer);
				faq.setFaqType(faqType);
				faq.setCreateUser(userId);
				faq.setCreateDate(currentDate);
			}
			
		}else {//등록하는 경우
			
			TFaq newFaq = new TFaq();
			
			newFaq.setTitle(title);
			newFaq.setAnswer(answer);
			newFaq.setFaqType(faqType);
			newFaq.setCreateUser(userId);
			newFaq.setCreateDate(currentDate);
			
			entityManager.persist(newFaq);
		}

		return resultBody;
	}

}
