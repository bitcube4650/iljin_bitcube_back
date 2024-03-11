package iljin.framework.ebid.etc.util.common.excel.dto;

import com.querydsl.core.annotations.QueryProjection;
import iljin.framework.ebid.etc.util.common.excel.utils.ExcelColumnName;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class FileExcelDownload {

    @ExcelColumnName(name = "No")
    String fileId;

    @ExcelColumnName(name = "파일번호")
    String biNo;

    @ExcelColumnName(name = "생성날짜")
    LocalDate createDate;

    @ExcelColumnName(name = "코드값")
    String fCustCode;

    @ExcelColumnName(name = "파일여부")
    String fileFlag;

    @ExcelColumnName(name = "파일이름")
    String fileNm;

    @ExcelColumnName(name = "파일주소")
    String filePath;

    @ExcelColumnName(name = "사용여부")
    String useYn;

    @QueryProjection
    public FileExcelDownload(String fileId, String biNo, LocalDate createDate, String fCustCode, String fileFlag, String fileNm, String filePath, String useYn) {
        this.fileId = fileId;
        this.biNo = biNo;
        this.createDate = createDate;
        this.fCustCode = fCustCode;
        this.fileFlag = fileFlag;
        this.fileNm = fileNm;
        this.filePath = filePath;
        this.useYn = useYn;
    }
}
