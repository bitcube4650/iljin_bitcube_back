package iljin.framework.ebid.etc.util.common.mail.service;

import iljin.framework.ebid.etc.util.common.mail.repository.MailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {
    private final MailRepository mailRepository;

    @Transactional
    public void saveMailInfo(String title, String content, String userMail) {
        mailRepository.saveMailInfo(title, content, userMail);
    }



}
