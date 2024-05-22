package iljin.framework.ebid.etc.main.service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections.MapUtils;
import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.AuthToken;
import iljin.framework.core.security.user.UserService;
import iljin.framework.ebid.bid.dto.InterUserInfoDto;
import iljin.framework.ebid.bid.service.BidProgressService;
import iljin.framework.ebid.custom.dto.TCoUserDto;
import iljin.framework.ebid.custom.entity.TCoCustUser;
import iljin.framework.ebid.custom.entity.TCoInterrelated;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoCustUserRepository;
import iljin.framework.ebid.custom.repository.TCoInterrelatedRepository;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.main.dto.BidCntDto;
import iljin.framework.ebid.etc.main.dto.PartnerBidCntDto;
import iljin.framework.ebid.etc.main.dto.PartnerCntDto;
import iljin.framework.ebid.etc.main.dto.PartnerCompletedBidCntDto;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.GeneralDao;
import iljin.framework.ebid.etc.util.common.consts.DB;

@Service
public class MainService {
	
	@Autowired
    private TCoUserRepository tCoUserRepository;
	
	@Autowired
    private TCoCustUserRepository tCoUserCustRepository;
	
	@Autowired
	private TCoInterrelatedRepository tCoInterrelatedRepository;
	
	@Autowired
    private BidProgressService bidProgressService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	@Autowired
	private GeneralDao generalDao;
	
	@PersistenceContext
    private EntityManager entityManager;

	//전자입찰 건수 조회(계열사메인)
	@Transactional
	public ResultBody selectBidCnt(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Map<String, Object> coUserMap = this.getCoUser();
		
		List<Object> userInfoList = new ArrayList<>();
		List<String> custCodes = new ArrayList<>();

		String userAuth = CommonUtils.getString(coUserMap.get("userAuth")); // userAuth(1 = 시스템관리자, 2 = 각사관리자, 3 = 일반사용자, 4 = 감사사용자)
		String interrelatedCode = CommonUtils.getString(coUserMap.get("interrelatedCustCode"));

		if(userAuth.equals("4")) {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("userId", principal.getUsername());
			userInfoList = generalDao.selectGernalList(DB.QRY_SELECT_INTER_CUST_CODE_LIST, paramMap);

			for(Object obj : userInfoList) {
				Map<String, Object> userInfoMap = (Map<String, Object>) obj;
				custCodes.add(CommonUtils.getString(userInfoMap.get("interrelatedCustCode")));
			}
			custCodes.add(interrelatedCode);
		}
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userId" , principal.getUsername());
		paramMap.put("userAuth" , userAuth);
		paramMap.put("interrelatedCode", interrelatedCode);
		paramMap.put("interrelatedCodeArr", custCodes);
		
		Map<String, Object> bidMap = (Map<String, Object>) generalDao.selectGernalObject(DB.QRY_SELECT_MAIN_CO_BID_CNT, paramMap);
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("planning", CommonUtils.getInt(resultMap.get("planning")));
		resultMap.put("noticing", CommonUtils.getInt(resultMap.get("noticing")));
		resultMap.put("beforeOpening", CommonUtils.getInt(resultMap.get("beforeOpening")));
		resultMap.put("opening", CommonUtils.getInt(resultMap.get("opening")));
		resultMap.put("completed", CommonUtils.getInt(resultMap.get("completed")));
		resultMap.put("unsuccessful", CommonUtils.getInt(resultMap.get("unsuccessful")));
		resultMap.put("ing", CommonUtils.getInt(resultMap.get("planning")) + CommonUtils.getInt(resultMap.get("noticing")) + CommonUtils.getInt(resultMap.get("beforeOpening")));
		
		resultBody.setData(resultMap);
		return resultBody;
	}

	//협력사 업채수 조회(계열사메인)
	@Transactional
	public ResultBody selectPartnerCnt(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		Map<String, Object> coUserMap = this.getCoUser();
		String interrelatedCode = CommonUtils.getString(coUserMap.get("interrelatedCustCode"));
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("interrelatedCode", interrelatedCode);
		Map<String, Object> partnerMap = (Map<String, Object>) generalDao.selectGernalObject(DB.QRY_SELECT_PARTNER_CNT, paramMap);
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("request", CommonUtils.getInt(partnerMap.get("request")));
		resultMap.put("approval", CommonUtils.getInt(partnerMap.get("approval")));
		resultMap.put("deletion", CommonUtils.getInt(partnerMap.get("deletion")));
		
		resultBody.setData(resultMap);
		return resultBody;
		
	}

