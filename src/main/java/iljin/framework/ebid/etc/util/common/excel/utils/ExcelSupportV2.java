package iljin.framework.ebid.etc.util.common.excel.utils;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ExcelSupportV2 {

	//추상메서드 선언
	public abstract SXSSFWorkbook getWorkBookPaging(Class<?> clazz, SXSSFWorkbook workbook, List<String> headerNames, Map<String, Object> param) throws IllegalAccessException, IOException;
	public abstract SXSSFWorkbook getWorkBookPaging(String interrelatedCustCode, SXSSFWorkbook workbook, List<String> headerNames, Map<String, Object> param) throws IllegalAccessException, IOException;

	public abstract void createHeaders(SXSSFWorkbook workbook, Row row, Cell cell, List<String> headerNames);
	public abstract void createbody(Class<?> clazz, SXSSFWorkbook workbook,List<?> data, Sheet sheet,
									Row row, Cell cell, int rowNo) throws IllegalAccessException;
	
	public abstract void createbody(SXSSFWorkbook workbook,List<?> data, Sheet sheet,
			Row row, Cell cell, int rowNo) throws IllegalAccessException;

	//일반 메서드 정의

	/**
	 * 엑셀의 헤더 명칭을 찾는 로직
	 */
	protected List<String> findHeaderNames(Class<?> clazz) {
		return Arrays.stream(clazz.getDeclaredFields())
				.filter(field -> field.isAnnotationPresent(ExcelColumnName.class))
				.map(field -> field.getAnnotation(ExcelColumnName.class).name())
				.collect(Collectors.toList());
	}


	/**
	 * 엑셀의 데이터의 값을 추출하는 메서드
	 */
	protected List<Object> findFieldValue(Class<?> clazz, Object obj) throws IllegalAccessException {
		List<Object> result = new ArrayList<>();
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			result.add(field.get(obj));
		}
		return result;
	}
}
