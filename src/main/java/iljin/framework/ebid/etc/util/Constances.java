package iljin.framework.ebid.etc.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
public class Constances {

    /*----------------------System Constances--------------------------*/

    public static boolean COMMON_SCHEDULE_FLAG;
    public static String SYSTEM_FILEENCRYPT_KEY;
    public static String FILE_UPLOAD_DIRECTORY;
    public static String CERTIFICATE_SETTING_PATH;
    public static String CERTIFICATE_FILE_PATH;

    @Value("${file.upload.directory}")
    private void setFileUploadDirectory(String fileUploadDirectory) {
        Constances.FILE_UPLOAD_DIRECTORY = fileUploadDirectory;
    }
    
    @Value("${certificate.setting.path}")
    private void setCertSettingDirectory(String certSettingDirectory) {
        Constances.CERTIFICATE_SETTING_PATH = certSettingDirectory;
    }
    
    @Value("${certificate.file.path}")
    private void setCertFileDirectory(String certFileDirectory) {
        Constances.CERTIFICATE_FILE_PATH = certFileDirectory;
    }


    //스케줄러 실행여부
    @Value("${common.schedule.flag}")
    private void setIsRealServer(boolean commonScheduleFlag) {
        Constances.COMMON_SCHEDULE_FLAG = commonScheduleFlag;
    }
    //암호화(AES 첨부파일) 키
    @Value("${file.encrypted.key}")
    private void setSystemFileencryptKey(String systemFileencryptKey) {
        Constances.SYSTEM_FILEENCRYPT_KEY = systemFileencryptKey;
    }


    /*----------------------Mail Constances----------------------------*/

    public static String MAIL_HOST;
    public static String MAIL_PORT;
    public static String MAIL_SENDER_ADDRESS;
    public static String MAIL_REPLYTO_ADDRESS;
    public static String MAIL_LINK_URL;

    //메일호스트정보
    @Value("${mail.host}")
    private void setMailHost(String mailHost) {
        Constances.MAIL_HOST = mailHost;
    }
    
  //메일호스트정보
    @Value("${mail.port}")
    private void setMailPort(String mailPort) {
        Constances.MAIL_PORT = mailPort;
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
