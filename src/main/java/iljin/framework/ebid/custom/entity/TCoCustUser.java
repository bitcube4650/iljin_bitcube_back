package iljin.framework.ebid.custom.entity;

import lombok.Data;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_co_cust_user")
@Data
public class TCoCustUser {
	@Id
	@Column(name="user_id")
	String userId;
	@Column(name="user_pwd")
	String userPwd;
	@Column(name="cust_code")
	int custCode;
	@Column(name="user_name")
	String userName;
	@Column(name="user_tel")
	String userTel;
	@Column(name="user_hp")
	String userHp;
	@Column(name="user_email")
	String userEmail;
	@Column(name="user_buseo")
	String userBuseo;
	@Column(name="user_position")
	String userPosition;
	@Column(name="pwd_chg_date")
	LocalDateTime pwdChgDate;
}
