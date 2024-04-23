package iljin.framework.ebid.etc.util.common.schedule.service;

import iljin.framework.ebid.bid.dto.BidProgressDto;
import iljin.framework.ebid.bid.dto.SendDto;
import iljin.framework.ebid.bid.service.BidProgressService;
import iljin.framework.ebid.bid.service.BidStatusService;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.common.message.MessageService;
import iljin.framework.ebid.etc.util.common.schedule.entity.MailEntity;
import iljin.framework.ebid.etc.util.common.schedule.repository.ScheduleRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final CommonUtils commonUtils;
    private final BidStatusService bidStatusService;
    private final BidProgressService bidProgressService;


    @Autowired
    private MessageService messageService;

    @PersistenceContext
    private EntityManager entityManager;
    
    @Transactional
    public void deleteBidPlan() throws Exception {
        //삭제대상 BiNo 조회
        List<String> biNoList = scheduleRepository.selectByDeleteByIngTag();

        //입찰 계획 삭제
        scheduleRepository.deleteByIngTag();

        //로그 입력
        for(int i = 0; i < biNoList.size(); i++) {
            //로그입력
            Map<String, String> logParams = new HashMap<>();
            logParams.put("msg", "[본사] 자동 입찰계획 삭제");
            logParams.put("biNo", biNoList.get(i));
            logParams.put("userId", "System");

            //Log update
            bidProgressService.updateLog(logParams);
        }
    }

    @Transactional
    public void updateIngTagForLast30Days() throws Exception {
        //유찰 처리 대상 BiNo 조회
        List<String> biNoList = scheduleRepository.selectBiNoForLast30Days();

        //유찰 처리
        scheduleRepository.updateIngTagForLast30Days();

        //입찰 이력
        for(int i = 0; i < biNoList.size(); i++) {
            //입찰 이력 Insert
            bidStatusService.bidHist(biNoList.get(i));

            //로그입력
            Map<String, String> logParams = new HashMap<>();
            logParams.put("msg", "[본사] 자동유찰");
            logParams.put("biNo", biNoList.get(i));
            logParams.put("userId", "System");

            //Log update
            bidProgressService.updateLog(logParams);
        }
    }

    @Transactional
    public void emailSendExe() {
        List<MailEntity> mailInfoList = scheduleRepository.findAllMailInfo();

        for(MailEntity mailEntity : mailInfoList) {
            UpdateEmailInfoDto updateEmailInfoDto = new UpdateEmailInfoDto();

            try {
                String fromMail = CommonUtils.getString(mailEntity.getFromMail(), "");
                String[] toEmailAddrArray = mailEntity.getReceives().split(";");
                String mailTitle = mailEntity.getTitle();
                String mailContents = mailEntity.getConts();

                if(!fromMail.isEmpty()) {
                    commonUtils.sendEmail(fromMail, toEmailAddrArray, mailTitle, mailContents);
                } else {
                    commonUtils.sendEmail(toEmailAddrArray, mailTitle, mailContents);
                }

                updateEmailInfoDto.setSendFlag("1");
                updateEmailInfoDto.setErrMsg("");
            } catch (Exception e) {
                updateEmailInfoDto.setSendFlag("9");
                updateEmailInfoDto.setErrMsg(e.getMessage());
            }
            updateEmailInfo(mailEntity.getMailId(), updateEmailInfoDto.getSendFlag(), updateEmailInfoDto.getErrMsg());
        }
    }

    @Transactional
    public void updateEmailInfo(String MailId, String sendFlag, String errMsg) {
        MailEntity mailEntity = scheduleRepository.findOneMailInfo(MailId);
        mailEntity.setSendFlag(sendFlag);
        mailEntity.setErrorMsg(errMsg);
    }

    @Data
    private static class UpdateEmailInfoDto {
        private String sendFlag;
        private String errMsg;

    }

	/**
	 * 지명입찰 제출마감시간 48시간 ~ 24시간 전 미투찰 협력사에게 메일, 문자 발송
	 * @throws Exception
	 */
	@Transactional
	public void ebidCloseSendCustAlarm() throws Exception{
		
		try {
			
			StringBuilder sbBid = new StringBuilder(
				  "select	tbim.BI_NO "
				+ ",		tbim.BI_NAME "
				+ ",		DATE_FORMAT(tbim.est_start_date, '%Y-%m-%d %H:%i') AS est_start_date "
				+ ",		DATE_FORMAT(tbim.est_close_date, '%Y-%m-%d %H:%i') AS est_close_date "
				+ ",		tci.INTERRELATED_NM "
				+ "FROM t_bi_info_mat tbim "
				+ "INNER JOIN t_co_interrelated tci "
				+ "	on tbim.INTERRELATED_CUST_CODE = tci.INTERRELATED_CUST_CODE "
				+ "where (now() > date_sub(tbim.est_close_date, interval 2 day) "
				+ "and now() < date_sub(tbim.est_close_date, interval 1 day)) "
				+ "and tbim.ing_tag in ('A1', 'A3') "
			);
			
			//쿼리 실행
			Query queryBid = entityManager.createNativeQuery(sbBid.toString());
			List<BidProgressDto> list = new JpaResultMapper().list(queryBid, BidProgressDto.class);
			
			for(BidProgressDto bidDto : list) {
				
				String biNo = bidDto.getBiNo();
				String biName = bidDto.getBiName();
				String estStartDate = bidDto.getEstStartDate();
				String estCloseDate = bidDto.getEstCloseDate();
				String interNm = bidDto.getInterrelatedNm();
			
				StringBuilder sbMail = new StringBuilder(
					  "select	tccu.USER_EMAIL  "
					+ ",		a.from_email "
					+ ",		REGEXP_REPLACE(tccu.USER_HP , '[^0-9]+', '') as USER_HP "
					+ ",		tccu.USER_NAME "
					+ "from "
					+ "( "
					+ "	select	jb.datas as user_id "
					+ "	,		tcu.user_email as from_email "
					+ "	from t_bi_info_mat_cust tbimc "
					+ "	inner join json_table( "
					+ "		replace(json_array(tbimc.USEMAIL_ID), ',', '\",\"'), "
					+ "		'$[*]' columns (datas varchar(50) path '$') "
					+ "	) jb "
					+ "	inner join t_bi_info_mat tbim "
					+ "		on tbimc.bi_no = tbim.bi_no "
					+ "	left outer join t_co_user tcu "
					+ "		on tbim.create_user = tcu.user_id "
					+ "	where tbimc.bi_no = :biNo "
					+ "	and tbimc.esmt_yn NOT IN ( '2', '3' ) "
					+ ") a "
					+ "inner join t_co_cust_user tccu "
					+ "	on a.user_id = tccu.user_id "
					+ "	and tccu.USE_YN = 'Y' "
					+ "group by tccu.USER_EMAIL "	
				);
			
				
				//쿼리 실행
				Query queryMail = entityManager.createNativeQuery(sbMail.toString());
				//조건 대입
				queryMail.setParameter("biNo", biNo);
				List<SendDto> sendList = new JpaResultMapper().list(queryMail, SendDto.class);
				
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
					for(SendDto dto : sendList) {
						messageService.send("일진그룹", dto.getUserHp(), dto.getUserName(), "[일진그룹 전자입찰시스템] 입찰("+biNo+") 마감시간이 다가오고 있습니다.\r\n확인바랍니다.", biNo);
					}
					
				}
			}
			
		}catch(Exception e) {
			log.error("ebidCloseSendCustAlarm sendMsg error : {}", e);
		}
	}



}
