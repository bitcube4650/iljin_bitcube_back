package iljin.framework.ebid.custom.dto;

import lombok.Data;

import java.math.BigInteger;

@Data
public class TCoCustMasterDto {
    int custCode;
    String custName;
    String interrelatedNm;
    String custType1;
    String custType2;
    String regnum;
    String presName;
    String presJuminNo;
    String tel;
    String fax;
    String zipcode;
    String addr;
    String addrDetail;
    BigInteger capital;
    String foundYear;
    String createDate;

    String userName;
    String userEmail;
    String userId;
    String userHp;
    String userTel;
    String userBuseo;
    String userPosition;

    /**
     * 승인 목록 (/api/v1/cust/approvalList)
     */
    public TCoCustMasterDto(int custCode, String custName, String custType1, String regnum, String presName, String userName, String createDate) {
        this.custCode = custCode;
        this.custName = custName;
        this.custType1 = custType1;
        this.regnum = regnum;
        this.presName = presName;
        this.userName = userName;
        this.createDate = createDate;
    }

    /**
     * 승인 상세 (/api/v1/cust/approvalDetail)
     */
    public TCoCustMasterDto(int custCode, String custName, String interrelatedNm, String custType1, String custType2, String regnum, String presName, String presJuminNo
            , BigInteger capital, String foundYear, String tel, String fax, String zipcode, String addr, String addrDetail
            , String userName, String userEmail, String userId, String userHp, String userTel, String userBuseo, String userPosition) {
        this.custCode = custCode;
        this.custName = custName;
        this.interrelatedNm = interrelatedNm;
        this.custType1 = custType1;
        this.custType2 = custType2;
        this.regnum = regnum;
        this.presName = presName;
        this.presJuminNo = presJuminNo;
        this.capital = capital;
        this.foundYear = foundYear;
        this.tel = tel;
        this.fax = fax;
        this.zipcode = zipcode;
        this.addr = addr;
        this.addrDetail = addrDetail;

        this.userName = userName;
        this.userEmail = userEmail;
        this.userId = userId;
        this.userHp = userHp;
        this.userTel = userTel;
        this.userBuseo = userBuseo;
        this.userPosition = userPosition;
    }

}
