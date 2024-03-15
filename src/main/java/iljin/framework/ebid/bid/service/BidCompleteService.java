package iljin.framework.ebid.bid.service;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.util.Util;
import iljin.framework.ebid.bid.dto.BidCompleteDto;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.PagaUtils;
import lombok.extern.slf4j.Slf4j;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class BidCompleteService {
	
	@PersistenceContext
    private EntityManager entityManager;
	
	@Autowired
    private TCoUserRepository tCoUserRepository;
	
	@Autowired
    Util util;
	
	/**
  	 * 입찰완료 리스트
  	 * @param params
  	 * @return
  	 */
//	@Transactional
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Page complateBidList(Map<String, Object> params) {
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userAuth = "";
		String userId = "";
		String userInterrelatedCustCode = "";
		
		if (userOptional.isPresent()) {//그룹사인 경우
			//userAuth(1 = 시스템관리자, 2 = 각사관리자, 3 = 일반사용자, 4 = 감사사용자)
			userAuth = userOptional.get().getUserAuth();
			userId = userOptional.get().getUserId();
			userInterrelatedCustCode = userOptional.get().getInterrelatedCustCode();
		}
		
		try {
			StringBuilder sbCount = new StringBuilder(
				 "select	count(1) "
				+ "from t_bi_info_mat tbim "
			);
			
			StringBuilder sbList = new StringBuilder(
				 "select	tbim.BI_NO "
				+ ", tbim.BI_NAME "
				+ ", DATE_FORMAT(tbim.UPDATE_DATE, '%Y-%m-%d %H:%i') as UPDATE_DATE "
				+ ", tbim.BI_MODE "
				+ ", tbim.ING_TAG "
				+ ", tbim.INS_MODE "
				+ ", tcu.USER_NAME "
				+ ", tcu.USER_EMAIL "
				+ "from t_bi_info_mat tbim "
				+ "left outer join t_co_user tcu "
				+ "	on tbim.CREATE_USER = tcu.USER_ID "
			);
			
			//조회조건 공통
			StringBuilder sbWhereIf = new StringBuilder("");
			
			if (!StringUtils.isEmpty(params.get("biNo"))) {
				sbWhereIf.append("and tbim.BI_NO = :biNo ");
			}
			if (!StringUtils.isEmpty(params.get("biName"))) {
				sbWhereIf.append("and tbim.BI_NAME like concat('%',:biName,'%') ");
			}
			
			Boolean succBi = (Boolean) params.get("succBi");
			Boolean ingBi = (Boolean) params.get("ingBi");
			if (succBi && ingBi) {
				sbWhereIf.append("and tbim.ING_TAG IN ('A5', 'A7') ");
			}else if (!succBi && ingBi) {
				sbWhereIf.append("and tbim.ING_TAG IN ('A7') ");
			}else if (succBi && !ingBi) {
				sbWhereIf.append("and tbim.ING_TAG IN ('A5') ");
			}
			
			//권한에 따른 조회쿼리 
			if(userAuth.equals("4")) {	//감사사용자
				
				String addSql = "inner join t_co_user_interrelated tcui "
							  + "	on tbim.INTERRELATED_CUST_CODE = tcui.INTERRELATED_CUST_CODE ";
				
				sbCount.append(addSql);
				sbList.append(addSql);
				
				//조건문 쿼리 삽입
				StringBuilder sbWhere = new StringBuilder();
				
				sbWhere.append("where 1=1 ");
				sbWhere.append("and tcui.USER_ID = :userId ");
				sbWhere.append(sbWhereIf);		//조회조건 공통
				
				sbCount.append(sbWhere);
				sbList.append(sbWhere);
				
				sbList.append("order by tbim.UPDATE_DATE desc ");
				
			}else if (userAuth.equals("2") || userAuth.equals("3")) {	//각사관리자, 일반사용자
					
				//조건문 쿼리 삽입
				StringBuilder sbWhere = new StringBuilder();
				sbWhere.append("where 1=1 ");
				sbWhere.append(
					  "AND tbim.INTERRELATED_CUST_CODE = :interrelatedCustCode "		//계열사 코드
					+ "AND (tbim.CREATE_USER = :userId "		//담당자
					+ "or tbim.OPEN_ATT1 = :userId "		//입회자1
					+ "or tbim.OPEN_ATT2 = :userId "		//입회자2
					+ "or tbim.EST_OPENER = :userId "		//개찰자
					+ "or tbim.EST_BIDDER = :userId "		//낙찰자
					+ "or tbim.GONGO_ID = :userId) "		//공고자
				);
				
				sbWhere.append(sbWhereIf);		//조회조건 공통
				
				sbCount.append(sbWhere);
				sbList.append(sbWhere);
				
				sbList.append("order by tbim.UPDATE_DATE desc ");
			
			}else {
				
				//조건문 쿼리 삽입
				sbCount.append("where 1=1 " + sbWhereIf);
				sbList.append("where 1=1 " + sbWhereIf);
				
				sbList.append("order by tbim.UPDATE_DATE desc ");
			}
			
			//쿼리 실행
			Query queryList = entityManager.createNativeQuery(sbList.toString());
			Query queryTotal = entityManager.createNativeQuery(sbCount.toString());
			
			//조건 대입
			if(userAuth.equals("4") || userAuth.equals("2") || userAuth.equals("3")) {	//감사사용자, 각사관리자, 일반사용자
				
				if(!userAuth.equals("4")) {
					queryList.setParameter("interrelatedCustCode", userInterrelatedCustCode);
					queryTotal.setParameter("interrelatedCustCode", userInterrelatedCustCode);	
				}
				queryList.setParameter("userId", userId);
				queryTotal.setParameter("userId", userId);
				
			}
			
			if (!StringUtils.isEmpty(params.get("biNo"))) {
				queryList.setParameter("biNo", params.get("biNo"));
				queryTotal.setParameter("biNo", params.get("biNo"));
			}
			
			if (!StringUtils.isEmpty(params.get("biName"))) {
				queryList.setParameter("biName", params.get("biName"));
				queryTotal.setParameter("biName", params.get("biName"));
			}
			
			Pageable pageable = PagaUtils.pageable(params);
			queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();
			List list = new JpaResultMapper().list(queryList, BidCompleteDto.class);
			
			BigInteger count = (BigInteger) queryTotal.getSingleResult();
			return new PageImpl(list, pageable, count.intValue());
			
		}catch(Exception e) {
			log.error("bidComplete list error : {}", e);
			e.printStackTrace();
			return new PageImpl(new ArrayList<>());
		}
	}
	
	/**
  	 * 입찰완료 상세
  	 * @param params
  	 * @return
  	 */
	@Transactional
	public ResultBody complateBidDetail(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		return resultBody;
	
	}
}
