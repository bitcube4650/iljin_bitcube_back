package iljin.framework.ebid.etc.util.common.excel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class BidProgressResponseDto {

    private String fileName;                            //파일이름
    private Map<String, Object> result;                 //엑셀 모든 컬럼
    private List<Map<String, Object>> tableContent;     //직접등록시 내역사항에 들어가는 세부사항 컬럼
    private List<Map<String, Object>> fileContent;      //파일등록시 내역사항에 들어가는 파일리스트 컬럼
    private List<Map<String, Object>> custContent;      //입찰참가업체 컬럼
}
