package iljin.framework.ebid.etc.util;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.text.DecimalFormat;
import java.util.Properties;


@Component
public class CommonUtils {
    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private Constances constances;

    /**
     * 메일전송
     *
     * @param toEmailAddrArray
     * @param mailSubject
     * @param mailContents
     */
    public static void sendEmail(String[] toEmailAddrArray, String mailSubject, String mailContents) throws Exception {
        String MAIL_SMTP_CONNECTIONTIMEOUT ="mail.smtp.connectiontimeout";
        String MAIL_SMTP_TIMEOUT = "mail.smtp.timeout";
        String MAIL_SMTP_WRITETIMEOUT = "mail.smtp.writetimeout";
        String MAIL_SOCKET_TIMEOUT = "60000";

        if(toEmailAddrArray==null || toEmailAddrArray.length==0) return;
        int mailToCnt = 0;
        for(String toEmailAddr : toEmailAddrArray) {
            if(isCheckMailAddr(toEmailAddr)) { mailToCnt++; }
        }
        if(mailToCnt==0) return;
        Properties prop = new Properties();

        prop.put("mail.smtp.host", Constances.MAIL_HOST);
        prop.put(MAIL_SMTP_CONNECTIONTIMEOUT, MAIL_SOCKET_TIMEOUT);
        prop.put(MAIL_SMTP_TIMEOUT, MAIL_SOCKET_TIMEOUT);
        prop.put(MAIL_SMTP_WRITETIMEOUT, MAIL_SOCKET_TIMEOUT);


        Session session = Session.getDefaultInstance(prop, null);
        MimeMessage message = new MimeMessage(session);
//		try {
        InternetAddress from = new InternetAddress(Constances.MAIL_SENDER_ADDRESS);
        message.setFrom(from);
        InternetAddress[] toList = new InternetAddress[mailToCnt];
        int i = 0;
        for(String toEmailAddr : toEmailAddrArray) {
            if(isCheckMailAddr(toEmailAddr)) {
                InternetAddress to = new InternetAddress(toEmailAddr.trim());
                toList[i++] = to;
            }
        }
        try{
            InternetAddress[] reply = new InternetAddress[1];
            reply[0] = new InternetAddress(Constances.MAIL_REPLYTO_ADDRESS.trim());
            message.setReplyTo(reply);
        }catch(Exception e){
            e.printStackTrace();
        }
        message.setRecipients(Message.RecipientType.TO, toList);
//			message.setSubject(mailSubject);
        message.setSubject(MimeUtility.encodeText(mailSubject,"EUC-KR","B"));
//			message.setContent(mailContents, "text/plain");
        message.setContent(setMailContents(mailContents), "text/html; charset=EUC-KR");

        Transport.send(message);
//		} catch (Exception e) {
//
//			throw e;
//		}
    }


    public static boolean isCheckMailAddr(String mailAddress) {
        if (mailAddress == null || "".equals(mailAddress.trim())) return false;
        else {
            if (mailAddress.indexOf("@") == -1 || mailAddress.indexOf(".") == -1) return false;
        }
        return true;
    }

    public static String setMailContents(String contents) {
        StringBuffer mailContents = new StringBuffer();
        mailContents.append("<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>");
        mailContents.append("<html xmlns='http://www.w3.org/1999/xhtml'>");
        mailContents.append("<head><meta http-equiv='Content-Type' content='text/html; charset=UTF-8'></head>");
        mailContents.append("<body style='background:#f2f3f6'>");
        mailContents.append("    <link href='./mail01_files/css2' rel='stylesheet'>");
        mailContents.append("    <div style='width:600px; min-height:600px; margin:20px auto; background:#fff; padding:40px 40px 100px 40px; box-sizing:border-box; position:relative; font-family:\"Noto Sans KR\", sans-serif'>");
        mailContents.append("        <div style='border-bottom:1px solid #9F9F9F; padding-bottom:30px; margin-bottom:30px'><img src='./mail01_files/loginLogo_iljin.svg' alt='일진' style='height:40px'></div>");
        mailContents.append("        <div style='font-size:18px; font-weight:700; line-height:150%'>");
        mailContents.append("            안녕하십니까<br>");
        mailContents.append("            일진그룹 전자입찰 e-bidding 입니다.<br>");
        mailContents.append("        </div>");
        mailContents.append("        <div style='font-size:16px; line-height:150%; margin-top:30px'>");
        mailContents.append(contents + "<br><br>");
        mailContents.append("            자세한 사항은 <a href='https://iljin.idr.myds.me/login.html' target='_blank' style='color:#004B9E !important; font-weight:700; text-decoration:none; border-bottom:1px solid #004B9E'>e-bidding</a> 시스템에 로그인하여 확인해 주십시오<br><br><br>");
        mailContents.append("            감사합니다.");
        mailContents.append("        </div>");
        mailContents.append("        <div style='position:absolute; left:0; bottom:0; width:100%; box-sizing:border-box; padding:0 40px'>");
        mailContents.append("            <div style='border-top:1px solid #E6E6E6; color:#858585; font-size:12px; padding:20px 0 25px 0'>© ILJIN ALL RIGHTS RESERVED.</div>");
        mailContents.append("        </div>");
        mailContents.append("    </div>");
        mailContents.append("</body></html>");

        return mailContents.toString();
    }

    /**
     * Object를 받아 문자열 값으로 리턴함.
     * @param obj
     * @return
     */
    public static String getString(Object obj) {
        return getString(obj,"");
    }

    /**
     * Object를 받아 문자열 값으로 리턴함, 없을경우 DefaultValue 리턴.
     * @param obj
     * @param defaultValue
     * @return
     */
    public static String getString(Object obj, String defaultValue) {
        String value = "" + obj;
        try {
            if(obj == null) {
                value = defaultValue;
            } else {
                if(value.equals("null") || value.length() == 0) {
                    value = defaultValue;
                }
            }
        } catch(Exception e){
            value = defaultValue;
        }
        return value;
    }

    /**
     * Object -> int
     */
    public static int getInt (Object quan) {
        int     value = 0;
        try{
            String  strValue = getString(quan);
            value = Integer.valueOf(strValue);
        }catch(Exception e){
        }
        return value;
    }
    
    /**
     * String 받아 3자리수마다 . 찍고 String으로 리턴함, null이거나 공백일시 그대로 리턴
     * @param value
     * @return
     */
    public static String getFormatNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value; // 값이 null이거나 공백인 경우 그대로 반환
        }
        try {
            // 숫자로 변환
            double number = Double.parseDouble(value);
            // 숫자 포맷
            DecimalFormat formatter = new DecimalFormat("#,###");
            return formatter.format(number);
        } catch (NumberFormatException e) {
            // 숫자가 아닌 경우 그대로 반환
            return value;
        }
    }

}
