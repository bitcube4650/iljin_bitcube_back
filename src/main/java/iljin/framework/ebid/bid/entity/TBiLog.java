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
@Table(name = "t_bi_log")
@Data
public class TBiLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	@Column(name="log_seq")
	Integer logSeq;
	
	@Column(name="bi_no")
	String biNo;
	
	@Column(name="user_id")
	String userId;
	
	@Column(name="log_text")
	String logText;
	
	@Column(name="create_date")
	LocalDateTime createDate;
}
