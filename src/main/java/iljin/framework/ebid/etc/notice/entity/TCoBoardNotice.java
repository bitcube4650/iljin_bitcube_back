package iljin.framework.ebid.etc.notice.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import iljin.framework.ebid.custom.entity.TCoItem;
import iljin.framework.ebid.custom.entity.TCoItemGrp;
import iljin.framework.ebid.custom.entity.TCoUser;
import lombok.Data;

@Entity
@Table(name = "t_co_board_notice")
@Data
public class TCoBoardNotice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	@Column(name="b_no")
	Integer bNo;
	
	@Column(name="b_userid")
	String bUserid;
	
	@Column(name="b_title")
	String bTitle;
	
	@Column(name="b_date")
	LocalDateTime bDate;
	
	@Column(name="b_count")
	Integer bCount;
	
	@Column(name="b_file")
	String bFile;
	
	@Column(name="b_content")
	String bContent;
	
	@Column(name="b_file_path")
	String bFilePath;

	@Column(name="b_co")
	String bCo;
}
