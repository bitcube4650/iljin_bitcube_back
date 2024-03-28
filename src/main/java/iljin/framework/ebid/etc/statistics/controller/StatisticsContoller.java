package iljin.framework.ebid.etc.statistics.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.etc.statistics.service.StatisticsService;

@RestController
@RequestMapping("/api/v1/statistics")
@CrossOrigin
public class StatisticsContoller {

	@Autowired
    private StatisticsService statisticsService;
		
	//계열사 리스트 조회
	@PostMapping("/coInterList")
	public List<List<?>> selectCoInterList() {

		return statisticsService.selectCoInterList();
	}
	
	/**
	 * 계열사리스트 조회 v2
	 * @param params
	 * @return
	 */
	@PostMapping("/interrelatedCustCodeList")
	public ResultBody interrelatedCustCodeList(@RequestBody Map<String, Object> params) {
		return statisticsService.interrelatedCustCodeList(params);
	}
	
	//입찰실적 리스트 조회
	@PostMapping("/biInfoList")
	public List<List<?>> selectBiInfoList(@RequestBody Map<String, Object> params) {

		return statisticsService.selectBiInfoList(params);
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
	
	
}
