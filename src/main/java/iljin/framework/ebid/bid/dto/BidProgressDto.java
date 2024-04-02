package iljin.framework.ebid.bid.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BidProgressDto {
    String biNo;
    String biName;
    String estStartDate;
    String estCloseDate;
    String biMode;
    String insMode;
    String ingTag;
    String cuser;
    String cuserEmail;
    String gongoId;
    String gongoEmail;
    String openerId;
    String openerEmail;
    String interrelatedCustCode;
    
    String damdangName;
    String damdangEmail;
    String esmtYn;

    /**
	 * 목록 (/api/v1/bid/progressList)
	 */
    public BidProgressDto(
        String biNo, String biName, String estStartDate, String estCloseDate, String biMode,
        String insMode, String ingTag, String cuser, String cuserEmail, String gongoId, 
        String gongoEmail, String interrelatedCustCode){
            this.biNo = biNo;
            this.biName = biName;
            this.estStartDate = estStartDate;
            this.estCloseDate = estCloseDate;
            this.biMode = biMode;
            this.insMode = insMode;
            this.ingTag = ingTag;
            this.cuser = cuser;
            this.cuserEmail = cuserEmail;
            this.gongoId = gongoId;
            this.gongoEmail = gongoEmail;
            this.interrelatedCustCode = interrelatedCustCode;
    }
    
    //그룹사 입찰진행 리스트
    public BidProgressDto(
        String biNo, String biName, String estCloseDate, String biMode,
        String insMode, String ingTag, String cuser, String cuserEmail, String openerId, String openerEmail){
            this.biNo = biNo;
            this.biName = biName;
            this.estCloseDate = estCloseDate;
            this.biMode = biMode;
            this.insMode = insMode;
            this.ingTag = ingTag;
            this.cuser = cuser;
            this.cuserEmail = cuserEmail;
            this.openerId = openerId;
            this.openerEmail = openerEmail;
    }
    
	//협력사 입찰진행 리스트
    public BidProgressDto(
            String biNo, String biName, String estStartDate, String estCloseDate, String biMode,
            String ingTag, String insMode, String damdangName, String damdangEmail, String esmtYn, String flag){
            this.biNo = biNo;
            this.biName = biName;
            this.estStartDate = estStartDate;
            this.estCloseDate = estCloseDate;
            this.biMode = biMode;
            this.insMode = insMode;
            this.ingTag = ingTag;
            this.damdangName = damdangName;
            this.damdangEmail = damdangEmail;
        }
}
