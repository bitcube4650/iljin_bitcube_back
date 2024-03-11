package iljin.framework.ebid.etc.util.common.excel.utils;

import com.opencsv.CSVWriter;
import iljin.framework.ebid.etc.util.common.excel.repository.ExcelRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
}





