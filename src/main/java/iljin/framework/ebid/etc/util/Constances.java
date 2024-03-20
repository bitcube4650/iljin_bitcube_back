package iljin.framework.ebid.etc.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
public class Constances {

    /*----------------------System Constances--------------------------*/

    public static String IS_REAL_SERVER;
    public static String SYSTEM_FILEENCRYPT_KEY;


    //스케줄러 실행여부
    @Value("${common.isReal.server}")
    private void setIsRealServer(String isRealServer) {
        Constances.IS_REAL_SERVER = isRealServer;
    }
    //암호화(AES 첨부파일) 키
    @Value("${file.encrypted.key}")
    private void setSystemFileencryptKey(String systemFileencryptKey) {
        Constances.SYSTEM_FILEENCRYPT_KEY = systemFileencryptKey;
    }


    /*----------------------Mail Constances----------------------------*/

    public static String MAIL_HOST;
    public static String MAIL_SENDER_ADDRESS;
    public static String MAIL_REPLYTO_ADDRESS;
    public static String MAIL_LINK_URL;

    //메일호스트정보
    @Value("${mail.host}")
    private void setMailHost(String mailHost) {
        Constances.MAIL_HOST = mailHost;
    }

    @Value("${mail.sender.address}")
    private void setMailSenderAddress(String MailSenderAddress) {
        Constances.MAIL_SENDER_ADDRESS = MailSenderAddress;
    }
    //반송메일주소
    @Value("${mail.replyto.address}")
    private void setMailReplytoAddress(String mailReplytoAddress) {
        Constances.MAIL_REPLYTO_ADDRESS = mailReplytoAddress;
    }
    //메일링크 URL
    @Value("${mail.replyto.address}")
    private void setMailLinkUrl(String mailLinkUrl) {
        Constances.MAIL_LINK_URL = mailLinkUrl;
    }


}
