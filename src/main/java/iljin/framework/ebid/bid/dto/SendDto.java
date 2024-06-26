package iljin.framework.ebid.bid.dto;

import lombok.Data;

@Data
public class SendDto {
    String userEmail;	//수신인 메일
    String fromEmail;	//발송인 메일
    String userHp;
    String userName;
    

    public SendDto(String userEmail){
        this.userEmail = userEmail;
    }
    
    public SendDto(String userEmail, String fromEmail){
        this.userEmail = userEmail;
        this.fromEmail = fromEmail;
    }
    
    public SendDto(String userEmail, String fromEmail, String userHp, String userName){
        this.userEmail = userEmail;
        this.fromEmail = fromEmail;
        this.userHp = userHp;
        this.userName = userName;
    }
}
