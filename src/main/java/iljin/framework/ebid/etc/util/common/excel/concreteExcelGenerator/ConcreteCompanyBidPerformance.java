package iljin.framework.ebid.etc.util.common.excel.concreteExcelGenerator;

import java.io.IOException;
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
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.GeneralDao;
import iljin.framework.ebid.etc.util.common.consts.DB;
import iljin.framework.ebid.etc.util.common.excel.dto.CompanyBidPerformanceExcelDto;
import iljin.framework.ebid.etc.util.common.excel.repository.ExcelRepository;
import iljin.framework.ebid.etc.util.common.excel.utils.ExcelSupportV2;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ConcreteCompanyBidPerformance extends ExcelSupportV2 {

	@Autowired
	ExcelRepository excelRepository;
	@Autowired
	GeneralDao generalDao;

	@SuppressWarnings("unchecked")
	@Override
	public SXSSFWorkbook getWorkBookPaging(Class<?> clazz, SXSSFWorkbook workbook, List<String> headerNames, Map<String, Object> param) throws IllegalAccessException, IOException {
		Sheet sheet = workbook.createSheet("Sheet1"); // 엑셀 sheet 이름
		sheet.setDefaultColumnWidth(12); // 디폴트 너비 설정

		Row row = null;
		Cell cell = null;
		row = sheet.createRow(0);

		//엑셀 헤더 작성
		createHeaders(workbook, row, cell, headerNames);

		//엑셀 바디 작성
		List<Object> list = new ArrayList<>();

		int offset = 0;
		int limit = 1000;

		try {
			int biInfoListCnt = CommonUtils.getInt(generalDao.selectGernalCount(DB.QRY_SELECT_BI_INFO_LIST_COUNT, param));

			while (offset < biInfoListCnt) {
				list = generalDao.selectGernalList(DB.QRY_SELECT_BI_INFO_LIST, param);
				
				List<CompanyBidPerformanceExcelDto> excelData = new ArrayList<>();
				for(Object obj : list) {
					Map<String,Object> map = (Map<String, Object>) obj;
					
					CompanyBidPerformanceExcelDto companyBidPerformanceExcelDto = new CompanyBidPerformanceExcelDto();
					
					String interrelatedNm = CommonUtils.getString(map.get("interrelatedNm"), "");
					String cnt = CommonUtils.getString(map.get("cnt"), "");
					BigDecimal bdAnt = (BigDecimal) map.get("bdAmt");
					BigDecimal succAmt = (BigDecimal) map.get("succAmt");
					BigDecimal mamt = (BigDecimal) map.get("MAmt");
					
					companyBidPerformanceExcelDto.setInterrelatedNm(interrelatedNm);
					companyBidPerformanceExcelDto.setCnt(CommonUtils.getFormatNumber(cnt));
					companyBidPerformanceExcelDto.setBdAmt(bdAnt);
					companyBidPerformanceExcelDto.setSuccAmt(succAmt);
					companyBidPerformanceExcelDto.setMAmt(mamt);
					
					excelData.add(companyBidPerformanceExcelDto);
				}

				int start = offset;

				createbody(clazz, workbook, excelData, sheet, row, cell, start);

				offset += limit;

				excelData.clear();
				((SXSSFSheet) sheet).flushRows(limit);
			}
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
		// headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 11);
		// font.setColor((short) 255);

		/**
		 * header cell style
		 */
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setAlignment(HorizontalAlignment.CENTER); // 가로 가운데 정렬
		headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 세로 가운데 정렬
		headerCellStyle.setFont(headerFont);

		// 테두리 설정
		headerCellStyle.setBorderLeft(BorderStyle.MEDIUM);
		headerCellStyle.setBorderRight(BorderStyle.MEDIUM);
		headerCellStyle.setBorderTop(BorderStyle.MEDIUM);
		headerCellStyle.setBorderBottom(BorderStyle.MEDIUM);

		// 배경 설정
		headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
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

	@Override
	public void createbody(Class<?> clazz, SXSSFWorkbook workbook, List<?> data, Sheet sheet, Row row, Cell cell,
			int rowNo) throws IllegalAccessException {
		int startRow = rowNo;
		/**
		 * body font style
		 */
		Font bodyFont = workbook.createFont();
		bodyFont.setBold(false);
		bodyFont.setFontHeightInPoints((short) 10);
		// font.setColor((short) 255);

		/**
		 * body cell style
		 */
		CellStyle bodyCellStyle = workbook.createCellStyle();
		bodyCellStyle.setAlignment(HorizontalAlignment.CENTER); // 가로 가운데 정렬
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

				Object fieldValue = fields.get(i);
				if (fieldValue instanceof BigDecimal) {
					BigDecimal decimalValue = (BigDecimal) fieldValue;
					bodyCellStyle.setDataFormat(cell.getSheet().getWorkbook().createDataFormat().getFormat("#,##0")); // 숫자
																														// 서식을
																														// 설정합니다.

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
	}

	@Override
	protected List<String> findHeaderNames(Class<?> clazz) {
		return super.findHeaderNames(clazz);
	}

	@Override
	protected List<Object> findFieldValue(Class<?> clazz, Object obj) throws IllegalAccessException {
		return super.findFieldValue(clazz, obj);
	}

	@Override
	public SXSSFWorkbook getWorkBookPaging(String interrelatedCustCode, SXSSFWorkbook workbook,
			List<String> headerNames, Map<String, Object> param) throws IllegalAccessException, IOException {
		return null;
	}

	@Override
	public void createbody(SXSSFWorkbook workbook, List<?> data, Sheet sheet, Row row, Cell cell, int rowNo)
			throws IllegalAccessException {

	}
}
