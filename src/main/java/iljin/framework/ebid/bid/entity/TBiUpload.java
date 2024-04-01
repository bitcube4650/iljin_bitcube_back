package iljin.framework.ebid.bid.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "t_bi_upload")
@Data
public class TBiUpload {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	@Column(name="file_id")
	Integer fileId;
	
	@Column(name="bi_no")
	String biNo;
	
	@Column(name="file_flag")
	String fileFlag;
	
	@Column(name="f_cust_code")
	Integer fCustCode;
	
	@Column(name="file_nm")
	String fileNm;
	
	@Column(name="file_path")
	String filePath;
	
	@Column(name="create_date")
	LocalDateTime createDate;
	
	@Column(name="use_yn")
	String useYn;
}
