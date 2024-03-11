package iljin.framework.ebid.etc.util.common.schedule.controller;

import iljin.framework.ebid.etc.util.common.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    @Value("${common.isReal.server}")
    private boolean isRealServer;

    //공고되지 않은 입찰 계획 삭제 : 0시 1분
    @Scheduled(cron="0 1 0 * * *")	//초 분 시 일 월 주(년)
    public void deleteBidPlan() {
        if(isRealServer) {
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
    public void changeBidStatus() {
        if(isRealServer) {
            log.info("--------------------------Scheduler changeBidStatus() method start!--------------------------");
            try {
                scheduleService.updateIngTagForLast30Days();
            } catch(Exception e) {
                log.error("changeBidStatus Exception : "+e);
            }
            log.info("--------------------------Scheduler changeBidStatus() method end!----------------------------");
        }
    }
}
