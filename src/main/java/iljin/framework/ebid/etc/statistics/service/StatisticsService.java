package iljin.framework.ebid.etc.statistics.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.GeneralDao;
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
	@SuppressWarnings("unchecked")
	public ResultBody selectBiInfoList(@RequestBody Map<String, Object> params, CustomUserDetails user) throws Exception {
		ResultBody resultBody = new ResultBody();
		
		// coInters setting
		this.setCoInterList(params, user);
		
		List<Object> list = generalDao.selectGernalList("statistics.selectBiInfoList", params);
		resultBody.setData(list);
		
		return resultBody;
	}
	
	/**
	 * 입찰실적 상세 내역
	 * @param params
	 * @param user 
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResultBody biInfoDetailList(Map<String, Object> params, CustomUserDetails user) throws Exception {
		ResultBody resultBody = new ResultBody();

		// coInters setting
		this.setCoInterList(params, user);
		
		Page listPage = generalDao.selectGernalListPage("statistics.biInfoDetailList", params);
		resultBody.setData(listPage);
		
		return resultBody;
	}
	
	/**
	 * 감사사용자 : 본인이 속한 계열사 리스트 조회 / 시트템관리자 : 전체 계열사 리스트 조회
	 * 조회조건 중 selCoInter 값이 있으면 해당 건만 조회
	 * @param params
	 * @param user
	 * @throws Exception
	 */
	public void setCoInterList(Map<String, Object> params, CustomUserDetails user) throws Exception {

		params.put("userId", user.getUsername());
		params.put("userAuth", user.getUserAuth());

		String srcCoInter = CommonUtils.getString(params.get("srcCoInter"));						// 조회조건 계열사
		
		// 감사사용자 : 본인이 속한 계열사 리스트 조회 / 시스템사용자 전체 계열사 조회
		List<Object> coInterList = generalDao.selectGernalList("statistics.selectCoInterList", params);
		List<String> coInters = new ArrayList<>();
		
		// 조회조건 '계열사'의 값이 있는 경우 해당 계열사만 조회
		if("".equals(srcCoInter)) {
			for(Object obj : coInterList) {
				Map<String, Object> coInterMap = (Map<String, Object>) obj;
				coInters.add(CommonUtils.getString(coInterMap.get("interrelatedCustCode")));
			}
		} else {
			coInters.add(srcCoInter);
		}
		
		params.put("coInters", coInters);
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
