package iljin.framework.ebid.etc.util.common.schedule.service;

import iljin.framework.ebid.bid.service.BidProgressService;
import iljin.framework.ebid.bid.service.BidStatusService;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.common.schedule.entity.MailEntity;
import iljin.framework.ebid.etc.util.common.schedule.repository.ScheduleRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final CommonUtils commonUtils;
    private final BidStatusService bidStatusService;
    private final BidProgressService bidProgressService;


    @Transactional
    public void deleteBidPlan() throws Exception {
        //삭제대상 BiNo 조회
        List<String> biNoList = scheduleRepository.selectByDeleteByIngTag();

        //입찰 계획 삭제
        scheduleRepository.deleteByIngTag();

        //로그 입력
        for(int i = 0; i < 1; i++) {
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
                String[] toEmailAddrArray = mailEntity.getReceives().split(";");
                String mailTitle = mailEntity.getTitle();
                String mailContents = mailEntity.getConts();

               commonUtils.sendEmail(toEmailAddrArray, mailTitle, mailContents);

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




}
