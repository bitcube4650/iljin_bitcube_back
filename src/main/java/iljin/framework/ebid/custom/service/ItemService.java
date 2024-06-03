package iljin.framework.ebid.custom.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
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
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.ebid.custom.dto.TCoItemDto;
import iljin.framework.ebid.custom.entity.TCoItem;
import iljin.framework.ebid.custom.entity.TCoItemGrp;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoItemGrpRepository;
import iljin.framework.ebid.custom.repository.TCoItemRepository;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.GeneralDao;
import iljin.framework.ebid.etc.util.PagaUtils;
import iljin.framework.ebid.etc.util.common.consts.DB;
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
	@Autowired
	private GeneralDao generalDao;
	
	public ResultBody itemGrpList() throws Exception {
		ResultBody resultBody = new ResultBody();
			
		try {
			List grpList = generalDao.selectGernalList(DB.QRY_SELECT_ITEM_GRP_LIST, new HashMap());
			resultBody.setData(grpList);
		}catch(Exception e) {
			e.printStackTrace();
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("An error occurred while selecting the itemGrpList.");
			resultBody.setData(e.getMessage());
		}
		return resultBody;
	}

	@SuppressWarnings("rawtypes")
	public ResultBody itemList(Map<String, Object> params) throws Exception{
		ResultBody resultBody = new ResultBody();
		
		if(params.get("size") != null && params.get("page") != null) {
			params.put("offset", CommonUtils.getInt(params.get("size")) * CommonUtils.getInt(params.get("page")));
			params.put("size", CommonUtils.getInt(params.get("size")));
		}
		
		try {
			Page listPage = generalDao.selectGernalListPage(DB.QRY_SELECT_ITEM_LIST, params);
			resultBody.setData(listPage);
			
		}catch(Exception e) {
			e.printStackTrace();
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("An error occurred while selecting the itemList.");
			resultBody.setData(e.getMessage());
		}
		

		return resultBody;

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
    
    //품목 상세 조회
    public ResultBody findById(String id) throws Exception {
    	ResultBody resultBody = new ResultBody();
    	
    	try {
    		Optional<TCoItem> result = tCoItemRepository.findById(id);
			resultBody.setData(result);
		}catch(Exception e) {
			e.printStackTrace();
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("An error occurred while selecting the item detail.");
			resultBody.setData(e.getMessage());
		}
  
        return resultBody;
    }

    // 품목 등록
    @Transactional
    public ResultBody save(Map<String, Object> params, CustomUserDetails user) {
        ResultBody resultBody = new ResultBody();
        
        params.put("dupCheck", "Y");
        params.put("itemCodeDetail", params.get("itemCode"));
        params.put("createUser", user.getUsername());
        
        try {
        	//품목코드에 해당하는 데이터가 있는지 조회
        	int cnt = (int) generalDao.selectGernalCount(DB.QRY_SELECT_ITEM_CNT, params);
        	if(cnt >0) {
        		//이미 있는 품목 코드인 경우
        		resultBody.setCode("DUP");
        		resultBody.setStatus(500);
    			resultBody.setMsg("An error occurred while inserting the item.");
    			
        	}else {
        		//품목 테이블에 insert
        		generalDao.insertGernal(DB.QRY_INSERT_ITEM, params);
        	}
			
		} catch (Exception e) {
			e.printStackTrace();
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("An error occurred while inserting the item.");
			resultBody.setData(e.getMessage());
		}
        
        return resultBody;
    }

    // 품목 수정
    @Transactional
	public ResultBody saveUpdate(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		try {
			generalDao.updateGernal(DB.QRY_UPDATE_ITEM, params);
			
		} catch (Exception e) {
			e.printStackTrace();
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("An error occurred while updating the item.");
			resultBody.setData(e.getMessage());
		}
		
        
        return resultBody;
	}
}
