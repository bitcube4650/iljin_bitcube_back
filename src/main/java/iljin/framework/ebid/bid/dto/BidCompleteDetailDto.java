package iljin.framework.ebid.bid.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class BidCompleteDetailDto {

	String biNo;								//입찰번호
	String biName;								//입찰명
	String itemName;							//품목
	String biMode;								//입찰방식
	String bidJoinSpec;							//입찰참가자격
	String specialCond;							//특수조건
	String spotDate;							//현장설명일시
	String spotArea;							//현장설명장소
	String succDeciMeth;						//낙찰자결정방법
	List<BidCompleteCustDto> custList;			//입찰참가업체
	String amtBasis;							//금액기준
	String payCond;								//결제조건
	BigDecimal bdAmt;							//예산금액
	String damdangName;							//입찰담당자
	String estStartDate;						//제출시작일시
	String estCloseDate;						//제출마감일시
	String estOpener;							//개찰자
	String estBidder;							//낙찰자
	String gongoName;							//입찰공고자
	String openAtt1;							//입회자1
	String openAtt2;							//입회자2
	String insMode;								//내역방식
	String supplyCond;							//납품조건
	String whyA3;								//재입찰사유
	String whyA7;								//유찰사유
	String addAccept;							//낙찰추가합의사항
	List<BidProgressFileDto> specFile;			//세부사항 - 파일등록
	List<BidCompleteSpecDto> specInput;			//세부사항 - 직접입력
	List<BidProgressFileDto> fileList;			//첨부파일
	
	public BidCompleteDetailDto(
			String biNo, String biName, String itemName, String biMode, String bidJoinSpec, 
			String specialCond, String spotDate, String spotArea, String succDeciMeth, 
			String amtBasis, String payCond, BigDecimal bdAmt, String damdangName, 	String estStartDate, 
			String estCloseDate, String estOpener, String estBidder, String gongoName, String openAtt1, String openAtt2, 
			String insMode, String supplyCond, String whyA3, String whyA7, String addAccept
	) {
		this.biNo = biNo;
		this.biName = biName;
		this.itemName = itemName;
		this.biMode = biMode;
		this.bidJoinSpec = bidJoinSpec;
		this.specialCond = specialCond;
		this.spotDate = spotDate;
		this.spotArea = spotArea;
		this.succDeciMeth = succDeciMeth;
		this.amtBasis = amtBasis;
		this.payCond = payCond;
		this.bdAmt = bdAmt;
		this.damdangName = damdangName;
		this.estStartDate = estStartDate;
		this.estCloseDate = estCloseDate;
		this.estOpener = estOpener;
		this.estBidder = estBidder;
		this.gongoName = gongoName;
		this.openAtt1 = openAtt1;
		this.openAtt2 = openAtt2;
		this.insMode = insMode;
		this.supplyCond = supplyCond;
		this.whyA3 = whyA3;
		this.whyA7 = whyA7;
		this.addAccept = addAccept;
	}
	
}
