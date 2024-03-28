package iljin.framework.ebid.bid.dto;

import java.math.BigDecimal;
import java.math.BigInteger;

import lombok.Data;

@Data
public class BidCompleteDto {

	String biNo;
	String biName;
	String updateDate;
	String biMode;
	String ingTag;
	String insMode;
	String userName;
	String userEmail;
	
	String bidOpenDate;
	String succYn;
	
	String matDept;
	String matProc;
	String matCls;
	String matFactory;
	String matFactoryLine;
	String matFactoryCnt;
	BigDecimal bdAmt;
	BigDecimal succAmt;
	String custName;
	BigInteger joinCustCnt;
	String estStartDate;
	String estCloseDate;
	
	//그룹사
	public BidCompleteDto(
			String biNo, String biName, String updateDate, String biMode,
			String ingTag, String insMode, String userName, String userEmail
	){
		this.biNo = biNo;
		this.biName = biName;
		this.updateDate = updateDate;
		this.biMode = biMode;
		this.ingTag = ingTag;
		this.insMode = insMode;
		this.userName = userName;
		this.userEmail = userEmail;
	}
	
	//입찰이력
	public BidCompleteDto(
		String biNo, String matDept, String matProc, String matCls, String matFactory, String matFactoryLine, String matFactoryCnt,
		String biName, BigDecimal bdAmt, BigDecimal succAmt, String custName, BigInteger joinCustCnt, String estStartDate, String estCloseDate,
		String userName
	) {
		this.biNo = biNo;
		this.matDept = matDept;
		this.matProc = matProc;
		this.matCls = matCls;
		this.matFactory = matFactory;
		this.matFactoryLine = matFactoryLine;
		this.matFactoryCnt = matFactoryCnt;
		this.biName = biName;
		this.bdAmt = bdAmt;
		this.succAmt = succAmt;
		this.custName = custName;
		this.joinCustCnt = joinCustCnt;
		this.estStartDate = estStartDate;
		this.estCloseDate = estCloseDate;
		this.userName = userName;
	}
	
	//협력사
	public BidCompleteDto(
			String biNo, String biName, String bidOpenDate, String biMode,
			String succYn, String insMode, String userName, String userEmail, int cust
	){
		this.biNo = biNo;
		this.biName = biName;
		this.bidOpenDate = bidOpenDate;
		this.biMode = biMode;
		this.succYn = succYn;
		this.insMode = insMode;
		this.userName = userName;
		this.userEmail = userEmail;
	}
	
	//그룹사 - 통계 - 입찰내역상세
	public BidCompleteDto(
			String biNo, String biName, BigDecimal bdAmt, BigDecimal succAmt, 
			String custName, BigInteger joinCustCnt, String estStartDate, String estCloseDate, String userName
	) {
		this.biNo = biNo;
		this.biName = biName;
		this.bdAmt = bdAmt;
		this.succAmt = succAmt;
		this.custName = custName;
		this.joinCustCnt = joinCustCnt;
		this.estStartDate = estStartDate;
		this.estCloseDate = estCloseDate;
		this.userName = userName;
	}
}
