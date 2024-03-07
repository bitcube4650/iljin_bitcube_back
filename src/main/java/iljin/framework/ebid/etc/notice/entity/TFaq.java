package iljin.framework.ebid.etc.notice.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "t_faq")
@Data
public class TFaq {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	@Column(name="faq_id")
	Integer faqId;
	
	@Column(name="faq_type")
	String faqType;
	
	@Column(name="title")
	String title;
	
	@Column(name="answer")
	String answer;
	
	@Column(name="create_user")
	String createUser;
	
	@Column(name="create_date")
	LocalDateTime createDate;
}
