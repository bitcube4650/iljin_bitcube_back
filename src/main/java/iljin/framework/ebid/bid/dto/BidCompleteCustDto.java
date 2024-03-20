package iljin.framework.ebid.bid.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class BidCompleteCustDto {
	String biNo;			//입찰번호
	String custCode;		//협력사코드
	String custName;		//협력사명
	String presName;		//대표자명
	String esmtCurr;		//통화
	BigDecimal esmtAmt;		//개찰 후 견적금액
	String submitDate;		//제출일시
	String damdangName;		//담당자(협력사 관리자)
	String updateDate;		//낙찰일시
	String esmtYn;			//업체투찰 플래그
	String fileNm;			//투찰파일 명
	String filePath;		//투찰파일 경로
	String succYn;			//낙찰여부
	String etcFile;			//기타첨부파일 명
	String etcPath;			//기타첨부파일 경로
	List<BidCompleteSpecDto> bidSpec;		//협력사 직접입력
	
	public BidCompleteCustDto(String biNo, String custCode, String custName, String presName, String esmtCurr, 
			BigDecimal esmtAmt, String submitDate, String damdangName, String updateDate, 
			String esmtYn, String fileNm, String filePath, String succYn, String etcFile, String etcPath){
		this.biNo = biNo;			
		this.custCode = custCode;
		this.custName = custName;
		this.presName = presName;
		this.esmtCurr = esmtCurr;
		this.esmtAmt = esmtAmt;	
		this.submitDate = submitDate;
		this.damdangName = damdangName;
		this.updateDate = updateDate;
		this.esmtYn = esmtYn;	
		this.fileNm = fileNm;	
		this.filePath = filePath;
		this.succYn = succYn;	
		this.etcFile = etcFile;	
		this.etcPath = etcPath;	
	}
}
