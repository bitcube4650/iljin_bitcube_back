package iljin.framework.ebid.etc.util.common.schedule.controller;

import iljin.framework.ebid.etc.util.Constances;
import iljin.framework.ebid.etc.util.common.mail.service.MailService;
import iljin.framework.ebid.etc.util.common.schedule.service.ScheduleService;
import iljin.framework.ebid.etc.util.common.schedule.service.ScheduleUserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;
    private final MailService mailService;
    private final ScheduleUserInfoService scheduleUserInfoSvc;

    @PostConstruct
    public void init() {
   //     emailSendExe();
    }

    //공고되지 않은 입찰 계획 삭제 : 0시 1분
    //@Scheduled(fixedRate = Long.MAX_VALUE)
    @Scheduled(cron="0 1 0 * * *")	//초 분 시 일 월 주(년)
    public void deleteBidPlan() {
        if(Constances.COMMON_SCHEDULE_FLAG) {
            log.info("--------------------------Scheduler deleteBidPlan() method start!--------------------------");
            try {
               scheduleService.deleteBidPlan();
            } catch(Exception e) {
                log.error("deleteEbidPlan Exception : "+e);
            }
            log.info("--------------------------Scheduler deleteBidPlan() method end!----------------------------");
        }
    }

    //진행중 입찰에서 30일이 지나도 낙찰처리가 안된 입찰은 유찰처리 : 0시 11분
    @Scheduled(cron="0 11 0 * * *")	//초 분 시 일 월 주(년)
    //@Scheduled(fixedRate = Long.MAX_VALUE)
    public void updateIngTagForLast30Days() {
        if(Constances.COMMON_SCHEDULE_FLAG) {
            log.info("--------------------------Scheduler updateIngTagForLast30Days() method start!-----------------");
            try {
                scheduleService.updateIngTagForLast30Days();
            } catch(Exception e) {
                log.error("updateIngTagForLast30Days Exception : "+e);
            }
            log.info("--------------------------Scheduler updateIngTagForLast30Days() method end!-------------------");
        }
    }


    //5분마다 이메일 발송
    //이메일발송 5분마다
   @Scheduled(cron="0 3,8,13,18,23,28,33,38,43,48,53,58 * * * *")	//초 분 시 일 월 주(년)
    //@Scheduled(fixedRate = Long.MAX_VALUE)
    public void emailSendExe() {
        if(Constances.COMMON_SCHEDULE_FLAG) {
            log.info("--------------------------Scheduler emailSendExe() method start!------------------------------");
            try {
                scheduleService.emailSendExe();
            }
            catch(Exception e) {
                log.error("emailSendExe Exception : " + e);
            }
            log.info("--------------------------Scheduler emailSendExe() method end!--------------------------------");
        }
    }
   
	/**
	 * 사용자 업데이트
	 */
	@Scheduled(cron="0 0 2 * * *")	//초 분 시 일 월 주(년)
	public void updateUserUseYn() {
		if(Constances.COMMON_SCHEDULE_FLAG) {
			log.info("--------------------------Scheduler updateUserUseYn() method start!------------------------------");
			try {
				scheduleUserInfoSvc.updateUserUseYn();
			}
			catch(Exception e) {
				log.error("updateUserUseYn Exception : " + e);
			}
			log.info("--------------------------Scheduler updateUserUseYn() method end!--------------------------------");
		}
	}
	
	/**
	 * 지명입찰 제출마감시간 48시간 ~ 24시간 전 미투찰 협력사에게 메일, 문자 발송
	 */
	@Scheduled(cron="0 0 10 * * *")	//초 분 시 일 월 주(년)
	public void ebidCloseSendCustAlarm() {
		if(Constances.COMMON_SCHEDULE_FLAG) {
			log.info("--------------------------Scheduler ebidCloseSendCustAlarm() method start!------------------------------");
			try {
				scheduleService.ebidCloseSendCustAlarm();
			}
			catch(Exception e) {
				log.error("ebidCloseSendCustAlarm Exception : " + e);
			}
			log.info("--------------------------Scheduler ebidCloseSendCustAlarm() method end!--------------------------------");
		}
	}
}
