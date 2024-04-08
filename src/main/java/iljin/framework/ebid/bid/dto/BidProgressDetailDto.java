package iljin.framework.ebid.bid.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BidProgressDetailDto {
	String biNo;
	String biName;
	String biMode;
	String biModeCode;
	String insMode;
	String insModeCode;
	String bidJoinSpec;
	String specialCond;
	String supplyCond;
	String spotDate;
	String spotArea;
	String succDeciMeth;
	String succDeciMethCode;
	String estStartDate;
	String estCloseDate;
	Integer estCloseCheck;
	String estOpener;
	String estOpenerCode;
	String cuser;
	String cuserCode;
	String estOpenDate;
	String openAtt1;
	String openAtt1Code;
	String estBidder;
	String estBidderCode;
	String openAtt1Sign;
	String openAtt2;
	String openAtt2Code;
	String openAtt2Sign;
	String ingTag;
	String createUser;
	String createDate;
	String updateUser;
	String updateDate;
	String itemCode;
	String itemName;
	String gongoId;
	String gongoIdCode;
	String cuserDept;
	String payCond;
	String whyA3;
	String whyA7;
	String biOpen;
	String interrelatedCustCode;
	String interrelatedNm;
	String realAmt;
	String amtBasis;
	BigDecimal bdAmt;
	String addAccept;
	String matDept;
	String matProc;
	String matCls;
	String matFactory;
	String matFactoryLine;
	String matFactoryCnt;
	
	String damdangName;
	String gongoName;
	
	List<BidCustDto> custList;			//입찰참가업체
	List<BidProgressFileDto> specFile;			//세부사항 - 파일등록
	List<BidItemSpecDto> specInput;			//세부사항 - 직접입력
	List<BidProgressFileDto> fileList;			//첨부파일
	
	Boolean bidAuth;							//낙찰권한
	Boolean openAuth;							//개찰권한
	
	String openAtt1Id;			//입회자1 id
	String openAtt2Id;			//입회자2 id

	String estOpenerId;			//개찰자 id
	String estBidderId;			//낙찰자 id

	public BidProgressDetailDto(
			String biNo, String biName, String itemName, String biMode, String bidJoinSpec, String specialCond, String spotDate, String spotArea,
			String succDeciMeth, String amtBasis, String payCond, BigDecimal bdAmt, String createUser, String damdangName, String estStartDate, String estCloseDate, Integer estCloseCheck,
			String estOpener, String estBidder, String gongoName, String openAtt1, String openAtt2, String insMode, String supplyCond, String whyA3,
			String ingTag, String interrelatedCustCode, String matDept, String matProc, String matCls, String matFactory, String matFactoryLine, String matFactoryCnt,
			String openAtt1Id, String openAtt2Id, String openAtt1Sign, String openAtt2Sign, String estOpenerId, String estBidderId
	){
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
			this.estCloseCheck = estCloseCheck;
			this.estCloseDate = estCloseDate;
			this.estOpener = estOpener;
			this.estBidder = estBidder;
			this.gongoName = gongoName;
			this.openAtt1 = openAtt1;
			this.openAtt2 = openAtt2;
			this.insMode = insMode;
			this.supplyCond = supplyCond;
			this.whyA3 = whyA3;
			this.ingTag = ingTag;
			this.interrelatedCustCode = interrelatedCustCode;
			this.matDept = matDept;
			this.matProc = matProc;
			this.matCls = matCls;
			this.matFactory = matFactory;
			this.matFactoryLine = matFactoryLine;
			this.matFactoryCnt = matFactoryCnt;
			this.createUser = createUser;
			this.openAtt1Id = openAtt1Id;
			this.openAtt2Id = openAtt2Id;
			this.openAtt1Sign = openAtt1Sign;
			this.openAtt2Sign = openAtt2Sign;
			this.estOpenerId = estOpenerId;
			this.estBidderId = estBidderId;
	}
}
