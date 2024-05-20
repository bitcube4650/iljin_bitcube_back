package iljin.framework.ebid.etc.statistics.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

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

}