	//협력사 전자입찰 건수 조회(협력사메인)
	@Transactional
	public ResultBody selectPartnerBidCnt(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("custCode", this.getCustCode());
		
		int noticing = CommonUtils.getInt(generalDao.selectGernalCount(DB.QRY_SELECT_BID_NOTICING_CNT, paramMap));			// 미투찰
		int submitted = CommonUtils.getInt(generalDao.selectGernalCount(DB.QRY_SELECT_BID_SUBMITTED_CNT, paramMap));		// 투찰
		int awarded = CommonUtils.getInt(generalDao.selectGernalCount(DB.QRY_SELECT_BID_AWARDED_CNT, paramMap));			// 낙찰
		int unsuccessful = CommonUtils.getInt(generalDao.selectGernalCount(DB.QRY_SELECT_BID_UNSUCCESSFUL_CNT, paramMap));	// 비선정
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("noticing", noticing);
		resultMap.put("submitted", submitted);
		resultMap.put("awarded", awarded);
		resultMap.put("unsuccessful", unsuccessful);
		resultMap.put("ing", noticing + submitted);
		resultBody.setData(resultMap);

		return resultBody;
	}

	//입찰완료 조회(협력사메인)
	@Transactional
	public ResultBody selectCompletedBidCnt(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("custCode", this.getCustCode());
		
		int posted = CommonUtils.getInt(generalDao.selectGernalCount(DB.QRY_SELECT_COMPLETE_POSTED_CNT, paramMap));			// 공고되었던 입찰
		int submitted = CommonUtils.getInt(generalDao.selectGernalCount(DB.QRY_SELECT_COMPLETE_SUBMITTED_CNT, paramMap));	// 투찰했던 입찰
		int awarded = CommonUtils.getInt(generalDao.selectGernalCount(DB.QRY_SELECT_COMPLETE_AWARDED_CNT, paramMap));		// 낙찰된 입찰
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("posted", posted);
		resultMap.put("submitted", submitted);
		resultMap.put("awarded", awarded);
		resultBody.setData(resultMap);

		return resultBody;
	}

