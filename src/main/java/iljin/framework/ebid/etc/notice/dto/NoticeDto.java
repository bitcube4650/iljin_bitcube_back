package iljin.framework.ebid.etc.notice.dto;

import java.math.BigInteger;
import java.sql.Timestamp;
import lombok.Data;

@Data
public class NoticeDto {

	private Integer bNo;
    private String bUserid;
    private String bTitle;
    private String bDate;
    private Integer bCount;
    private String bFile;
    private String bContent;
    private String bFilePath;
    private String bCo;
    private String bUserName;
    private BigInteger rowNo;
	
    public NoticeDto() {}
    
	/**
	 * 목록 (/api/v1/notice/noticeList)
	 */

    public NoticeDto(
            Integer bNo, 
            String bUserid, 
            String bTitle, 
            String bDate, 
            Integer bCount, 
            String bFile, 
            String bContent, 
            String bFilePath, 
            String bCo, 
            String bUserName, 
            BigInteger rowNo
    ) {
        this.bNo = bNo;
        this.bUserid = bUserid;
        this.bTitle = bTitle;
        this.bDate = bDate;
        this.bCount = bCount;
        this.bFile = bFile;
        this.bContent = bContent;
        this.bFilePath = bFilePath;
        this.bCo = bCo;
        this.bUserName = bUserName;
        this.rowNo = rowNo;
    }
}
