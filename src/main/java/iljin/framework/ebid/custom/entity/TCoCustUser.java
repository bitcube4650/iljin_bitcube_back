package iljin.framework.ebid.custom.entity;

import lombok.Data;

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
	@Column(name="cust_code")
	String custCode;
	@Column(name="user_name")
	String userName;
	@Column(name="user_tel")
	String userTel;
	@Column(name="user_hp")
	String userHp;
}
