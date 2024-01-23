package iljin.framework.ijeas.sm.code;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;

@Entity
@Table(name = "tb_code_dt")
@IdClass(CodeDetailKey.class)
@Data
public class CodeDetail {

	@Id
	@Column(name="group_cd", nullable=false)
	String groupCd;

	@Id
	@Column(name="detail_cd")
	String detailCd;

	@Id
	@Column(name="comp_cd", nullable=false)
	String compCd;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumns({
	    @JoinColumn(name = "group_cd", referencedColumnName="group_cd", insertable = false, updatable = false),
		@JoinColumn(name = "comp_cd", referencedColumnName="comp_cd", insertable = false, updatable = false)
	})
	CodeHeader codeHeader;
	
	@Column(name = "detail_nm")
	String detailNm;

	@Column(name = "use_yn")
	String useYn;

	@Column(name = "order_seq")
	Integer orderSeq;

	@Column(name = "detail_desc")
	String detailDesc;

	@Column(name = "remark1")
	String remark1;

	@Column(name = "remark2")
	String remark2;

	@Column(name = "remark3")
	String remark3;

	@Column(name = "remark4")
	String remark4;

	@Column(name = "remark5")
	String remark5;

	@Column(name = "reg_id")
	String regId;

	@CreationTimestamp
	@Column(name = "reg_dtm")
	LocalDateTime regDtm;

	@Column(name = "chg_id")
	String chgId;

	@UpdateTimestamp
	@Column(name = "chg_dtm")
	LocalDateTime chgDtm;
	
	
}
