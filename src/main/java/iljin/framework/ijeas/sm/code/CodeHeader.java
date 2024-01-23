package iljin.framework.ijeas.sm.code;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Entity
@Table(name = "tb_code_hd")
@IdClass(CodeHeaderKey.class)
@Data
public class CodeHeader {

	@Id
	@Column(name="group_cd", nullable=false)
	String groupCd;

	@Id
	@Column(name="comp_cd", nullable=false)
	String compCd;

	@OneToMany(mappedBy = "codeHeader", fetch = FetchType.LAZY)
	@JsonIgnore
	List<CodeDetail> codeDetails;
	
	@Column(name = "group_nm")
	String groupNm;

	@Column(name = "group_desc")
	String groupDesc;

	@Column(name = "use_yn")
	String useYn;

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
