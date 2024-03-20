package iljin.framework.ebid.etc.util.common.excel.utils;

import com.opencsv.CSVWriter;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.common.excel.dto.BidProgressResponseDto;
import iljin.framework.ebid.etc.util.common.excel.repository.ExcelRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public final class ExcelUtils implements ExcelSupport {

    private static final int MAX_ROW = 10;
    private ExcelRepository excelRepository;


    /**
     * CSV 다운로드 10000건 이상
     */
    @Override
    public void downLoadCsv(Class<?> clazz, List<?> data, String fileName, HttpServletResponse response) {
        try {
            log.info("----------------CSV Write Start----------------");
            response.setContentType("text/csv; charset=UTF-8"); // Set the character encoding
            fileName = URLEncoder.encode("회원정보.csv", "UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + fileName + "\"");

            OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(),
                    StandardCharsets.UTF_8);
            writer.write("\uFEFF");
            CSVWriter csvWriter = new CSVWriter(writer);

            csvWriter.writeAll(listMemberString(clazz, data));
            csvWriter.close();
            writer.close();

        } catch (IOException e) {
            log.error("Excel Download Error Message = {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Excel 다운로드 10000건 미만
     */
    @Override
    public void downLoadExcel(Class<?> clazz, List<?> data, String fileName, HttpServletResponse response) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            int listSize = data.size();
            int start = 0;


            getWorkBook(clazz, workbook, start, findHeaderNames(clazz), data, listSize);

            response.setCharacterEncoding("UTF-8");  // UTF-8 설정 추가
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");



            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            workbook.close();

            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            log.error("Excel Download Error Message = {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private SXSSFWorkbook getWorkBook(Class<?> clazz, SXSSFWorkbook workbook, int rowIdx, List<String> headerNames, List<?> data, int maxSize) throws IllegalAccessException, IOException {
        Sheet sheet = workbook.createSheet("Sheet4"); // 엑셀 sheet 이름
        sheet.setDefaultColumnWidth(10); // 디폴트 너비 설정

        Row row = null;
        Cell cell = null;

        // 0, 5000, 10000, 15000, 20000 : 5000씩 증가됨
        //int rowNo = rowIdx % maxSize; // 0, 5000, 10000, 15000, 20000 : 5000씩 증가됨

        row = sheet.createRow(0);

        //엑셀 헤더 생성
        createHeaders(workbook, row, cell, headerNames);
        //엑셀 내용 생성
        createBody(clazz, workbook, data, sheet, row, cell, rowIdx);

        // 주기적인 flush 진행
        ((SXSSFSheet) sheet).flushRows(maxSize);

        return workbook;
    }

    private void createHeaders(SXSSFWorkbook workbook, Row row, Cell cell, List<String> headerNames) {
        /**
         * header font style
         */
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        //font.setColor((short) 255);

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
        headerCellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND );
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER );
        headerCellStyle.setFont(headerFont);


        for (int i = 0, size = headerNames.size(); i < size; i++) {
            cell = row.createCell(i);
            headerCellStyle.setBorderBottom(BorderStyle.THIN);
            headerCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
            headerCellStyle.setBorderLeft(BorderStyle.THIN);
            headerCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
            headerCellStyle.setBorderRight(BorderStyle.THIN);
            headerCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
            headerCellStyle.setBorderTop(BorderStyle.THIN);
            headerCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());


            cell.setCellStyle(headerCellStyle);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(headerNames.get(i));

        }
    }

    private void createBody(Class<?> clazz, SXSSFWorkbook workbook,List<?> data, Sheet sheet, Row row, Cell cell, int rowNo) throws IllegalAccessException, IOException {
        int startRow = 0;

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

        for (Object o : data) {
            List<Object> fields = findFieldValue(clazz, o);
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

                cell.setCellType(CellType.STRING);
                cell.setCellValue(String.valueOf(fields.get(i)));


                // 주기적인 flush 진행
                if (rowNo % MAX_ROW == 0) {
                    ((SXSSFSheet) sheet).flushRows(MAX_ROW);
                }
            }
        }
    }

    /**
     * 엑셀의 헤더 명칭을 찾는 로직
     */
    private List<String> findHeaderNames(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ExcelColumnName.class))
                .map(field -> field.getAnnotation(ExcelColumnName.class).name())
                .collect(Collectors.toList());
    }

    /**
     * 엑셀의 데이터의 값을 추출하는 메서드
     */
    private List<Object> findFieldValue(Class<?> clazz, Object obj) throws IllegalAccessException {
        List<Object> result = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            result.add(field.get(obj));
        }
        return result;
    }

    private void setCellValueByType(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellType(CellType.STRING);
            cell.setCellValue((String) value);
        } else if (value instanceof Double) {
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue((Double) value);
        } else if (value instanceof Integer) {
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof LocalDate) {
            DateTimeFormatter dateFormatter = null;
            cell.setCellValue(((LocalDate) value).format(dateFormatter));
        }  else {
            // 기타 데이터 타입에 대한 처리
            cell.setCellValue((String) value);
        }
    }

    /**
     * CSV에 사용하는 List
     */
    public List<String[]> listMemberString(Class<?> clazz, List<?> data) throws IllegalAccessException {

        List<String[]> listStrings = new ArrayList<>();

        //헤더 값 추출
        List<String> headerNames = findHeaderNames(clazz);

        // 헤더 이름을 리스트에 추가합니다.
        listStrings.add(headerNames.toArray(new String[0]));

        //데이터 값 추출
        for (Object o : data) {
            List<Object> fields = findFieldValue(clazz, o);
            String[] rowData = new String[8];
            for (int i = 0, fieldSize = fields.size(); i < fieldSize; i++) {

                rowData[i] = (fields.get(i) != null) ? fields.get(i).toString() : null;
            }
            listStrings.add(rowData);
        }
        return listStrings;
    }

    //전자입찰>입찰계획>입찰계획상세 Excel DownLoad
    public void createBidProgressDetailExcel(BidProgressResponseDto param, HttpServletResponse response) throws IOException {
        InputStream fis = getClass().getResourceAsStream("/menual/bidProgressDetail.xlsx");
        XSSFWorkbook xssfWorkBook = new XSSFWorkbook(fis);
        XSSFSheet xssfSheet = xssfWorkBook.getSheetAt(0);

        String fileName = param.getFileName();
        fileName = URLEncoder.encode(fileName,"UTF-8").replaceAll("\\+", "%20");

        Map<String, Object> result = param.getResult();                     //엑셀 모든 컬럼
        List<Map<String, Object>> custContent = param.getCustContent();     //입찰참가업체 컬럼
        List<Map<String, Object>> tableContent = param.getTableContent();   //직접등록시 내역사항에 들어가는 세부사항 컬럼
        List<Map<String, Object>> fileContent = param.getFileContent();     //파일등록시 내역사항에 들어가는 파일리스트 컬럼

        /*
         *  입찰참가 업체 컬럼 생성
         */
        StringBuilder custNameBuilder = new StringBuilder();
        int index = 0;

        for(Map<String, Object> map : custContent) {
            custNameBuilder.append(map.get("custName"));
            // 다음 요소가 있으면 쉼표 추가
            if (++index < custContent.size()) {
                custNameBuilder.append(", ");
            }
        }

        /*
         *  Flag : 직접등록, 파일등록
         *  직접등록시 내역사항에 세부사항   들어감
         *  파일등록시 내역사항에 파일리스트 들어감
         */
        String insModeFlag = String.valueOf(result.get("insMode"));
        StringBuilder insModeContentBuilder = new StringBuilder();

        //단가에 통화 포멧팅
        DecimalFormat decimalFormat = new DecimalFormat("#,###");

        if("직접입력".equals(insModeFlag)) {
            //직접등록시 내역사항에 들어가는 세부사항
            for(Map<String, Object> map : tableContent) {
                insModeContentBuilder.append(map.get("name"));
                insModeContentBuilder.append("     ");
                insModeContentBuilder.append(map.get("ssize"));
                insModeContentBuilder.append("     ");
                insModeContentBuilder.append(map.get("orderQty"));
                insModeContentBuilder.append(map.get("unitcode"));
                insModeContentBuilder.append("     ");

                //단가에 통화 포멧팅
                String orderUc = decimalFormat.format(map.get("orderUc"));
                insModeContentBuilder.append("\\");
                insModeContentBuilder.append(orderUc);
                insModeContentBuilder.append("\n");
            }
        } else {
            //파일등록시 내역사항에 들어가는 파일리스트
            index = 0;
            for(Map<String, Object> map : fileContent) {
                insModeContentBuilder.append(map.get("fileNm"));
                // 다음 요소가 있으면 공백 추가
                if (++index < fileContent.size()) {
                    insModeContentBuilder.append("\n");
                }
            }
        }

        /*
         ** 입찰 기본정보
         */
        String biNo         = CommonUtils.getString(result.get("biNo"), "");             //입찰번호
        String biName       = CommonUtils.getString(result.get("biName"), "");           //입찰명
        String itemName     = CommonUtils.getString(result.get("itemName"), "");         //품목
        String biMode       = CommonUtils.getString(result.get("biMode"), "");           //입찰방식
        String bidJoinSpec  = CommonUtils.getString(result.get("bidJoinSpec"), "");      //입찰참가자격
        String specialCond  = CommonUtils.getString(result.get("specialCond"), "");      //특수조건
        String spotDate     = CommonUtils.getString(result.get("spotDate"), "");         //현장설명일시
        String spotArea     = CommonUtils.getString(result.get("spotArea"), "");         //현장설명장소
        String succDeciMeth = CommonUtils.getString(result.get("succDeciMeth"), "");     //낙찰자결정방법
        String custName     = CommonUtils.getString(custNameBuilder, "");                //입찰참가업체
        String amtBasis     = CommonUtils.getString(result.get("amtBasis"), "");         //금액기준
        String payCond      = CommonUtils.getString(result.get("payCond"), "");          //결제조건
        String bdAmt        = CommonUtils.getString(result.get("bdAmt"), "");            //예산금액
        String cuser        = CommonUtils.getString(result.get("cuser"), "");            //입찰담당자

        /*
         ** 입찰 공고 추가등록 사항
         */
        String estStartDate    = CommonUtils.getString(result.get("estStartDate"), "");  //제출 시작일시
        String estCloseDate    = CommonUtils.getString(result.get("estCloseDate"), "");  //제출 마감일시
        String estOpener       = CommonUtils.getString(result.get("estOpener"), "");     //개찰자
        String gongoId         = CommonUtils.getString(result.get("gongoId"), "");       //입찰 공고자
        String estBidder       = CommonUtils.getString(result.get("estBidder"), "");     //낙찰자
        String openAtt1        = CommonUtils.getString(result.get("openAtt1"), "");      //입회자1
        String openAtt2        = CommonUtils.getString(result.get("openAtt2"), "");      //입회자2
        String supplyCond      = CommonUtils.getString(result.get("supplyCond"), "");    //납품 조건
        String insMode         = CommonUtils.getString(result.get("insMode"), "");       //내역 방식
        String insModeContent  = CommonUtils.getString(insModeContentBuilder, "");       //내역사항

        /*
        ** 입찰 기본정보
         */
        xssfSheet.getRow(3).getCell(1).setCellValue(biNo);          //입찰번호
        xssfSheet.getRow(4).getCell(1).setCellValue(biName);        //입찰명
        xssfSheet.getRow(5).getCell(1).setCellValue(itemName);      //품목
        xssfSheet.getRow(6).getCell(1).setCellValue(biMode);        //입찰방식
        xssfSheet.getRow(7).getCell(1).setCellValue(bidJoinSpec);   //입찰참가자격
        xssfSheet.getRow(8).getCell(1).setCellValue(specialCond);   //특수조건
        xssfSheet.getRow(9).getCell(1).setCellValue(spotDate);      //현장설명일시
        xssfSheet.getRow(10).getCell(1).setCellValue(spotArea);     //현장설명장소
        xssfSheet.getRow(11).getCell(1).setCellValue(succDeciMeth); //낙찰자결정방법
        xssfSheet.getRow(12).getCell(1).setCellValue(custName);     //입찰참가업체
        xssfSheet.getRow(13).getCell(1).setCellValue(amtBasis);     //금액기준
        xssfSheet.getRow(14).getCell(1).setCellValue(payCond);      //결제조건
        xssfSheet.getRow(15).getCell(1).setCellValue(bdAmt);        //예산금액
        xssfSheet.getRow(16).getCell(1).setCellValue(cuser);        //입찰담당자
       /*
       ** 입찰 공고 추가등록 사항
        */
        xssfSheet.getRow(19).getCell(1).setCellValue(estStartDate);  //제출시작일시
        xssfSheet.getRow(20).getCell(1).setCellValue(estCloseDate);  //제출마감일시
        xssfSheet.getRow(21).getCell(1).setCellValue(estOpener);     //개찰자
        xssfSheet.getRow(22).getCell(1).setCellValue(gongoId);       //입찰공고자
        xssfSheet.getRow(23).getCell(1).setCellValue(estBidder);     //낙찰자
        xssfSheet.getRow(24).getCell(1).setCellValue(openAtt1);      //입회자1
        xssfSheet.getRow(25).getCell(1).setCellValue(openAtt2);      //입회자2
        xssfSheet.getRow(26).getCell(1).setCellValue(supplyCond);    //납품조건
        xssfSheet.getRow(27).getCell(1).setCellValue(insMode);       //내역방식
        xssfSheet.getRow(28).getCell(1).setCellValue(insModeContent);//내역사항

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");

        ServletOutputStream outputStream = response.getOutputStream();
        xssfWorkBook.write(outputStream);
        xssfWorkBook.close();
        outputStream.flush();
        outputStream.close();
    }
}





