package iljin.framework.ebid.etc.statistics.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.bid.dto.BidCompleteDto;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.statistics.dto.BiInfoDetailDto;
import iljin.framework.ebid.etc.statistics.dto.BiInfoDto;
import iljin.framework.ebid.etc.statistics.dto.CoInterDto;
import iljin.framework.ebid.etc.util.GeneralDao;
import iljin.framework.ebid.etc.util.PagaUtils;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class StatisticsService {
	
    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private TCoUserRepository tCoUserRepository;
	
    @Autowired
    GeneralDao generalDao;
    
	//계열사 목록 조회
	public ResultBody selectCoInterList(Map<String, Object> params) throws Exception {

		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String userId = principal.getUsername();
		params.put("userId", userId);

		ResultBody resultBody = new ResultBody();
		List<Object> list = generalDao.selectGernalList("statistics.selectCoInterList", params);
		resultBody.setData(list);
        return resultBody;
	}
	
	
	//회사별 입찰실적 리스트 조회
	public ResultBody selectBiInfoList(@RequestBody Map<String, Object> params) throws Exception {

		ResultBody resultBody = new ResultBody();
		List<Object> list = generalDao.selectGernalList("statistics.selectBiInfoList", params);
		resultBody.setData(list);
        return resultBody;
	}
	
	/**
	 * 입찰실적 상세 내역
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResultBody biInfoDetailList(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();

		try {
			Page listPage = generalDao.selectGernalListPage("statistics.biInfoDetailList", params);
			resultBody.setData(listPage);
			
		}catch(Exception e) {
			log.error("bidDetailList list error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰실적 상세내역 리스트를 가져오는 것을 실패하였습니다.");	
		}
		
		return resultBody;
	}

	/**
	 * 입찰현황 리스트 조회
	 * 
	 * @param params
	 * @return
	 * @throws Exception 
	 */
	public ResultBody bidPresentList(Map<String, Object> params) throws Exception {
		
		//세션 정보 조회
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		String userId = principal.getUsername();
		params.put("userId", userId);

		ResultBody resultBody = new ResultBody();
		List<Object> list = generalDao.selectGernalList("statistics.bidPresentList", params);
		resultBody.setData(list);
        return resultBody;
	}
	
	/**
	 * 입찰 상세내역 리스트
	 * @param params : (String) biNo, (String) startDate, (String) endDate, (String) interrelatedCustCode
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResultBody bidDetailList(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = userOptional.get().getUserId();
		params.put("userId", userId);
		try {
			Page listPage = generalDao.selectGernalListPage("statistics.bidDetailList", params);
			resultBody.setData(listPage);
			
			
		}catch(Exception e) {
			log.error("bidDetailList list error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰상세내역 리스트를 가져오는것을 실패하였습니다.");	
		}
		
		return resultBody;
	}
}
