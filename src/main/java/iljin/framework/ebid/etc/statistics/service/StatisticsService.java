package iljin.framework.ebid.etc.statistics.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.statistics.dto.CoInterDto;


@Service
public class StatisticsService {
	
    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private TCoUserRepository tCoUserRepository;
	
	//계열사 목록 조회
	public List<List<?>> selectCoInterList() {
  
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = principal.getUsername();
		String userAuth = userOptional.get().getUserAuth();// userAuth(1 = 시스템관리자, 4 = 감사사용자)
		
		
		StringBuilder sbList = null;
		if("1".equals(userAuth)) {
			 sbList = new StringBuilder("SELECT INTERRELATED_CUST_CODE\r\n"
			 		+ ",INTERRELATED_NM  FROM t_co_interrelated");
		}else if("4".equals(userAuth)) {
			 sbList = new StringBuilder("select 	tci.INTERRELATED_CUST_CODE  \r\n"
			 		+ "	,		tci.INTERRELATED_NM \r\n"
			 		+ "	from t_co_interrelated tci \r\n"
			 		+ "	inner join t_co_user_interrelated tcui \r\n"
			 		+ "	on tci.INTERRELATED_CUST_CODE = tcui.INTERRELATED_CUST_CODE \r\n"
			 		+ "	inner join t_co_user tcu \r\n"
			 		+ "	on tcui.USER_ID = tcu.USER_ID \r\n"
			 		+ "	where tcu.USER_ID = :userId \r\n"
			 		+ "	order by INTERRELATED_NM ");
		}
		
        Query queryList = entityManager.createNativeQuery(sbList.toString());

        if("4".equals(userAuth)) {
            queryList.setParameter("userId", userId);
        }

        List<CoInterDto> resultList = new JpaResultMapper().list(queryList, CoInterDto.class);

        List<List<?>> combinedResults = new ArrayList<>();
        combinedResults.add(resultList);

        return combinedResults;
	}

}
