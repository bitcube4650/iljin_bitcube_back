package iljin.framework.ebid.bid.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@IdClass(TBiDetailMatCustTempID.class)
@Table(name = "t_bi_detail_mat_cust_temp")
public class TBiDetailMatCustTemp {

	@Id
	@Column(name = "bi_no")
	String biNo;

	@Id
	@Column(name = "seq")
	Integer seq;
	
	@Id
	@Column(name = "cust_code")
	Integer custCode;

	@Id
	@Column(name = "bi_order")
	Short biOrder;
	
	@Column(name="esmt_uc")
	BigDecimal esmtUc;
	
}
