package iljin.framework.ebid.etc.util.common.excel.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.opencsv.CSVWriter;

import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.common.excel.concreteExcelGenerator.ConcreteBiInfoDetailList;
import iljin.framework.ebid.etc.util.common.excel.concreteExcelGenerator.ConcreteBidCompleteList;
import iljin.framework.ebid.etc.util.common.excel.concreteExcelGenerator.ConcreteBidPresentList;
import iljin.framework.ebid.etc.util.common.excel.concreteExcelGenerator.ConcreteBiddingDetail;
import iljin.framework.ebid.etc.util.common.excel.concreteExcelGenerator.ConcreteCompanyBidPerformance;
import iljin.framework.ebid.etc.util.common.excel.dto.BidProgressResponseDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public final class ExcelUtils implements ExcelSupport {

    private static final int MAX_ROW = 1000;

    @Autowired
    private ConcreteCompanyBidPerformance concreteCompanyBidPerformance;
    @Autowired
    private ConcreteBiInfoDetailList concreteBiInfoDetailList;
    @Autowired
    private ConcreteBiddingDetail excelBiddingDetail;
    @Autowired
    private ConcreteBidPresentList concreteBidPresentList;
    @Autowired
    private ConcreteBidCompleteList concreteBidCompleteList;


    /**
     * CSV 다운로드
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
     * Excel 다운로드
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

    //통계>>입찰이력
    public void downLoadBidCompleteList(Class<?> clazz, Map<String, Object> param, String fileName, HttpServletResponse response) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            concreteBidCompleteList.getWorkBookPaging(clazz, workbook, findHeaderNames(clazz), param);

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


    public void downLoadExcelCompanyBidPerformance(Class<?> clazz, Map<String, Object> param, String fileName, HttpServletResponse response) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            concreteCompanyBidPerformance.getWorkBookPaging(clazz, workbook, findHeaderNames(clazz), param);

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

    /**
     * Excel 다운로드 Pagination 적용.
     */

    public void downLoadExcelBiddingDetail(Class<?> clazz, Map<String, Object> param, String fileName, @NotNull HttpServletResponse response) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            excelBiddingDetail.getWorkBookPaging(clazz, workbook, findHeaderNames(clazz), param);

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

    public void downLoadExcelBiddingStatus(Class<?> clazz, Map<String, Object> param, String fileName, @NotNull HttpServletResponse response) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            concreteBidPresentList.getWorkBookPaging(clazz, workbook, findHeaderNames(clazz), param);

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
        }
    }

    /**
     * Excel 다운로드 Pagination 적용.
     */

    public void downLoadExcelBiInfoDetailList(Class<?> clazz, Map<String, Object> params, String fileName, HttpServletResponse response) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            concreteBiInfoDetailList.getWorkBookPaging(clazz, workbook, findHeaderNames(clazz), params);

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

        row = sheet.createRow(0);
        //엑셀 해더 생성
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
        //headerFont.setBold(true);
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
        headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
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
        int startRow = 1;

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

                Object fieldValue = fields.get(i);
                if (fieldValue instanceof BigDecimal) {
                    // BigDecimal 값을 통화 형식으로 변환하여 셀에 설정
                    BigDecimal decimalValue = (BigDecimal) fieldValue;
                    String currencyValue = NumberFormat.getCurrencyInstance().format(decimalValue.doubleValue());
                    cell.setCellValue(currencyValue);
                } else {
                    // 다른 형식의 데이터는 그대로 셀에 설정
                    cell.setCellType(CellType.STRING);
                    cell.setCellValue(CommonUtils.getString(fieldValue, " "));
                }

                // 주기적인 flush 진행
                if (rowNo % MAX_ROW == 0) {
                    ((SXSSFSheet) sheet).flushRows(MAX_ROW);
                }

            }
        }
    }
    private void createBodyPaging(Class<?> clazz, SXSSFWorkbook workbook,List<?> data, Sheet sheet,
                                  Row row, Cell cell, int rowNo, boolean commonheaders) throws IllegalAccessException, IOException {
        int startRow = rowNo + 1;

        //헤더가 공통이면
        if(commonheaders) {
            startRow = rowNo;
        }

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

                Object fieldValue = fields.get(i);
                if (fieldValue instanceof BigDecimal) {
                    // BigDecimal 값을 통화 형식으로 변환하여 셀에 설정
                    BigDecimal decimalValue = (BigDecimal) fieldValue;
                    String currencyValue = NumberFormat.getCurrencyInstance().format(decimalValue.doubleValue());
                    cell.setCellValue(currencyValue);
                } else {
                    // 다른 형식의 데이터는 그대로 셀에 설정
                    cell.setCellType(CellType.STRING);
                    cell.setCellValue(CommonUtils.getString(fieldValue, " "));
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
            String[] rowData = new String[fields.size()];
            for (int i = 0, fieldSize = fields.size(); i < fieldSize; i++) {

                rowData[i] = (fields.get(i) != null) ? fields.get(i).toString() : null;
            }
            listStrings.add(rowData);
        }
        return listStrings;
    }

    //전자입찰>입찰계획>입찰계획상세 Excel DownLoad
    public void createBidProgressDetailExcel(BidProgressResponseDto param, HttpServletResponse response) throws IOException {
    	Map<String, Object> result = param.getResult();                     //엑셀 모든 컬럼
        List<Map<String, Object>> custContent = param.getCustContent();     //입찰참가업체 컬럼
        List<Map<String, Object>> tableContent = param.getTableContent();   //직접등록시 내역사항에 들어가는 세부사항 컬럼
        List<Map<String, Object>> fileContent = param.getFileContent();     //파일등록시 내역사항에 들어가는 파일리스트 컬럼
        InputStream fis = null;
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        
        String insModeFlag = String.valueOf(result.get("insMode"));
        
        if("직접입력".equals(insModeFlag)) {
        	fis = getClass().getResourceAsStream("/menual/bidProgressItemDetail.xlsx");
        }else {
        	fis = getClass().getResourceAsStream("/menual/bidProgressDetail.xlsx");
        }
    	
        XSSFWorkbook xssfWorkBook = new XSSFWorkbook(fis);
        XSSFSheet xssfSheet = xssfWorkBook.getSheetAt(0);

        String fileName = param.getFileName();
        fileName = URLEncoder.encode(fileName,"UTF-8").replaceAll("\\+", "%20");

        
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
        
        StringBuilder insModeContentBuilder = new StringBuilder();

        if(!"직접입력".equals(insModeFlag)) {//파일입력인 경우
            insModeContentBuilder.append(fileContent.stream()
                    .filter(map -> "K".equals(map.get("fileFlag")))
                    .collect(Collectors.toList()).get(0).get("fileNm"));
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
        String custName     = CommonUtils.getString(custNameBuilder, "가입회원사 전체"); //입찰참가업체 -> 일반경쟁입찰 일 경우, default로 가입회원사 전체
        String amtBasis     = CommonUtils.getString(result.get("amtBasis"), "");         //금액기준
        String payCond      = CommonUtils.getString(result.get("payCond"), "");          //결제조건
        
        String bdAmt        = CommonUtils.getString(result.get("bdAmt"), "");//예산금액
        
        if (StringUtils.isNotBlank(bdAmt)) {
            BigInteger amount = new BigInteger(bdAmt);
            bdAmt = CommonUtils.getString(decimalFormat.format(amount), "");
        }
        
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
        String insModeContent  = CommonUtils.getString(insModeContentBuilder, "");       //세부내역

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
        
        if("직접입력".equals(insModeFlag)) {
        	//셀 테두리 얇은선으로 설정
        	CellStyle cellStyle = xssfWorkBook.createCellStyle();
        	cellStyle.setBorderTop(BorderStyle.THIN);
        	cellStyle.setBorderBottom(BorderStyle.THIN);
        	cellStyle.setBorderLeft(BorderStyle.THIN);
        	cellStyle.setBorderRight(BorderStyle.THIN);
        	cellStyle.setAlignment(HorizontalAlignment.CENTER);
        	
        	int startIdx = 29;
        	//직접등록시 내역사항에 들어가는 세부사항
            for(Map<String, Object> map : tableContent) {
            	Row newRow = xssfSheet.createRow(startIdx);
            	
            	String name = CommonUtils.getString(map.get("name"), "");//품목명
            	String ssize = CommonUtils.getString(map.get("ssize"), "");//규격
            	String unitcode = CommonUtils.getString(map.get("unitcode"), "");//단위
            	BigInteger orderUc =  BigInteger.valueOf((int)map.get("orderUc"));//실행단가
            	BigInteger orderQty = BigInteger.valueOf((int)map.get("orderQty"));//수량
            	String strOrderUc = decimalFormat.format(orderUc);
            	BigInteger  itemTotal = orderQty.multiply(orderUc) ;//합계
            	String strItemTotal = decimalFormat.format(itemTotal);

            	
            	Cell cell1 = newRow.createCell(1);
            	cell1.setCellValue(name);
            	Cell cell2 = newRow.createCell(2);
            	cell2.setCellValue(ssize);
            	Cell cell3 = newRow.createCell(3);
            	cell3.setCellValue(unitcode);
            	Cell cell4 = newRow.createCell(4);
            	cell4.setCellValue(strOrderUc);
            	Cell cell5 = newRow.createCell(5);
            	cell5.setCellValue(decimalFormat.format(orderQty));
            	Cell cell6 = newRow.createCell(6);
            	cell6.setCellValue(strItemTotal);
            	
            	//셀 테두리 적용
            	cell1.setCellStyle(cellStyle);
            	cell2.setCellStyle(cellStyle);
            	cell3.setCellStyle(cellStyle);
            	
            	//밑에 3개의 셀만 숫자라서 오른쪽 정렬
            	CellStyle cellStyleRight = xssfWorkBook.createCellStyle();
            	cellStyleRight.setBorderTop(BorderStyle.THIN);
            	cellStyleRight.setBorderBottom(BorderStyle.THIN);
            	cellStyleRight.setBorderLeft(BorderStyle.THIN);
            	cellStyleRight.setBorderRight(BorderStyle.THIN);
            	cellStyleRight.setAlignment(HorizontalAlignment.RIGHT);
            	cell4.setCellStyle(cellStyleRight);
            	cell5.setCellStyle(cellStyleRight);
            	cell6.setCellStyle(cellStyleRight);
            	
            	//내역사항 셀 밑에 테두리 두께 적용
            	if(startIdx == 28+tableContent.size()) {
            		CellStyle cellStyle2 = xssfWorkBook.createCellStyle();
                    cellStyle2.setBorderBottom(BorderStyle.THIN);
            		Cell cell = newRow.createCell(0);
            		cell.setCellStyle(cellStyle2);
            	}
            	
            	startIdx++;
            }
            
            //내역사항 셀 세로 합치기
            CellRangeAddress mergedRegion = new CellRangeAddress(28, 28+tableContent.size(), 0, 0); 
            xssfSheet.addMergedRegion(mergedRegion); 

           	List<Map<String,Object>> innerFileList = fileContent.stream()
                    .filter(map -> "0".equals(map.get("fileFlag")))
                    .collect(Collectors.toList());
        	
        	List<Map<String,Object>> outerFileList = fileContent.stream()
                    .filter(map -> "1".equals(map.get("fileFlag")))
                    .collect(Collectors.toList());
        	
        	if(innerFileList.size() > 0 || outerFileList.size() > 0) {
    	        StringBuilder fileNmBuilder = new StringBuilder();
    	        
    	        int lastRowNum = xssfSheet.getLastRowNum() + 1; 
    	        Row newRow = xssfSheet.createRow(lastRowNum); 

    	        Cell cell0 = newRow.createCell(0);
    	        Cell cell1 = newRow.createCell(1);
       	        Cell cell2 = newRow.createCell(2);
       	        Cell cell3 = newRow.createCell(3);
       	        Cell cell4 = newRow.createCell(4);
       	        Cell cell5 = newRow.createCell(5);
       	        Cell cell6 = newRow.createCell(6);
    	        
    	        cell0.setCellValue("첨부파일");

        		if(innerFileList.size() > 0) {
        			fileNmBuilder.append("대내용\n");
        	        for(Map<String, Object> map : innerFileList) {
        	        	fileNmBuilder.append(map.get("fileNm"));
        	        	fileNmBuilder.append("\n");
        	        }

        		}
        		
        		if(outerFileList.size() > 0) {
        			fileNmBuilder.append( innerFileList.size() > 0 ? "\n대외용\n" : "대외용\n");
        	        for(Map<String, Object> map : outerFileList) {
        	        	fileNmBuilder.append(map.get("fileNm"));
        	        	fileNmBuilder.append("\n");
        	        }
        		}
        		
        		RichTextString richText = xssfWorkBook.getCreationHelper().createRichTextString(fileNmBuilder.toString());

        		cell1.setCellValue(CommonUtils.getString(richText, ""));
        		xssfSheet.addMergedRegion(new CellRangeAddress(lastRowNum, lastRowNum, 1, 6));
        		CellStyle style = xssfWorkBook.createCellStyle();
        		style.setWrapText(true);
        		style.setBorderLeft(BorderStyle.THIN);
        		style.setBorderRight(BorderStyle.THIN);
        		style.setBorderTop(BorderStyle.THIN);
        		style.setBorderBottom(BorderStyle.THIN);
        		style.setVerticalAlignment(VerticalAlignment.TOP);
        		cell0.setCellStyle(style);
        		cell1.setCellStyle(style);
        		cell2.setCellStyle(style);
        		cell3.setCellStyle(style);
        		cell4.setCellStyle(style);
        		cell5.setCellStyle(style);
        		cell6.setCellStyle(style);
        		newRow.setHeightInPoints(409.60f);
        	}
        		
            
        }else {
        	xssfSheet.getRow(28).getCell(1).setCellValue(insModeContent);//세부내역
        	List<Map<String,Object>> innerFileList = fileContent.stream()
                    .filter(map -> "0".equals(map.get("fileFlag")))
                    .collect(Collectors.toList());
        	
        	List<Map<String,Object>> outerFileList = fileContent.stream()
                    .filter(map -> "1".equals(map.get("fileFlag")))
                    .collect(Collectors.toList());
        	
        	if(innerFileList.size() > 0 || outerFileList.size() > 0) {
    	        StringBuilder fileNmBuilder = new StringBuilder();
    	       // xssfSheet.getRow(29).createCell(0).setCellValue("첨부파일");
    			
    	        
    	        Row row29 = xssfSheet.getRow(29);
    	        if (row29 == null) {
    	            row29 = xssfSheet.createRow(29);
    	        }
    	        Cell cell0 = row29.getCell(0);
    	        if (cell0 == null) {
    	            cell0 = row29.createCell(0);
    	        }
    	        cell0.setCellValue("첨부파일");
    	        
    	        
        		if(innerFileList.size() > 0) {
        			fileNmBuilder.append("대내용\n");
        	        for(Map<String, Object> map : innerFileList) {
        	        	fileNmBuilder.append(map.get("fileNm"));
        	        	fileNmBuilder.append("\n");
        	        }

        		}
        		
        		if(outerFileList.size() > 0) {
        			fileNmBuilder.append( innerFileList.size() > 0 ? "\n대외용\n" : "대외용\n");
        	        for(Map<String, Object> map : outerFileList) {
        	        	fileNmBuilder.append(map.get("fileNm"));
        	        	fileNmBuilder.append("\n");
        	        }
        		}
        		Cell cell = row29.createCell(1);
        		Cell cell2 = row29.createCell(2);
				Cell cell3 = row29.createCell(3);
				Cell cell4 = row29.createCell(4);
				Cell cell5 = row29.createCell(5);
				Cell cell6 = row29.createCell(6);
				Cell cell7 = row29.createCell(7);
				Cell cell8 = row29.createCell(8);
				Cell cell9 = row29.createCell(9);
				Cell cell10 = row29.createCell(10);				
        		
        		RichTextString richText = xssfWorkBook.getCreationHelper().createRichTextString(fileNmBuilder.toString());
        		

        		row29.getCell(1).setCellValue(CommonUtils.getString(richText, ""));
        		xssfSheet.addMergedRegion(new CellRangeAddress(29, 29, 1, 10));
        		CellStyle style = xssfWorkBook.createCellStyle();
        		style.setWrapText(true);
        		style.setBorderLeft(BorderStyle.THIN);
        		style.setBorderRight(BorderStyle.THIN);
        		style.setBorderTop(BorderStyle.THIN);
        		style.setBorderBottom(BorderStyle.THIN);
        		style.setVerticalAlignment(VerticalAlignment.TOP);
        		cell0.setCellStyle(style);
        		cell.setCellStyle(style);
        		cell2.setCellStyle(style);
        		cell3.setCellStyle(style);
        		cell4.setCellStyle(style);
        		cell5.setCellStyle(style);
        		cell6.setCellStyle(style);
        		cell7.setCellStyle(style);
        		cell8.setCellStyle(style);
        		cell9.setCellStyle(style);
        		cell10.setCellStyle(style);
        		row29.setHeightInPoints(409.60f);

        	}
        	
        }

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




