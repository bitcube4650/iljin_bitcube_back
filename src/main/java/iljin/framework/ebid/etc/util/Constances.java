package iljin.framework.ebid.etc.util;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Setter
public class Constances {

    /*----------------------System Constances--------------------------*/

    //스케줄러 실행여부
    @Value("${common.isReal.server}")
    public static Boolean isRealServer;

    //암호화(AES 첨부파일) 키
    @Value("${file.encrypted.key}")
    public static String SYSTEM_FILEENCRYPT_KEY;


    /*----------------------Mail Constances----------------------------*/

    //메일호스트정보
    @Value("${mail.host}")
    public static String MAIL_HOST;
    //메일송신자 메일주소
    @Value("${mail.sender.address}")
    public static String MAIL_SENDER_ADDRESS;
    //반송메일주소
    @Value("${mail.replyto.address}")
    public static String MAIL_REPLYTO_ADDRESS;
    //메일링크 URL
    @Value("${mail.link.url}")
    public static String MAIL_LINK_URL;





}
