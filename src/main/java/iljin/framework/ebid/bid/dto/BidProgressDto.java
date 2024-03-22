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
    String interrelatedCustCode;
    String openAtt1;
    String openAtt2;

    /**
	 * 목록 (/api/v1/bid/progressList)
	 */
    public BidProgressDto(
        String biNo, String biName, String estStartDate, String estCloseDate, String biMode,
        String insMode, String ingTag, String cuser, String cuserEmail, String gongoId, 
        String gongoEmail, String interrelatedCustCode, String openAtt1, String openAtt2){
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
            this.openAtt1 = openAtt1;
            this.openAtt2 = openAtt2;
    }
}
