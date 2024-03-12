package iljin.framework.ebid.etc.util;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.util.Properties;

public class CommonUtils {

    /**
     * 메일전송
     *
     * @param toEmailAddrArray
     * @param mailSubject
     * @param mailContents
     */
    public static void sendEmail(String[] toEmailAddrArray, String mailSubject, String mailContents) throws Exception {
        String MAIL_SMTP_CONNECTIONTIMEOUT = "mail.smtp.connectiontimeout";
        String MAIL_SMTP_TIMEOUT = "mail.smtp.timeout";
        String MAIL_SMTP_WRITETIMEOUT = "mail.smtp.writetimeout";
        String MAIL_SOCKET_TIMEOUT = "60000";

        if (toEmailAddrArray == null || toEmailAddrArray.length == 0) return;
        int mailToCnt = 0;
        for (String toEmailAddr : toEmailAddrArray) {
            if (isCheckMailAddr(toEmailAddr)) {
                mailToCnt++;
            }
        }
        if (mailToCnt == 0) return;
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
        mailContents.append("<head>");
        mailContents.append("<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />");
        mailContents.append("<title>OKPlaza</title>");
        mailContents.append("</head>");
        mailContents.append("<body style='margin:0;padding:0'>");

        mailContents.append("	<table cellspacing='0' style='width:700px;padding:0px;background:#fff;border:2px solid #F53243;'>");

        //HEADER
        mailContents.append("		<tr>");
        mailContents.append("			<td style='padding:0 0 0 25px;background:#F53243;border:2px solid #F53243;height:50px;overflow:hidden;text-align:center;' nowrap>");
        mailContents.append("				<img src='" + Constances.MAIL_LINK_URL+"/img/buy/edm_ico.png'>");
        mailContents.append("			</td>");
        mailContents.append("			<td style='padding:0 25px 0 0;background:#F53243;border:2px solid #F53243;width:300px;height:50px;overflow:hidden;font-size:20px;font-family:\"Malgun Gothic\";font-weight:bold;' nowrap>");
        mailContents.append("				<span style='margin-left:10px; color:#fff;'>E-mail 서비스 입니다.</span>");
        mailContents.append("			</td>");
        mailContents.append("			<td style='background:#F53243;height:50px;overflow:hidden;border:2px solid #F53243;width:310px;' nowrap></td>");
        mailContents.append("		</tr>");

        //CONTENT
        mailContents.append("		<tr>");
        mailContents.append("			<td colspan='3' style='margin:0;padding:20px 55px 10px;background:#fff;font-size:14px;line-height:20px;font-family:\"Malgun Gothic\";letter-spacing:-1px;font-weight:bold;color:#343434;word-break:break-all'>");
        mailContents.append(contents);
      //  mailContents.append("			<br/><br/><span style='color:#1f497d;'>Okplaza 바로가기 클릭 ☞ </span><a href='"+Constances.SYSTEM_FRONT_URL+"' style='vertical-align:sub;'><img src='"+Constances.MAIL_LINK_URL+"/img/mailLogos.png'></a>");
        mailContents.append("			</td>");
        mailContents.append("		</tr>");

        //FOOTER
        mailContents.append("		<tr>");
        mailContents.append("			<td colspan='3' style='padding:0 25px;overflow:hidden;background:#fff;' nowrap>");
        mailContents.append("				<hr style='width:650px;border:1px solid #F53243;overflow:hidden;color:#F53243;'>");
        mailContents.append("			</td>");
        mailContents.append("		</tr>");
        mailContents.append("		<tr>");
        mailContents.append("			<td colspan='3' style='padding:0px 25px 5px;overflow:hidden;background:#fff;text-align:right;' nowrap>");
        mailContents.append("				<img src='"+Constances.MAIL_LINK_URL+"/img/SKtelesys_mails.png' alt='OK plaza'>");
        mailContents.append("			</td>");
        mailContents.append("		</tr>");

        mailContents.append("	</table>");

        mailContents.append("</body>");
        mailContents.append("</html>");

        return mailContents.toString();
    }
}
