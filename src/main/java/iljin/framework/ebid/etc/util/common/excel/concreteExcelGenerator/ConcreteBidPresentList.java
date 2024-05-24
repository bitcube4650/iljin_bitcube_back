package iljin.framework.ebid.etc.util.common.excel.concreteExcelGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import iljin.framework.ebid.etc.statistics.dto.BiInfoDto;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.GeneralDao;
import iljin.framework.ebid.etc.util.common.excel.repository.ExcelRepository;
import iljin.framework.ebid.etc.util.common.excel.utils.ExcelSupportV2;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ConcreteBidPresentList extends ExcelSupportV2 {
    @Autowired
    ExcelRepository excelRepository;
    
    @Autowired
    GeneralDao generalDao;
    @Override
    public SXSSFWorkbook getWorkBookPaging(Class<?> clazz, SXSSFWorkbook workbook, List<String> headerNames, Map<String, Object> param) {
        Sheet sheet = workbook.createSheet("Sheet1"); // 엑셀 sheet 이름
        sheet.setDefaultColumnWidth(12); // 디폴트 너비 설정

        Row row = null;
        Cell cell = null;
        row = sheet.createRow(0);


        //엑셀 헤더 작성
        createHeaders(workbook, row, cell, headerNames);

        //엑셀 바디 작성
        List<BiInfoDto> data = new ArrayList<>();

        int offset = 0;
        int limit = 1000;
        int cnt = 0;
        
		try {
			List<Object> bidPresentList = generalDao.selectGernalList("statistics.bidPresentList", param);
		
        List<Map<String,Object>> excelData = new ArrayList<>();
	        for(Object bidPresendListObject : bidPresentList) {
	        	Map<String,Object> bidPresendListMap = (Map<String, Object>) bidPresendListObject;
	        	

	        	/*
                BiddingStatusDto biddingStatusDto = new BiddingStatusDto();

                String interrelatedNm = CommonUtils.getString(data.get(i).getInterrelatedNm(), " ");
                String planCnt = CommonUtils.getString(data.get(i).getPlanCnt(), " ");
                BigDecimal planAmt = data.get(i).getPlanAmt();
                String ingCnt = CommonUtils.getString(data.get(i).getIngCnt(), " ");
                BigDecimal ingAmt = data.get(i).getIngAmt();
                String succCnt = CommonUtils.getString(data.get(i).getSuccCnt(), " ");
                BigDecimal succAmt = data.get(i).getSuccAmt();
                String custCnt = CommonUtils.getString(data.get(i).getCustCnt(), " ");
                String regCustCnt = CommonUtils.getString(data.get(i).getRegCustCnt(), " ");
                String testNull = "";

                biddingStatusDto.setInterrelatedNm(interrelatedNm);
                biddingStatusDto.setPlanCnt(CommonUtils.getFormatNumber(planCnt));
                biddingStatusDto.setPlanAmt(planAmt);
                biddingStatusDto.setIngCnt(CommonUtils.getFormatNumber(ingCnt));
                biddingStatusDto.setIngAmt(ingAmt);
                biddingStatusDto.setSuccCnt(CommonUtils.getFormatNumber(succCnt));
                biddingStatusDto.setSuccAmt(succAmt);
                biddingStatusDto.setCustCnt(custCnt);
                biddingStatusDto.setRegCustCnt(CommonUtils.getFormatNumber(regCustCnt));
                biddingStatusDto.setTestNull("");
                */

                excelData.add(bidPresendListMap);
	            
	
	        }
	        
            int start = offset;
        	
            createbody(clazz, workbook, excelData, sheet, row, cell, start);

            offset += limit;
            cnt++;

            excelData.clear();
            ((SXSSFSheet) sheet).flushRows(limit);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return workbook;
    }

    @Override
    public void createHeaders(SXSSFWorkbook workbook, Row row, Cell cell, List<String> headerNames) {
        /**
         * header font style
         */
        Font headerFont = workbook.createFont();
        headerFont.setFontHeightInPoints((short) 11);

        /**
         * header cell style
         */
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);       // 가로 가운데 정렬
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 세로 가운데 정렬
        headerCellStyle.setFont(headerFont);

        //테두리 설정
        headerCellStyle.setBorderLeft(BorderStyle.MEDIUM);
        headerCellStyle.setBorderRight(BorderStyle.MEDIUM);
        headerCellStyle.setBorderTop(BorderStyle.MEDIUM);
        headerCellStyle.setBorderBottom(BorderStyle.MEDIUM);

        //배경 설정
        headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND );
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER );
        headerCellStyle.setFont(headerFont);

        // 두 번째 행 생성 (1행)
        Row secondRow = row.getSheet().createRow(1);
        Cell cell2 = null;

        headerNames.clear();


        CellRangeAddress mergedRegion = null;


        mergedRegion = new CellRangeAddress(0, 1, 0, 0);
        row.getSheet().addMergedRegion(mergedRegion);


        mergedRegion = new CellRangeAddress(0, 0, 1, 2);
        row.getSheet().addMergedRegion(mergedRegion);


        mergedRegion = new CellRangeAddress(0, 0, 3, 4);
        row.getSheet().addMergedRegion(mergedRegion);


        mergedRegion = new CellRangeAddress(0, 0, 5, 7);
        row.getSheet().addMergedRegion(mergedRegion);


        mergedRegion = new CellRangeAddress(0, 1, 8, 8);
        row.getSheet().addMergedRegion(mergedRegion);

        mergedRegion = new CellRangeAddress(0, 1, 9, 9);
        row.getSheet().addMergedRegion(mergedRegion);

        for (int i = 0; i < 10; i++) {
            cell = row.createCell(i);
            cell2 = secondRow.createCell(i);

            headerCellStyle.setBorderBottom(BorderStyle.THIN);
            headerCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
            headerCellStyle.setBorderLeft(BorderStyle.THIN);
            headerCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
            headerCellStyle.setBorderRight(BorderStyle.THIN);
            headerCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
            headerCellStyle.setBorderTop(BorderStyle.THIN);
            headerCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());

            cell.setCellStyle(headerCellStyle);
            cell2.setCellStyle(headerCellStyle);

            cell.setCellType(CellType.STRING);
            cell2.setCellType(CellType.STRING);


            if(i == 0) {
                cell.setCellValue("회사명");
            }

            if(i == 1) {
                cell.setCellValue("입찰계획");
                cell2.setCellValue("건수");
            }

            if(i == 2) {
                cell2.setCellValue("예산금액");
            }

            if(i == 3) {
                cell.setCellValue("입찰진행");
                cell2.setCellValue("건수");
            }

            if(i == 4) {
                cell.setCellValue("입찰진행");
                cell2.setCellValue("예산금액");
            }

            if(i == 5) {
                cell.setCellValue("입찰완료(유찰제외)");
                cell2.setCellValue("건수");
            }

            if(i == 6) {
                cell2.setCellValue("낙찰금액");
            }

            if(i == 7) {
                cell2.setCellValue("업체수/건수");
            }

            if(i == 8) {
                cell.setCellValue("등록업체수");
            }

            if(i == 9) {
                cell.setCellValue("기타");
            }
        }
    }

    @Override
    public void createbody(Class<?> clazz, SXSSFWorkbook workbook, List<?> data, Sheet sheet, Row row, Cell cell, int rowNo) throws IllegalAccessException {
        int startRow = rowNo + 1;
        /**
         * body font style
         */
        Font bodyFont = workbook.createFont();
        bodyFont.setBold(false);
        bodyFont.setFontHeightInPoints((short) 10);
        //font.setColor((short) 255);

        /**
         * body cell style
         */
        CellStyle bodyCellStyle = workbook.createCellStyle();
        bodyCellStyle.setAlignment(HorizontalAlignment.CENTER);       // 가로 가운데 정렬
        bodyCellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 세로 가운데 정렬
        bodyCellStyle.setFont(bodyFont);

        // 통화 서식 생성
        short currencyFormat = workbook.createDataFormat().getFormat("#,##0.00");


        List<Object> fields = (List<Object>) data;
        row = sheet.createRow(++startRow);
        for (int i = 0, fieldSize = fields.size(); i < fieldSize; i++) {
            cell = row.createCell(i);

            bodyCellStyle.setBorderBottom(BorderStyle.THIN);
            bodyCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
            bodyCellStyle.setBorderLeft(BorderStyle.THIN);
            bodyCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
            bodyCellStyle.setBorderRight(BorderStyle.THIN);
            bodyCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
            bodyCellStyle.setBorderTop(BorderStyle.THIN);
            bodyCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());



            cell.setCellStyle(bodyCellStyle);

            Object fieldValue = fields.get(i);
            if (fieldValue instanceof BigDecimal) {
                BigDecimal decimalValue = (BigDecimal) fieldValue;
                bodyCellStyle.setDataFormat(cell.getSheet().getWorkbook().createDataFormat().getFormat("#,##0")); // 숫자 서식을 설정합니다.

                // 셀에 값을 설정할 때 숫자 형식으로 설정합니다.
                cell.setCellType(CellType.NUMERIC);

                // 셀에 값을 설정합니다.
                cell.setCellValue(decimalValue.doubleValue());

                // 셀에 숫자 서식을 적용합니다.
                cell.setCellStyle(bodyCellStyle);
            } else {
                // 다른 형식의 데이터는 그대로 셀에 설정
                cell.setCellType(CellType.STRING);
                cell.setCellValue(CommonUtils.getString(fieldValue, " "));
            }
        }
        
    }

    @Override
    protected List<String> findHeaderNames(Class<?> clazz) {
        return super.findHeaderNames(clazz);
    }

    @Override
    protected List<Object> findFieldValue(Class<?> clazz, Object obj) throws IllegalAccessException {
        return super.findFieldValue(clazz, obj);
    }
}
