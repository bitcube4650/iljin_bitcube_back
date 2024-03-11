package iljin.framework.ebid.etc.main.service;

import java.math.BigInteger;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Service;

import iljin.framework.ebid.etc.main.dto.BidCntDto;
import iljin.framework.ebid.etc.main.dto.PartnerCntDto;

@Service
public class MainService {
	
	@PersistenceContext
    private EntityManager entityManager;

	//전자입찰 건수 조회
	public BidCntDto selectBidCnt(Map<String, Object> params) {
		BidCntDto bidCntDto = new BidCntDto();
		
		StringBuilder sbCnt = new StringBuilder(" SELECT COUNT(CASE WHEN tbim.ING_TAG = 'A0' THEN 1 END) AS 'planning', "
											         + " COUNT(CASE WHEN tbim.ING_TAG = 'A1' THEN 1 END) AS 'progress', "
											         + " COUNT(CASE WHEN tbim.ING_TAG = 'A1' and tbim.est_close_date < NOW() THEN 1 END) AS 'beforeOpening', "
											         + " COUNT(CASE WHEN tbim.ING_TAG = 'A3' and tbim.est_close_date < now() THEN 1 END) AS 'beforeReopening', "
											         + " COUNT(CASE WHEN tbim.ING_TAG = 'A2' THEN 1 END) AS 'opening', "
											         + " COUNT(CASE WHEN tbim.ING_TAG = 'A3' THEN 1 END) AS 'rebid', "
											         + " COUNT(CASE WHEN tbim.ING_TAG = 'A5' AND tbim.update_date >= CURDATE() - INTERVAL 12 MONTH THEN 1 END) AS 'completed', "
											         + " COUNT(CASE WHEN tbim.ING_TAG = 'A7' AND tbim.update_date >= CURDATE() - INTERVAL 12 MONTH THEN 1 END) AS 'unsuccessful' "
											  + " FROM t_bi_info_mat tbim ");
		
		Query queryCnt = entityManager.createNativeQuery(sbCnt.toString());

	    Object[] result = (Object[]) queryCnt.getSingleResult();

	    bidCntDto.setPlanning((BigInteger) result[0]);
		bidCntDto.setProgress((BigInteger) result[1]);
		bidCntDto.setBeforeOpening((BigInteger) result[2]);
		bidCntDto.setBeforeReopening((BigInteger) result[3]);
		bidCntDto.setOpening((BigInteger) result[4]);
		bidCntDto.setRebid((BigInteger) result[5]);
		bidCntDto.setCompleted((BigInteger) result[6]);
		bidCntDto.setUnsuccessful((BigInteger) result[7]);

		
		return bidCntDto;
	}

	//협력사 업채수 조회
	public PartnerCntDto selectPartnerCnt(Map<String, Object> params) {
		
		PartnerCntDto partnerCntDto = new PartnerCntDto();
		
		StringBuilder sbCnt = new StringBuilder(" select COUNT(CASE WHEN tccm.CERT_YN = 'N' THEN 1 END) as 'request', "
													 + " COUNT(CASE WHEN tccm.CERT_YN = 'Y' THEN 1 END) as 'approval', "
													 + " COUNT(CASE WHEN tccm.CERT_YN = 'D' THEN 1 END) as 'deletion' "
											  + " from t_co_cust_master tccm ");
		
		Query queryCnt = entityManager.createNativeQuery(sbCnt.toString());

	    Object[] result = (Object[]) queryCnt.getSingleResult();
	    
	    partnerCntDto.setRequest((BigInteger) result[0]);
	    partnerCntDto.setApproval((BigInteger) result[1]);
	    partnerCntDto.setDeletion((BigInteger) result[2]);
	    
		return partnerCntDto;
	}

}
