package iljin.framework.ebid.bid.dto;

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
	
}
