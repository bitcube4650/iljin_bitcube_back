package iljin.framework.ebid.etc.statistics.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.etc.statistics.service.StatisticsService;
import iljin.framework.ebid.etc.util.GeneralDao;

@RestController
@RequestMapping("/api/v1/statistics")
@CrossOrigin
public class StatisticsContoller {

	@Autowired
    private StatisticsService statisticsService;
		
    @Autowired
    GeneralDao generalDao;
    
	//계열사 리스트 조회
	
	@PostMapping("/coInterList")
	public ResultBody selectCoInterList(@RequestBody Map<String, Object> params) throws Exception {

		return statisticsService.selectCoInterList(params);
	}
	
	
	//회사별 입찰실적 리스트 조회
	@PostMapping("/biInfoList")
	public ResultBody selectBiInfoList(@RequestBody Map<String, Object> params) throws Exception {

		return statisticsService.selectBiInfoList(params);
	}
	
	/**
	 * 입찰실적 상세내역 리스트
	 * @param params
	 * @return
	 */
	@PostMapping("/biInfoDetailList")
	public ResultBody biInfoDetailList(@RequestBody Map<String, Object> params) {

		return statisticsService.biInfoDetailList(params);
	}
	
	
	/**
	 * 입찰현황
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/bidPresentList")
	public ResultBody bidPresentList(@RequestBody Map<String, Object> params) throws Exception {

		return statisticsService.bidPresentList(params);
	}

	/**
	 * 입찰 상세내역 리스트
	 * @param params
	 * @return
	 */
	@PostMapping("/bidDetailList")
	public ResultBody bidDetailList(@RequestBody Map<String, Object> params) {

		return statisticsService.bidDetailList(params);
	}

	
    @PostMapping("/excel")
    public void downloadExcel(HttpServletResponse response, @RequestBody Map<String, Object> params) throws Exception {

        List<String> columnNames = null;
        List<String> mappingColumnNames = (List<String>) params.get("mappingColumnNames");
        List<Map<String, Object>> data = new ArrayList<>();

        List<Object> excelDataList = generalDao.selectGernalList("statistics." + params.get("excelUrl"), params);

        for (Object excelDataObject : excelDataList) {
            Map<String, Object> row = (Map<String, Object>) excelDataObject;
            data.add(row);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + params.get("fileName") + ".xlsx\"");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");
        
        Boolean isMergeColumns = params.containsKey("mergeColumns");
        

        if(!isMergeColumns) {
        	columnNames = (List<String>) params.get("columnNames");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnNames.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnNames.get(i));
            }
        }else {
        	List<Object> mergeColumns = (List<Object>) params.get("mergeColumns");
        	int startCol = 0; // 열의 시작 위치

        	for (Object mergeColumn : mergeColumns) {
        	    if (mergeColumn instanceof String) {
        	        // String인 경우, 첫 번째 행과 두 번째 행을 병합합니다.
        	        String columnName = (String) mergeColumn;
        	        Row row = sheet.getRow(0);
        	        if (row == null) {
        	            row = sheet.createRow(0);
        	        }
        	        Cell cell = row.createCell(startCol);
        	        cell.setCellValue(columnName);
        	        
        	        // 첫 번째 행과 두 번째 행을 병합합니다.
        	        sheet.addMergedRegion(new CellRangeAddress(0, 1, startCol, startCol));
        	        startCol++; // 다음 열로 이동
        	    } else if (mergeColumn instanceof List) {
        	        // List인 경우, 해당 열에 값을 채워넣습니다.
        	        List<?> subList = (List<?>) mergeColumn;
        	        String columnName = (String) subList.get(0);
        	        List<String> subColumnNames = (List<String>) subList.get(1);

        	        int mergeColumnSize = subColumnNames.size();

        	        // 열을 병합합니다.
        	        sheet.addMergedRegion(new CellRangeAddress(0, 0, startCol, startCol + mergeColumnSize - 1));

        	        // 열의 제목을 입력합니다.
        	        Row row = sheet.getRow(0);
        	        if (row == null) {
        	            row = sheet.createRow(0);
        	        }
        	        Cell cell = row.createCell(startCol);
        	        cell.setCellValue(columnName);

        	        // 서브 열의 제목을 입력합니다.
        	        for (int i = 0; i < subColumnNames.size(); i++) {
        	            String subColumnName = subColumnNames.get(i);
        	            row = sheet.getRow(1);
        	            if (row == null) {
        	                row = sheet.createRow(1);
        	            }
        	            cell = row.createCell(startCol + i);
        	            cell.setCellValue(subColumnName);
        	        }

        	        startCol += mergeColumnSize; // 다음 열로 이동
        	    }
        	}
        }
        

        //병합할 컬럼이 없으면 데이터 표시를 로우를 1부터, 있으면 데이터 표시 로우를 2부터.
        int rowNum = !isMergeColumns ? 1 : 2 ;

        for (Map<String, Object> dataMap : data) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < mappingColumnNames.size(); i++) {
                Cell cell = row.createCell(i);
                Object value = dataMap.get(mappingColumnNames.get(i));
                if (value instanceof String) {
                    cell.setCellValue((String) value);
                } else if (value instanceof Integer) {
                    cell.setCellValue((Integer) value);
                } else if (value instanceof Double) {
                    cell.setCellValue((Double) value);
                } else if (value instanceof Boolean) {
                    cell.setCellValue((Boolean) value);
                } else if (value != null) {
                    cell.setCellValue(value.toString());
                }
            }
        }

        for (int i = 0; i < mappingColumnNames.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Write the output to the response's output stream
        workbook.write(response.getOutputStream());
        workbook.close();
    }
	
}
