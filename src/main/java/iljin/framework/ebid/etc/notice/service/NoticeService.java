package iljin.framework.ebid.etc.notice.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import iljin.framework.ebid.custom.dto.TCoUserDto;
import iljin.framework.ebid.custom.service.UserService;
import iljin.framework.ebid.etc.notice.dto.NoticeDto;
import iljin.framework.ebid.etc.util.PagaUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NoticeService {
	
	@PersistenceContext
    private EntityManager entityManager;
	
	//공지사항 목록 조회
	public Page noticeList(Map<String, Object> params) {
		
        StringBuilder sbCount = new StringBuilder(" select count(1) "
        										 +" from ( "
        										 	    + " ( "
        										 	    	+ " select tcbn.* ,"
        										 	    	       + " tcu.USER_NAME as bUserName "
        												    + " from t_co_board_notice tcbn "
        												    + " left outer join t_co_user tcu "
        												    + " on (tcbn.b_userid = tcu.user_id) "
        												    + " where tcbn.B_CO = 'ALL' "
        											    + " ) "
        												+ " union all "
        												+ "	("
        												    + " select tcbn2.* , "
        												    	   + " tcu2.USER_NAME as bUserName "
        												    + "	from t_co_board_notice tcbn2 "
        												    + "	inner join t_co_board_cust tcbc "
        												    + "	on(tcbn2.B_NO = tcbc.B_NO) "
        												    + " left outer join t_co_user tcu2 "
        												    + " on (tcbn2.B_USERID = tcu2.USER_ID) "
        												    + "	where tcbn2.B_CO = 'CUST' "
        												    + "	and tcbc.INTERRELATED_CUST_CODE = :custCode"
        												+ " ) "
        											 + " ) rst "
        										+ "	where 1=1 "
        										);
        
        StringBuilder sbList = new StringBuilder(" select rst.* , "
        											  + " row_number() over( order by rst.b_date desc ) as rowNo "
								        		+" from ( "
												 	    + " ( "
												 	    	+ " select tcbn.* ,"
												 	    	       + " tcu.USER_NAME as userName "
														    + " from t_co_board_notice tcbn "
														    + " left outer join t_co_user tcu "
														    + " on (tcbn.b_userid = tcu.user_id) "
														    + " where tcbn.B_CO = 'ALL' "
													    + " ) "
														+ " union all "
														+ "	("
														    + " select tcbn2.* , "
														    	   + " tcu2.USER_NAME as userName "
														    + "	from t_co_board_notice tcbn2 "
														    + "	inner join t_co_board_cust tcbc "
														    + "	on(tcbn2.B_NO = tcbc.B_NO) "
														    + " left outer join t_co_user tcu2 "
														    + " on (tcbn2.B_USERID = tcu2.USER_ID) "
														    + "	where tcbn2.B_CO = 'CUST' "
														    + "	and tcbc.INTERRELATED_CUST_CODE = :custCode"
														+ " ) "
													+ " ) rst "
											   + " where 1=1 "
										);
        StringBuilder sbWhere = new StringBuilder();

        if (!StringUtils.isEmpty(params.get("title"))) {
            sbWhere.append(" and b_title like concat('%',:title,'%')");
        }
        if (!StringUtils.isEmpty(params.get("content"))) {
            sbWhere.append(" and b_content like concat('%',:content,'%')");
        }
        if (!StringUtils.isEmpty(params.get("userId"))) {
            sbWhere.append(" and b_userid like concat('%',:userId,'%')");
        }
        
        sbList.append(sbWhere);
        sbList.append(" order by b_date desc");
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        sbCount.append(sbWhere);
        Query queryTotal = entityManager.createNativeQuery(sbCount.toString());

        if (!StringUtils.isEmpty(params.get("custCode"))) {
            queryList.setParameter("custCode", params.get("custCode"));
            queryTotal.setParameter("custCode", params.get("custCode"));
        }
        if (!StringUtils.isEmpty(params.get("title"))) {
            queryList.setParameter("title", params.get("title"));
            queryTotal.setParameter("title", params.get("title"));
        }
        if (!StringUtils.isEmpty(params.get("content"))) {
            queryList.setParameter("content", params.get("content"));
            queryTotal.setParameter("content", params.get("content"));
        }
        if (!StringUtils.isEmpty(params.get("userId"))) {
            queryList.setParameter("userId", params.get("userId"));
            queryTotal.setParameter("userId", params.get("userId"));
        }

        Pageable pageable = PagaUtils.pageable(params);
        queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();
        List list = new JpaResultMapper().list(queryList, NoticeDto.class);

        BigInteger count = (BigInteger) queryTotal.getSingleResult();
        return new PageImpl(list, pageable, count.intValue());
    }
	
	/*
	@Autowired
    private NoticeRepository noticeRepository;
	
	public Page noticeList(Map<String, Object> params) {
        return noticeRepository.findAll(searchWith(params), PagaUtils.pageable(params, "createDate"));
    }
	
	public Specification<Notice> searchWith(Map<String, Object> params) {
        return (Specification<Notice>) ((root, query, builder) -> {
            List<Predicate> predicate = getPredicateWithKeyword(params, root, builder);
            return builder.and(predicate.toArray(new Predicate[0]));
        });
    }
	
	private List<Predicate> getPredicateWithKeyword(Map<String, Object> params, Root<Notice> root, CriteriaBuilder builder) {
        List<Predicate> predicate = new ArrayList<>();
        for (String key : params.keySet()) {
            Object value = params.get(key);
            if (value == null || "".equals(value)) continue;
            switch (key) {
                case "title":
                    predicate.add(builder.equal(root.get(key),"%"+value+"%"));
                    break;
                case "content":
                	predicate.add(builder.equal(root.get(key),"%"+value+"%"));
                    break;
                case "registrant":
                    predicate.add(builder.like(root.get(key),"%"+value+"%"));
                    break;
            }
        }
        return predicate;
    }
    */
}
