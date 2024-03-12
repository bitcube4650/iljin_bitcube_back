package iljin.framework.ebid.etc.util.common.schedule.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "T_EMAIL")
@Data
public class MailEntity {

    @Id
    @Column(name="MAIL_ID")
    String mailId;

    @Column(name="TITLE")
    String title;

    @Column(name="CONTS")
    String conts;

    @Column(name="RECEIVES")
    String receives;

    @Column(name="SEND_FLAG")
    String sendFlag;

    @Column(name="ERROR_MSG")
    String errorMsg;

    @Column(name="SEND_DATE")
    LocalDate sendDate;

    @Column(name="CREATE_DATE")
    LocalDate createDate;

   // @Column(name="BI_NO")
   // String biNo;

    public MailEntity() {

    }
}
