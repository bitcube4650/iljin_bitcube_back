package iljin.framework.ebid.etc.main.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import iljin.framework.ebid.bid.dto.InterUserInfoDto;
import iljin.framework.ebid.bid.service.BidProgressService;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.main.dto.BidCntDto;
import iljin.framework.ebid.etc.main.dto.PartnerBidCntDto;
import iljin.framework.ebid.etc.main.dto.PartnerCntDto;
import iljin.framework.ebid.etc.main.dto.PartnerCompletedBidCntDto;

@Service
public class MainService {
	
	@Autowired
    private TCoUserRepository tCoUserRepository;
	
	@Autowired
    private BidProgressService bidProgressService;
	
	@PersistenceContext
    private EntityManager entityManager;

	//전자입찰 건수 조회(계열사메인)
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
												      + " COUNT(CASE WHEN ing_tag IN ('A1', 'A3') THEN 1 END) AS noticing, "
												      + " COUNT(CASE WHEN ing_tag IN ('A1', 'A3') AND est_close_date < SYSDATE() THEN 1 END) AS beforeOpening, "
												      + " COUNT(CASE WHEN ing_tag = 'A2' THEN 1 END) AS opening, "
												      + " COUNT(CASE WHEN ing_tag = 'A5' AND update_date >= CURDATE() - INTERVAL 12 MONTH THEN 1 END) AS completed, "
												      + " COUNT(CASE WHEN ing_tag = 'A7' AND update_date >= CURDATE() - INTERVAL 12 MONTH THEN 1 END) AS unsuccessful "
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

		
		return bidCntDto;
	}

	//협력사 업채수 조회(계열사메인)
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
											  + " from t_co_cust_ir tcci "
											  + " inner join t_co_cust_master tccm "
											  + " on tcci.cust_code = tccm.cust_code "
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
	public PartnerBidCntDto selectPartnerBidCnt(Map<String, Object> params) {

		
		PartnerBidCntDto partnerBidCntDto = new PartnerBidCntDto();
/*
		StringBuilder sbCnt = new StringBuilder(" select (select COUNT(1) from t_bi_info_mat tbim where tbim.ING_TAG IN ('A1')) AS noticing, "
												     + " (select COUNT(1) from t_bi_info_mat tbim inner join t_bi_info_mat_cust tbimc on tbim.BI_NO = tbimc.BI_NO where tbim.ING_TAG IN ('A1', 'A2', 'A3', 'A7') and tbimc.CUST_CODE = :custCode) AS count2, "
												     + " (select COUNT(1) from t_bi_info_mat tbim inner join t_bi_info_mat_cust tbimc on tbim.BI_NO = tbimc.BI_NO where tbim.ING_TAG IN ('A5') and tbimc.CUST_CODE = :custCode) AS count3, "
												     + " (select COUNT(1) from t_bi_info_mat tbim inner join t_bi_info_mat_cust tbimc on tbim.BI_NO = tbimc.BI_NO where tbim.ING_TAG IN ('A5') and tbimc.SUCC_YN = 'Y' and (tbimc.UPDATE_DATE >= CURDATE() - INTERVAL 12 MONTH ) and tbimc.CUST_CODE = :custCode) AS count4, "
												     + " (select COUNT(1) from t_bi_info_mat tbim inner join t_bi_info_mat_cust tbimc on tbim.BI_NO = tbimc.BI_NO where tbim.ING_TAG IN ('A7') and (tbim.UPDATE_DATE >= CURDATE() - INTERVAL 12 month) and tbimc.CUST_CODE = :custCode) AS count5 "
											   );
		
		Query queryCnt = entityManager.createNativeQuery(sbCnt.toString());

		if(!StringUtils.isEmpty(params.get("custCode"))) {

			queryCnt.setParameter("custCode", params.get("custCode"));
		}
	    Object[] result = (Object[]) queryCnt.getSingleResult();
	    
	    partnerBidCntDto.setNoticing((BigInteger) result[0]);
	    partnerBidCntDto.setSubmitted((BigInteger) result[1]);
	    partnerBidCntDto.setConfirmation((BigInteger) result[2]);
	    partnerBidCntDto.setAwarded((BigInteger) result[3]);
	    partnerBidCntDto.setUnsuccessful((BigInteger) result[4]);
*/
	    return partnerBidCntDto;
	}

	//입찰완료 조회(협력사메인)
	public PartnerCompletedBidCntDto selectCompletedBidCnt(Map<String, Object> params) {
		
		PartnerCompletedBidCntDto partnerCompletedBidCntDto = new PartnerCompletedBidCntDto();
		/*
		StringBuilder sbCnt = new StringBuilder(" select (select COUNT(1) from t_bi_info_mat tbim where tbim.ING_TAG IN ('A5') and (tbim.UPDATE_DATE >= CURDATE() - INTERVAL 12 MONTH)) AS posted, "
													 + " (select COUNT(1) from t_bi_info_mat tbim inner join t_bi_info_mat_cust tbimc on(tbim.BI_NO = tbimc.BI_NO) where tbim.ING_TAG IN ('A5') and (tbim.UPDATE_DATE >= CURDATE() - INTERVAL 12 MONTH) and tbimc.CUST_CODE = :custCode) AS submitted, "
													 + " (select COUNT(1) from t_bi_info_mat tbim inner join t_bi_info_mat_cust tbimc on(tbim.BI_NO = tbimc.BI_NO) where tbim.ING_TAG IN ('A5') and (tbim.UPDATE_DATE >= CURDATE() - INTERVAL 12 MONTH) and tbimc.SUCC_YN = 'Y' and tbimc.CUST_CODE = :custCode) AS awarded "
											   );
		Query queryCnt = entityManager.createNativeQuery(sbCnt.toString());

		if(!StringUtils.isEmpty(params.get("custCode"))) {

			queryCnt.setParameter("custCode", params.get("custCode"));
		}
		Object[] result = (Object[]) queryCnt.getSingleResult();
		
		partnerCompletedBidCntDto.setPosted((BigInteger) result[0]);
		partnerCompletedBidCntDto.setSubmitted((BigInteger) result[1]);
		partnerCompletedBidCntDto.setAwarded((BigInteger) result[2]);
		*/
		return partnerCompletedBidCntDto;
	}

}
