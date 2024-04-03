package iljin.framework.ebid.bid.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class BidCustDto {
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
	List<BidItemSpecDto> bidSpec;		//협력사 직접입력
	
	String biName;			//입찰명
	
	String fileId;			//투찰파일 id (암호화)
	String encQutn;			//견적금액 (암호화)
	String encEsmtSpec;		//협력사 입찰 직접입력정보 (암호화)
	String insMode;			//입찰 내역방식
	
	public BidCustDto(String biNo, String custCode, String custName, String presName, String esmtCurr, 
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
	
	public BidCustDto(String biNo, String biName, String custName, BigDecimal esmtAmt, String submitDate, String succYn) {
		this.biNo = biNo;
		this.biName = biName;
		this.custName = custName;
		this.esmtAmt = esmtAmt;
		this.submitDate = submitDate;
		this.succYn = succYn;
	}
	
	//입찰진행 - 업체견적사항
	public BidCustDto(String biNo, String custCode, String custName, String presName, String esmtCurr, String submitDate, String damdangName, String esmtYn, String etcPath) {
		this.biNo = biNo;
		this.custCode = custCode;
		this.custName = custName;
		this.presName = presName;
		this.esmtCurr= esmtCurr;
		this.submitDate = submitDate;
		this.damdangName = damdangName;
		this.esmtYn = esmtYn;
		this.etcPath = etcPath;
	}
	
	//입찰진행 - 개찰
	public BidCustDto(String biNo, String custCode, String fileId, String encQutn, String encEsmtSpec, String insMode) {
		this.biNo = biNo;
		this.custCode = custCode;
		this.fileId = fileId;
		this.encQutn = encQutn;
		this.encEsmtSpec = encEsmtSpec;
		this.insMode = insMode;
	}
}