	//비밀번호 확인
	@Transactional
	public boolean checkPwd(Map<String, Object> params) {
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String userId = principal.getUsername();
		String password = (String) params.get("password");
		
		try {
			
			return userService.checkPassword(userId, password);
		
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	//비밀번호 변경
	@Transactional
	public boolean changePwd(Map<String, Object> params) {
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String userId = principal.getUsername();
		String password = (String) params.get("password");
		LocalDateTime currentDate = LocalDateTime.now();


		try {
			Optional<TCoUser> userOptional = tCoUserRepository.findById(userId);
			String encodedPassword = passwordEncoder.encode(password);

			if (userOptional.isPresent()) {//계열사인 경우
				TCoUser tCoUser = userOptional.get();
				tCoUser.setUserPwd(encodedPassword);
				tCoUser.setPwdEditDate(currentDate);
				tCoUser.setPwdEditYn("Y");
			}else {//협력사인 경우
				Optional<TCoCustUser> userOptional2 = tCoUserCustRepository.findById(userId);
				TCoCustUser tCoCustUser = userOptional2.get();
				tCoCustUser.setUserPwd(encodedPassword);
				tCoCustUser.setPwdChgDate(currentDate);
			}
			
			return true;

		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	//유저정보 조회
	@Transactional
	public ResultBody selectUserInfo(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String userId = principal.getUsername();
		Map<String,Object> userInfo = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		try {
			Optional<TCoUser> userOptional = tCoUserRepository.findById(userId);
	
			if (userOptional.isPresent()) {//계열사인 경우
				String formattedDateTime = "";
				
				String bidauth = userOptional.get().getBidauth();
				String deptName = userOptional.get().getDeptName();
				String openauth = userOptional.get().getOpenauth();
				LocalDateTime pwdEditDate = userOptional.get().getPwdEditDate();
				String pwdEditYn = userOptional.get().getPwdEditYn();
				String userEmail = userOptional.get().getUserEmail();
				String userHp = userOptional.get().getUserHp();
				String userPosition = userOptional.get().getUserPosition();
				String userTel = userOptional.get().getUserTel();
				
		        if(pwdEditDate != null) {
					formattedDateTime = pwdEditDate.format(formatter);
				}
				
				userInfo.put("bidauth", bidauth);
				userInfo.put("deptName", deptName);
				userInfo.put("openauth", openauth);
				userInfo.put("pwdEditDate", formattedDateTime);
				userInfo.put("pwdEditYn", pwdEditYn);
				userInfo.put("userEmail", userEmail);
				userInfo.put("userHp", userHp);
				userInfo.put("userPosition",userPosition);
				userInfo.put("userTel", userTel);
				
				resultBody.setData(userInfo);
				
				return resultBody;
				
			}else {//협력사인 경우
				
				Optional<TCoCustUser> userOptional2 = tCoUserCustRepository.findById(userId);
				String formattedDateTime = "";
				
				if(userOptional2.isPresent()) {
				
					String userTel = userOptional2.get().getUserTel();
					String userHp = userOptional2.get().getUserHp();
					String userEmail = userOptional2.get().getUserEmail();
					LocalDateTime pwdChgDate = userOptional2.get().getPwdChgDate();
					String userBuseo = userOptional2.get().getUserBuseo();
					String userPosition = userOptional2.get().getUserPosition();
					
					if(pwdChgDate != null) {
						formattedDateTime = pwdChgDate.format(formatter);
					}
					
					userInfo.put("userTel", userTel);
					userInfo.put("userHp", userHp);
					userInfo.put("userEmail", userEmail);
					userInfo.put("pwdChgDate", formattedDateTime);
					userInfo.put("userBuseo", userBuseo);
					userInfo.put("userPosition", userPosition);
				}
				
				resultBody.setData(userInfo);
				
				return resultBody;
			}
	
		}catch(Exception e){
			e.printStackTrace();
			resultBody.setCode("ERROR");
	        resultBody.setStatus(500);
	        resultBody.setMsg("An error occurred while selecting the user info.");
	        resultBody.setData(e.getMessage());
	        
	        return resultBody;
		}
	}

	//유저 정보 변경
	@Transactional
	public ResultBody saveUserInfo(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String userId = principal.getUsername();
		
		try {
			Optional<TCoUser> userOptional = tCoUserRepository.findById(userId);
	
			if (userOptional.isPresent()) {//계열사인 경우
				
				TCoUser tCoUser = userOptional.get();

				String bidauth = "";
				String openauth = "";
				String deptName = (String) params.get("deptName");
				String userEmail = (String) params.get("userEmail");
				String userHp = (String) params.get("userHp");
				String userPosition = (String) params.get("userPosition");
				String userTel = (String) params.get("userTel");
				
				if((boolean) params.get("bidauth")) {
					bidauth = "1";
				}else {
					bidauth = "";
				}
				
				if((boolean) params.get("openauth")) {
					openauth = "1";
				}else {
					openauth = "";
				}
				
				tCoUser.setBidauth(bidauth);
				tCoUser.setDeptName(deptName);
				tCoUser.setOpenauth(openauth);
				tCoUser.setUserEmail(userEmail);
				tCoUser.setUserHp(userHp);
				tCoUser.setUserPosition(userPosition);
				tCoUser.setUserTel(userTel);
				
				return resultBody;
				
			}else {//협력사인 경우
				
				Optional<TCoCustUser> userOptional2 = tCoUserCustRepository.findById(userId);
				
				TCoCustUser tCoCustUser = userOptional2.get();
				
				String userTel = (String) params.get("userTel");
				String userHp = (String) params.get("userHp");
				String userEmail = (String) params.get("userEmail");
				String userBuseo = (String) params.get("userBuseo");
				String userPosition = (String) params.get("userPosition");
				
				tCoCustUser.setUserTel(userTel);
				tCoCustUser.setUserHp(userHp);
				tCoCustUser.setUserEmail(userEmail);
				tCoCustUser.setUserBuseo(userBuseo);
				tCoCustUser.setUserPosition(userPosition);
				
				return resultBody;
			}
	
		}catch(Exception e){
			e.printStackTrace();
			resultBody.setCode("ERROR");
	        resultBody.setStatus(500);
	        resultBody.setMsg("An error occurred while updating the user info.");
	        resultBody.setData(e.getMessage());
	        
	        return resultBody;
		}
	}

	//계열사 정보 조회
	public ResultBody selectCompInfo(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		String custCode = "";
		 
		try {
			if (!StringUtils.isEmpty(params.get("custCode"))) {
				custCode = (String) params.get("custCode");
			}
			Optional<TCoInterrelated> tCoInterrelated = tCoInterrelatedRepository.findById(custCode);
			
			resultBody.setData(tCoInterrelated.get());
			
		}catch(Exception e) {
			resultBody.setCode("ERROR");
	        resultBody.setStatus(500);
	        resultBody.setMsg("An error occurred while selecting the company info.");
	        resultBody.setData(e.getMessage());
		}
		
		return resultBody;
	}
	
	//비밀번호 변경 권장 플래그
	public ResultBody chkPwChangeEncourage(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		resultBody.setData(false);
		
		LocalDateTime currentDate = LocalDateTime.now();	//현재시간
		LocalDateTime pwChangeDate = null;					//비밀번호 변경일
		
		String userId = CommonUtils.getString(params.get("userId"));
		Boolean isGroup = (Boolean) params.get("isGroup");
		
		if(isGroup) {
			String userOptional = CommonUtils.getString(generalDao.selectGernalObject(DB.QRY_SELECT_GROUP_PWD_EDIT_DATE, params));
			if (!userOptional.isEmpty()) {//계열사인 경우
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				pwChangeDate = LocalDateTime.parse(userOptional, formatter);
			}
		}else {
			String userOptional = CommonUtils.getString(generalDao.selectGernalObject(DB.QRY_SELECT_PWD_CHG_DATE, params));
			if (!userOptional.isEmpty()) {//계열사인 경우
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				pwChangeDate = LocalDateTime.parse(userOptional, formatter);
			}
		}
		
		//비밀번호 변경일이 null이거나 1년이상 지난경우
		if(pwChangeDate != null) {
			LocalDateTime pwChangeDatePlusYear = pwChangeDate.plusYears(1);
			resultBody.setData(currentDate.isAfter(pwChangeDatePlusYear));
		}else {
			resultBody.setData(true);
		}
		
		return resultBody;
	}
	
	// 초기 계열사 사용자 비밀번호 변경 처리
    @SuppressWarnings("rawtypes")
	@Transactional
	public void chgPwdFirst() {
//		log.info("-----------------------chgPwdFirst service start----------------------");
		// 계열사 사용자 리스트 조회
    	// 새로 쿼리를 짜면 dto를 하나 더 생성해야해서 기존 쿼리에서 쓰던 dto를 사용하기위해 쿼리가 길어짐
		StringBuilder sbList = new StringBuilder(" select "
				+ "user_name"
				+ ", user_id"
				+ ", user_position"
				+ ", dept_name"
				+ ", user_tel"
				+ ", user_hp"
				+ ", user_auth"
				+ ", use_yn"
				+ ", interrelated_cust_code as interrelated_cust_nm "
				+ "from t_co_user a "
				+ "where 1=1 "
//				+ "and user_id = 'gaksa01' "
				+ " ");
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        List list = new JpaResultMapper().list(queryList, TCoUserDto.class);
        
        for(int i = 0; i < list.size(); i++) {
        	TCoUserDto userInfo = (TCoUserDto) list.get(i);
        	String userId = userInfo.getUserId();
        	// 패스워드 규칙 사용자 아이디 + !@# 
    		String chgPwd = userId + "!@#";
    		// 비밀번호 암호화
    		String encodedPassword = passwordEncoder.encode(chgPwd);
    		// 업데이트
    		StringBuilder sbQuery = new StringBuilder(
    	            " update t_co_user "
    	            + " set user_pwd = :userPwd "
//    	            + ", pwd_edit_yn = 'Y'"
//    	            + ", pwd_edit_date = now()"
//    	            + ", update_user = :updateUser"
//    	            + ", update_date = now() "
    	            + " where user_id = :userId ");
            Query query = entityManager.createNativeQuery(sbQuery.toString());
            query.setParameter("userId", userId);
            query.setParameter("userPwd", encodedPassword);
            query.executeUpdate();
        }
//        log.info("-----------------------chgPwdFirst service end----------------------");
	}
	
	private int getCustCode() throws Exception {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		paramMap.put("userId", principal.getUsername());
		Map<String, Object> custMap = (Map<String, Object>) generalDao.selectGernalObject(DB.QRY_SELECT_COMMON_CUST_USER_DETAIL, paramMap);
		return CommonUtils.getInt(custMap.get("custCode"));
	}
	
	private Map<String, Object> getCoUser() throws Exception {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		paramMap.put("userId", principal.getUsername());
		Map<String, Object> userMap = (Map<String, Object>) generalDao.selectGernalObject(DB.QRY_SELECT_COMMON_CO_USER_DETAIL, paramMap);
		return userMap;
	}

}
