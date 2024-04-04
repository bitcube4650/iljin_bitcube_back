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
@IdClass(TBiInfoMatID.class)
@Table(name = "t_bi_info_mat")
public class TBiInfoMat {

	@Id
    @Column(name = "bi_no")
    String biNo;
	
	@Column(name="bi_name")
	String biName;
	
	@Column(name="bi_mode")
	String biMode;	
	
	@Column(name="ins_mode")
	String insMode;
	
	@Column(name="bid_join_spec")
	String bidJoinSpec;
	
	@Column(name="special_cond")
	String specialCond;
	
	@Column(name="supply_cond")
	String supplyCond;
	
	@Column(name="spot_date")
	String spotDate;
	
	@Column(name="spot_area")
	String spotArea;
	
	@Column(name="succ_deci_meth")
	String succDeciMeth;
	
	@Column(name="bid_open_date")
	String bidOpenDate;
	
	@Column(name="amt_basis")
	String amtBasis;
	
	@Column(name="bd_amt")
	BigDecimal bdAmt;
	
	@Column(name="succ_amt")
	BigDecimal succAmt;
	
	@Column(name="est_start_date")
	String estStartDate;
	
	@Column(name="est_close_date")
	String estCloseDate;
	
	@Column(name="est_opener")
	String estOpener;
	
	@Column(name="est_bidder")
	String estBidder;
	
	@Column(name="est_open_date")
	String estOpenDate;
	
	@Column(name="open_att1")
	String openAtt1;
	
	@Column(name="open_att1_sign")
	String openAtt1Sign;
	
	@Column(name="open_att2")
	String openAtt2;
	
	@Column(name="open_att2_sign")
	String openAtt2Sign;
	
	@Column(name="ing_tag")
	String ingTag;
	
	@Column(name="create_user")
	String createUser;
	
	@Column(name="create_date")
	String createDate;
	
	@Column(name="update_user")
	String updateUser;
	
	@Column(name="update_date")
	String updateDate;
	
	@Column(name="item_code")
	String itemCode;
	
	@Column(name="gongo_id")
	String gongoId;
	
	@Column(name="pay_cond")
	String payCond;
	
	@Column(name="why_a3")
	String whyA3;
	
	@Column(name="why_a7")
	String whyA7;
	
	@Column(name="bi_open")
	String biOpen;
	
	@Column(name="interrelated_cust_code")
	String interrelatedCustCode;
	
	@Column(name="real_amt")
	BigDecimal realAmt;
	
	@Column(name="add_accept")
	String addAccept;
	
	@Column(name="mat_dept")
	String matDept;
	
	@Column(name="mat_proc")
	String matProc;
	
	@Column(name="mat_cls")
	String matCls;
	
	@Column(name="mat_factory")
	String matFactory;
	
	@Column(name="mat_factory_line")
	String matFactoryLine;
	
	@Column(name="mat_factory_cnt")
	String matFactoryCnt;
	
}
