package iljin.framework.ebid.etc.util.common.schedule.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import iljin.framework.ebid.bid.service.BidProgressService;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.GeneralDao;
import iljin.framework.ebid.etc.util.common.message.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {
	private final BidProgressService bidProgressService;

	@Autowired
	private GeneralDao generalDao;
	
	@Autowired
	private MessageService messageService;

	@PersistenceContext
	private EntityManager entityManager;
	
	/**
	 * 공고되지 않은 입찰 계획 삭제
	 * @throws Exception
	 */
	@Transactional
	public void deleteBidPlan() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("msg", "[본사] 자동 입찰계획 삭제");
		generalDao.updateGernal("schedule.updateEbidStatusDel", map);
		generalDao.insertGernal("schedule.insertTBiLog", map);
	}

	/**
	 * 진행중 입찰에서 30일이 지나도 낙찰처리가 안된 입찰은 유찰처리
	 * @throws Exception
	 */
	@Transactional
	public void updateIngTagForLast30Days() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("msg", "[본사] 자동유찰");
		generalDao.updateGernal("schedule.updateIngTagForLast30Days", map);
		generalDao.insertGernal("schedule.insertTBiInfoMatHist", map);
		generalDao.insertGernal("schedule.insertTBiLog", map);
		
	}
	
	/**
	 * 이메일발송 
	 * @throws Exception
	 */
	@Transactional
	@SuppressWarnings({ "unchecked" })
	public void emailSendExe() throws Exception{
		List<Object> mailInfoList = generalDao.selectGernalList("schedule.selectSendMailList", null);
		
		for(Object obj : mailInfoList) {
			Map<String, Object> map = (Map<String, Object>) obj;
			
			try {
				String fromMail = CommonUtils.getString(map.get("fromMail"), "");
				String[] toEmailAddrArray = CommonUtils.getString(map.get("receives")).split(";");
				String mailTitle = CommonUtils.getString(map.get("title"));
				String mailContents = CommonUtils.getString(map.get("conts"));

				CommonUtils.sendEmail(fromMail, toEmailAddrArray, mailTitle, mailContents);

				map.put("sendFlag","1");
				map.put("errMsg", "");
			} catch (Exception e) {
				map.put("sendFlag","9");
				map.put("errMsg", e.getMessage());
			}
			
			generalDao.updateGernal("schedule.updateEmailSendResponse", map);
		}
	}

	/**
	 * 지명입찰 제출마감시간 48시간 ~ 24시간 전 미투찰 협력사에게 메일, 문자 발송
	 * @throws Exception
	 */
	@Transactional
	@SuppressWarnings({ "unchecked" })
	public void ebidCloseSendCustAlarm() throws Exception{
		
		try {
			List<Object> list = generalDao.selectGernalList("schedule.ebidCloseSendCustAlarm", null);
			
			for(Object obj : list) {
				Map<String, Object> map = (Map<String, Object>) obj;
				String biNo = CommonUtils.getString(map.get("biNo"));
				String biName = CommonUtils.getString(map.get("biName"));
				String estStartDate = CommonUtils.getString(map.get("estStartDate"));
				String estCloseDate = CommonUtils.getString(map.get("estCloseDate"));
				String interNm = CommonUtils.getString(map.get("interrelatedNm"));
			
				List<Object> sendList = generalDao.selectGernalList("schedule.ebidCloseSendCustReceiverList", map);
				
				if(sendList.size() != 0) {
					Map<String, Object> emailParam = new HashMap<String, Object>();
					emailParam.put("type", "push");
					emailParam.put("biName", biName);
					emailParam.put("estStartDate", estStartDate);
					emailParam.put("estCloseDate", estCloseDate);
					emailParam.put("interNm", interNm);
					emailParam.put("sendList", sendList);
					emailParam.put("biNo", biNo);
					
					bidProgressService.updateEmail(emailParam);
					
					//문자
					for(Object obj2 : sendList) {
						Map<String, Object> map2 = (Map<String, Object>) obj2;
						messageService.send("일진그룹", CommonUtils.getString(map2.get("userHp")), CommonUtils.getString(map2.get("userName")), "[일진그룹 전자입찰시스템] 입찰("+biNo+") 마감시간이 다가오고 있습니다.\r\n확인바랍니다.", biNo);
					}
					
				}
			}
			
		}catch(Exception e) {
			log.error("ebidCloseSendCustAlarm sendMsg error : {}", e);
		}
	}

}
