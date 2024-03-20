package iljin.framework.ebid.etc.util.common.schedule.service;

import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.common.schedule.entity.MailEntity;
import iljin.framework.ebid.etc.util.common.schedule.repository.ScheduleRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final CommonUtils commonUtils;


    @Transactional
    public void deleteBidPlan() throws Exception {
        scheduleRepository.deleteByIngTag();
    }

    @Transactional
    public void updateIngTagForLast30Days() throws Exception {
        scheduleRepository.updateIngTagForLast30Days();
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
