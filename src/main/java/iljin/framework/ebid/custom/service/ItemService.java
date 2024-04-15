package iljin.framework.ebid.custom.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.custom.dto.TCoItemDto;
import iljin.framework.ebid.custom.entity.TCoItem;
import iljin.framework.ebid.custom.entity.TCoItemGrp;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoItemGrpRepository;
import iljin.framework.ebid.custom.repository.TCoItemRepository;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.PagaUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ItemService {

    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private TCoItemRepository tCoItemRepository;
    @Autowired
    private TCoItemGrpRepository tCoItemGrpRepository;
    @Autowired
    private TCoUserRepository tCoUserRepository;

    public List itemGrpList() {
        return tCoItemGrpRepository.findAll();
    }

    public Page itemList(Map<String, Object> params) {
    	StringBuilder sbCount = new StringBuilder(" SELECT COUNT(1) FROM T_CO_ITEM A LEFT OUTER JOIN T_CO_USER B ON A.CREATE_USER = B.USER_ID INNER JOIN T_CO_ITEM_GRP C ON A.ITEM_GRP_CD = C.ITEM_GRP_CD WHERE 1=1  ");
        StringBuilder sbList = new StringBuilder(" SELECT A.ITEM_CODE , A.ITEM_NAME , C.GRP_NM , A.USE_YN , B.USER_NAME AS CREATE_USER , DATE_FORMAT(A.CREATE_DATE, '%Y-%m-%d %h:%m:%s') AS CREATE_DATE  FROM T_CO_ITEM A LEFT OUTER JOIN T_CO_USER B ON A.CREATE_USER = B.USER_ID INNER JOIN T_CO_ITEM_GRP C ON A.ITEM_GRP_CD = C.ITEM_GRP_CD WHERE 1=1 ");
        StringBuilder sbWhere = new StringBuilder();

        if (!StringUtils.isEmpty(params.get("itemGrp"))) {
            sbWhere.append(" and A.ITEM_GRP_CD = :itemGrp");
        }
        if (!StringUtils.isEmpty(params.get("useYn"))) {
            sbWhere.append(" and a.use_yn = :useYn");
        }
        
        if(params.containsKey("nonPopYn")) {
            if (!StringUtils.isEmpty(params.get("itemCode"))) {
                sbWhere.append(" and A.ITEM_CODE like concat('%',:itemCode,'%')");
            }
            if (!StringUtils.isEmpty(params.get("itemName"))) {
                sbWhere.append(" and A.ITEM_NAME like concat('%',:itemName,'%')");
            }
        }else if(!params.containsKey("nonPopYn") && !StringUtils.isEmpty(params.get("itemName"))) {
        	sbWhere.append(" and ( A.ITEM_CODE like concat('%',:itemName,'%')  or A.ITEM_NAME like concat('%',:itemName,'%'))");
        }

        
        sbList.append(sbWhere);
        sbList.append(" order by A.ITEM_CODE ");
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        sbCount.append(sbWhere);
        Query queryTotal = entityManager.createNativeQuery(sbCount.toString());

        if (!StringUtils.isEmpty(params.get("itemGrp"))) {
            queryList.setParameter("itemGrp", params.get("itemGrp"));
            queryTotal.setParameter("itemGrp", params.get("itemGrp"));
        }
        if (!StringUtils.isEmpty(params.get("useYn"))) {
            queryList.setParameter("useYn", params.get("useYn"));
            queryTotal.setParameter("useYn", params.get("useYn"));
        }
        if (!StringUtils.isEmpty(params.get("itemCode"))) {
            queryList.setParameter("itemCode", params.get("itemCode"));
            queryTotal.setParameter("itemCode", params.get("itemCode"));
        }
        if (!StringUtils.isEmpty(params.get("itemName"))) {
            queryList.setParameter("itemName", params.get("itemName"));
            queryTotal.setParameter("itemName", params.get("itemName"));
        }

        Pageable pageable = PagaUtils.pageable(params);
        queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();
        List list = new JpaResultMapper().list(queryList, TCoItemDto.class);

        BigInteger count = (BigInteger) queryTotal.getSingleResult();
        return new PageImpl(list, pageable, count.intValue());
    	
        //return tCoItemRepository.findAll(searchWith(params), PagaUtils.pageable(params, "itemCode"));
    }

    public Specification<TCoItem> searchWith(Map<String, Object> params) {
        return (Specification<TCoItem>) ((root, query, builder) -> {
            List<Predicate> predicate = getPredicateWithKeyword(params, root, builder);
            return builder.and(predicate.toArray(new Predicate[0]));
        });
    }

    private List<Predicate> getPredicateWithKeyword(Map<String, Object> params, Root<TCoItem> root, CriteriaBuilder builder) {
        List<Predicate> predicate = new ArrayList<>();
        List<Predicate> orPredicates = new ArrayList<>(); 

        for (String key : params.keySet()) {
            Object value = params.get(key);
            if (value == null || "".equals(value)) continue;
            switch (key) {
                case "itemGrp":
                    TCoItemGrp itemGrp = tCoItemGrpRepository.findById((String) value).get();
                    predicate.add(builder.equal(root.get(key), itemGrp));
                    break;
                case "useYn":
                    predicate.add(builder.equal(root.get(key), value));
                    break;
                case "itemCode":
                case "itemName":
                    orPredicates.add(builder.like(root.get(key), "%" + value + "%")); 
                    break;
            }
        }

        if (!orPredicates.isEmpty()) {
            Predicate orPredicate = builder.or(orPredicates.toArray(new Predicate[0])); 
            predicate.add(orPredicate); 
        }

        return predicate;
    }
    public Optional<TCoItem> findById(String id) {
        return tCoItemRepository.findById(id);
    }

    // 품목 등록
    @SneakyThrows
    @Transactional
    public ResultBody save(TCoItem tCoItem) {
        ResultBody resultBody = new ResultBody();
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TCoUser user = tCoUserRepository.findById(principal.getUsername()).get();
        tCoItem.setCreateUser(user);
        
        // 조회 후, 존재하는 품목코드면 return
        StringBuilder sb = new StringBuilder(" SELECT COUNT(1) FROM t_co_item WHERE ITEM_CODE = :itemCode");
        Query query = entityManager.createNativeQuery(sb.toString());
        query.setParameter("itemCode", tCoItem.getItemCode());
        BigInteger cnt = (BigInteger) query.getSingleResult();
        if (cnt.longValue() > 0) {
            resultBody.setCode("DUP"); // 품목코드 중복됨 -- 이미 등록된 품목코드가 존재합니다.
        } else {
        	// 저장
            tCoItemRepository.save(tCoItem);
        }
        
        return resultBody;
    }

    // 품목 수정
    @SneakyThrows
    @Transactional
	public ResultBody saveUpdate(TCoItem tCoItem) {
		ResultBody resultBody = new ResultBody();
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TCoUser user = tCoUserRepository.findById(principal.getUsername()).get();
        tCoItem.setCreateUser(user);
        
        // 저장
        tCoItemRepository.save(tCoItem);
        
        return resultBody;
	}
}
