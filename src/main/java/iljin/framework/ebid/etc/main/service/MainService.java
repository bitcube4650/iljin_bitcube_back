package iljin.framework.ebid.etc.main.service;

import java.math.BigDecimal;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.UserService;
import iljin.framework.ebid.bid.dto.InterUserInfoDto;
import iljin.framework.ebid.bid.service.BidProgressService;
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
	
	@PersistenceContext
    private EntityManager entityManager;

	//전자입찰 건수 조회(계열사메인)
	@Transactional
	public BidCntDto selectBidCnt(Map<String, Object> params) {
		BidCntDto bidCntDto = new BidCntDto();
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userAuth = "";// userAuth(1 = 시스템관리자, 2 = 각사관리자, 3 = 일반사용자, 4 = 감사사용자)
		String interrelatedCode = "";
		String userId = principal.getUsername();
		
		List<InterUserInfoDto> userInfoList = new ArrayList<>(); 
        List<String> custCodes = new ArrayList<>();
        
		
		if (userOptional.isPresent()) {
        	
        	userAuth = userOptional.get().getUserAuth();
    		interrelatedCode = userOptional.get().getInterrelatedCustCode();
  
        	if(userAuth.equals("4")) {//감사사용자에 해당하는 계열사 조회
        		userInfoList = (List<InterUserInfoDto>) bidProgressService.findInterCustCode(userId);
        		//감사사용자에 해당하는 계열사리스트 담기
                for (InterUserInfoDto userInfo : userInfoList) {
                    custCodes.add(userInfo.getInterrelatedCustCode());
                }
        	}
        	
        	
        }
		

		StringBuilder sbCnt = new StringBuilder(" select COUNT(CASE WHEN ing_tag = 'A0' THEN 1 END) AS planning, "
												      + " COUNT(CASE WHEN ing_tag IN ('A1', 'A3') and est_close_date >= SYSDATE() THEN 1 END) AS noticing, "
												      + " COUNT(CASE WHEN ing_tag IN ('A1', 'A3') AND est_close_date < SYSDATE() THEN 1 END) AS beforeOpening, "
												      + " COUNT(CASE WHEN ing_tag = 'A2' THEN 1 END) AS opening, "
												      + " COUNT(CASE WHEN ing_tag = 'A5' AND update_date >= CURDATE() - INTERVAL 12 MONTH THEN 1 END) AS completed, "
												      + " COUNT(CASE WHEN ing_tag = 'A7' AND update_date >= CURDATE() - INTERVAL 12 MONTH THEN 1 END) AS unsuccessful, "
												      + " COUNT(CASE WHEN ing_tag IN ('A1','A2', 'A3') THEN 1 END) AS ing "			// 입찰 진행 전체 건수
											   + " from t_bi_info_mat "
											   + " where 1=1 "											   
												);
		

		
		StringBuilder sbWhere = new StringBuilder();
		
		//계열사 조건
		//userAuth(1 = 시스템관리자, 2 = 각사관리자, 3 = 일반사용자)
		if (userAuth.equals("1") || userAuth.equals("2") || userAuth.equals("3")) {
            sbWhere.append(" and interrelated_cust_code = :interrelatedCustCode ");
        }
		
		//계열사 조건
		//userAuth(4 = 감사사용자)
		if (userAuth.equals("4")) {

            sbWhere.append(" and (");
            
            sbWhere.append(" interrelated_cust_code = " + interrelatedCode );
            
            //감사사용자에 해당하는 계열사 조건 추가
            for (int i = 0; i < custCodes.size(); i++) {
            	sbWhere.append(" or ");
                sbWhere.append(" interrelated_cust_code = :custCode").append(i);
            }
            
            sbWhere.append(")");

           
        }
		
		//관계자 조건
		sbWhere.append(" and ( create_user = :userid " +
                       	  	 " or open_att1 = :userid " +
                       	  	 " or open_att2 = :userid " +
                       	  	 " or gongo_id = :userid " +
                       	  	 " or est_bidder = :userid " +
                       	  	 " or est_opener = :userid"
                         + " ) ");
		
		sbCnt.append(sbWhere);
		
		Query queryCnt = entityManager.createNativeQuery(sbCnt.toString());
		
		//계열사 조건 set
		if (userAuth.equals("1") || userAuth.equals("2") || userAuth.equals("3")) {
			queryCnt.setParameter("interrelatedCustCode", interrelatedCode);
        }
		
        if (userAuth.equals("4")) {

            for (int i = 0; i < custCodes.size(); i++) {
            	queryCnt.setParameter("custCode" + i, custCodes.get(i));
            }
        }
        
        //관계자 조건 set
        queryCnt.setParameter("userid", userId);
		
	    Object[] result = (Object[]) queryCnt.getSingleResult();

	    bidCntDto.setPlanning((BigInteger) result[0]);
	    bidCntDto.setNoticing((BigInteger) result[1]);
	    bidCntDto.setBeforeOpening((BigInteger) result[2]);
	    bidCntDto.setOpening((BigInteger) result[3]);
	    bidCntDto.setCompleted((BigInteger) result[4]);
	    bidCntDto.setUnsuccessful((BigInteger) result[5]);
	    bidCntDto.setIng((BigInteger) result[6]);
	    
	    // 진행 중
	    BigInteger ing = ((BigInteger) result[1]).add((BigInteger) result[2]).add((BigInteger) result[3]);
	    bidCntDto.setIng(ing);

		
		return bidCntDto;
	}

	//협력사 업채수 조회(계열사메인)
	@Transactional
	public PartnerCntDto selectPartnerCnt(Map<String, Object> params) {
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String interrelatedCode = "";
		PartnerCntDto partnerCntDto = new PartnerCntDto();
		
		if (userOptional.isPresent()) {
        
    		interrelatedCode = userOptional.get().getInterrelatedCustCode();
  
        }

		StringBuilder sbCnt = new StringBuilder(" select COUNT(CASE WHEN tccm.cert_yn = 'N' THEN 1 END) as 'request', "
													 + " COUNT(CASE WHEN tccm.cert_yn = 'Y' THEN 1 END) as 'approval', "
													 + " COUNT(CASE WHEN tccm.cert_yn = 'D' THEN 1 END) as 'deletion' "
											  + " from t_co_cust_master tccm "
											  + " inner join t_co_cust_ir tcci "
											  + " on tccm.cust_code = tcci.cust_code "
											  + " where 1=1 "
											   );
		
		StringBuilder sbWhere = new StringBuilder();
	
		//계열사 조건
		sbWhere.append(" and tcci.interrelated_cust_code = :interrelatedCustCode ");
		
		sbCnt.append(sbWhere);
		
		Query queryCnt = entityManager.createNativeQuery(sbCnt.toString());
		
		//계열사 조건 set
		queryCnt.setParameter("interrelatedCustCode", interrelatedCode);
		
	    Object[] result = (Object[]) queryCnt.getSingleResult();
	    
	    partnerCntDto.setRequest((BigInteger) result[0]);
	    partnerCntDto.setApproval((BigInteger) result[1]);
	    partnerCntDto.setDeletion((BigInteger) result[2]);
	    
		return partnerCntDto;
	}

	//협력사 전자입찰 건수 조회(협력사메인)
	@Transactional
	public PartnerBidCntDto selectPartnerBidCnt(Map<String, Object> params) {

		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoCustUser> userOptional = tCoUserCustRepository.findById(principal.getUsername());
		int custCode = -1;
		TCoCustUser tCoCustUser = null;
		
		if(userOptional.isPresent()) {
			tCoCustUser = userOptional.get();
			custCode = tCoCustUser.getCustCode();
		}

		PartnerBidCntDto partnerBidCntDto = new PartnerBidCntDto();

		StringBuilder sbCnt = new StringBuilder(" select (select count(1) as cnt "
													+ "from t_bi_info_mat tbim "
													+ "inner join t_co_cust_ir tcci "
													+ "on tbim.INTERRELATED_CUST_CODE = tcci.INTERRELATED_CUST_CODE "
													+ "and tcci.CUST_CODE = :custCode "
													+ "left outer join t_bi_info_mat_cust tbimc "
													+ "on tbim.BI_NO = tbimc.BI_NO "
													+ "and tbimc.CUST_CODE = :custCode "
													+ "where 1=1 "
													+ "and ((tbim.BI_MODE = 'A' AND tbimc.CUST_CODE = :custCode) OR tbim.BI_MODE = 'B') "
													+ "and tbim.ING_TAG in ('A1', 'A2', 'A3') "
													+ "and (tbimc.ESMT_YN is null "
													+ "or tbimc.ESMT_YN in ('0', '1'))"
													 + ") as noticing, "//미투찰(재입찰 포함)

												     + "(select count(1) from t_bi_info_mat tbim inner join t_co_cust_ir tcci "
												     + "on tbim.INTERRELATED_CUST_CODE = tcci.INTERRELATED_CUST_CODE "
												     + "and tcci.CUST_CODE = :custCode "
												     + "left outer join t_bi_info_mat_cust tbimc "
												     + "on tbim.BI_NO = tbimc.BI_NO "
												     + "and tbimc.CUST_CODE = :custCode "
												     + "left outer join t_co_user tcu "
												     + "on tbim.CREATE_USER = tcu.USER_ID "
												     + "where 1 = 1 "
												     + "and tbim.BI_MODE in ('A', 'B') "
												     + "and tbim.ING_TAG in ( 'A1', 'A2', 'A3' ) "
												     + "and tbimc.ESMT_YN in ('2')"
												     + ") as submitted, "//투찰한 입찰
												     
//												     + " (select 0) as confirmation, "//재입찰
												     
												     + " (select COUNT(1) from t_bi_info_mat tbim inner join t_bi_info_mat_cust tbimc on tbim.bi_no = tbimc.bi_no where tbim.ing_tag = 'A5' and tbimc.succ_yn = 'Y' and tbimc.cust_code = :custCode and tbim.update_date >= CURDATE() - INTERVAL 12 month) AS awarded, "//낙찰(12개월)
												     + " (select COUNT(1) from t_bi_info_mat tbim inner join t_bi_info_mat_cust tbimc on tbim.bi_no = tbimc.bi_no where tbim.ing_tag IN ('A5', 'A7') and tbimc.cust_code = :custCode and tbimc.succ_yn = 'N' and tbim.update_date >= CURDATE() - INTERVAL 12 month) AS unsuccessful "//비선정(12개월)
											   );
		
		Query queryCnt = entityManager.createNativeQuery(sbCnt.toString());

		queryCnt.setParameter("custCode", custCode);
	    Object[] result = (Object[]) queryCnt.getSingleResult();
	    
	    partnerBidCntDto.setNoticing((BigInteger) result[0]);
	    partnerBidCntDto.setSubmitted((BigInteger) result[1]);
	    partnerBidCntDto.setConfirmation(BigInteger.ZERO);
	    partnerBidCntDto.setAwarded((BigInteger) result[2]);
	    partnerBidCntDto.setUnsuccessful((BigInteger) result[3]);
	    
	    BigInteger ing = (partnerBidCntDto.getNoticing().add(partnerBidCntDto.getSubmitted()));
	    partnerBidCntDto.setIng(ing);

	    return partnerBidCntDto;
	}

	//입찰완료 조회(협력사메인)
	@Transactional
	public PartnerCompletedBidCntDto selectCompletedBidCnt(Map<String, Object> params) {
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoCustUser> userOptional = tCoUserCustRepository.findById(principal.getUsername());
		int custCode = -1;
		TCoCustUser tCoCustUser = null;
		
		if(userOptional.isPresent()) {
			tCoCustUser = userOptional.get();
			custCode = tCoCustUser.getCustCode();
		}
		
		PartnerCompletedBidCntDto partnerCompletedBidCntDto = new PartnerCompletedBidCntDto();
		
		StringBuilder sbCnt = new StringBuilder(" select ( "
														+ "(select count(1) "
														+ "from t_bi_info_mat tbim "
														+ "left outer join t_bi_info_mat_cust tbimc "
														+ "on tbim.BI_NO = tbimc.BI_NO "
														+ "and tbimc.CUST_CODE = :custCode "
														+ "where tbim.ING_TAG in ('A5', 'A7') "
														+ "and  (tbim.UPDATE_DATE >= CURDATE() - interval 12 month) "
														+ "and (tbim.BI_MODE = 'B' OR (tbim.BI_MODE = 'A' AND tbimc.CUST_CODE = :custCode))"
														+ ")"
													+ ") as posted, "//공고되었던 입찰
													+ " (select COUNT(1) from t_bi_info_mat tbim inner join t_bi_info_mat_cust tbimc on(tbim.bi_no = tbimc.bi_no) where tbim.ing_tag IN ('A5', 'A7') and tbimc.ESMT_YN IN('2' , '3') and (tbim.update_date >= CURDATE() - INTERVAL 12 MONTH) and tbimc.cust_code = :custCode) as submitted, "//투찰했던 입찰
													+ " (select COUNT(1) from t_bi_info_mat tbim inner join t_bi_info_mat_cust tbimc on(tbim.bi_no = tbimc.bi_no) where tbim.ing_tag IN ('A5') and (tbim.update_date >= CURDATE() - INTERVAL 12 MONTH) and tbimc.succ_yn = 'Y' and tbimc.cust_code = :custCode) as awarded "//낙찰했던 입찰
											   );
		Query queryCnt = entityManager.createNativeQuery(sbCnt.toString());

		queryCnt.setParameter("custCode", custCode);
		
		Object[] result = (Object[]) queryCnt.getSingleResult();
		
		partnerCompletedBidCntDto.setPosted((BigInteger) result[0]);
		partnerCompletedBidCntDto.setSubmitted((BigInteger) result[1]);
		partnerCompletedBidCntDto.setAwarded((BigInteger) result[2]);
		
		return partnerCompletedBidCntDto;
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
				
					String userTel = userOptional2.get().getUserHp();
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
	public ResultBody chkPwChangeEncourage(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		resultBody.setData(false);
		
		LocalDateTime currentDate = LocalDateTime.now();	//현재시간
		LocalDateTime pwChangeDate = null;					//비밀번호 변경일
		
		String userId = CommonUtils.getString(params.get("userId"));
		Boolean isGroup = (Boolean) params.get("isGroup");
		
		if(isGroup) {
			Optional<TCoUser> userOptional = tCoUserRepository.findById(userId);
	
			if (userOptional.isPresent()) {//계열사인 경우
				pwChangeDate = userOptional.get().getPwdEditDate();
			}
		}else {
			Optional<TCoCustUser> userOptional = tCoUserCustRepository.findById(userId);
			
			if (userOptional.isPresent()) {//계열사인 경우
				pwChangeDate = userOptional.get().getPwdChgDate();
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
	

}
