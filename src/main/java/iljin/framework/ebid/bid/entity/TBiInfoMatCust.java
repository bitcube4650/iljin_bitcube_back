package iljin.framework.ebid.bid.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import iljin.framework.ebid.etc.notice.entity.TCoBoardCustID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@IdClass(TBiInfoMatCustID.class)
@Table(name = "t_bi_info_mat_cust")
public class TBiInfoMatCust {

	@Id
    @Column(name = "bi_no")
    String biNo;
	
	@Id
    @Column(name = "cust_code")
    Integer custCode;
	
	@Column(name="rebid_att")
	String rebidAtt;
	
	@Column(name="esmt_yn")
	String esmtYn;
	
	@Column(name="esmt_amt")
    private BigDecimal esmtAmt;
	
	@Column(name="succ_yn")
	String succYn;
	
	@Column(name = "file_id")
    Integer fileId;
	
	@Column(name="submit_date")
	LocalDateTime submitDate;
	
	@Column(name="create_user")
	String createUser;
	
	@Column(name="create_date")
	LocalDateTime createDate;
	
	@Column(name="update_user")
	String updateUser;
	
	@Column(name="update_date")
	LocalDateTime updateDate;
	
	@Column(name = "bi_order")
    Integer biOrder;
	
	@Column(name="enc_qutn")
	String encQutn;
	
	@Column(name="enc_esmt_spec")
	String encEsmtSpec;
	
	@Column(name="esmt_curr")
	String esmtCurr;
	
	@Column(name="etc_b_file")
	String etcBFile;
	
	@Column(name="file_hash_value")
	String fileHashValue;
	
	@Column(name="etc_b_file_path")
	String etcBFilePath;
	
	
}
