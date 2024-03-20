package iljin.framework.ebid.custom.entity;

import iljin.framework.core.security.user.User;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_co_user")
@Data
public class TCoUser {
	@Id
	@Column(name="user_id")
	String userId;
	@Column(name="user_pwd")
	String userPwd;
	@Column(name="user_name")
	String userName;
	@Column(name="user_auth")
	String userAuth;
	@Column(name="user_email")
	String userEmail;
	@Column(name="user_hp")
	String userHp;
	@Column(name="user_position")
	String userPosition;
	@Column(name="user_tel")
	String userTel;
	@Column(name="pwd_edit_yn")
	String pwdEditYn;
	@Column(name="pwd_edit_date")
	LocalDateTime pwdEditDate;
	@Column(name="dept_name")
	String deptName;
	@Column(name="interrelated_cust_code")
	String interrelatedCustCode;
	@Column(name="openauth")
	String openauth;
	@Column(name="bidauth")
	String bidauth;
}
