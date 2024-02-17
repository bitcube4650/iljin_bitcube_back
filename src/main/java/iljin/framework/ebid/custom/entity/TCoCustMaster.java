package iljin.framework.ebid.custom.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_co_cust_master")
@Data
public class TCoCustMaster {
	@Id
	@Column(name="cust_code")
	String custCode;
	@Column(name="cust_name")
	String custName;
}
