package iljin.framework.ebid.etc.util.common.schedule.controller;

import iljin.framework.ebid.etc.util.Constances;
import iljin.framework.ebid.etc.util.common.mail.service.MailService;
import iljin.framework.ebid.etc.util.common.schedule.service.ScheduleService;
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
}
