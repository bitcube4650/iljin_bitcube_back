package iljin.framework.ebid.etc.notice.dto;


import java.sql.Date;

import lombok.Data;

@Data
public class FaqDto {

	private Integer faqId;
	private String faqType;
	private String faqTypeDescription;
    private String title;
    private String answer;
    private String createUser;
    private String createDate;
	
    public FaqDto() {}
    
	/**
	 * 목록 (/api/v1/faq/faqList)
	 */

    public FaqDto(
            Integer faqId, 
            String faqType,
            String faqTypeDescription,
            String title, 
            String answer, 
            String createUser, 
            String createDate
    ) {
        this.faqId = faqId; 
        this.faqType = faqType;
        this.faqTypeDescription = faqTypeDescription;
        this.title = title; 
        this.answer = answer; 
        this.createUser = createUser; 
        this.createDate = createDate;
    }
}
