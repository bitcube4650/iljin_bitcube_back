package iljin.framework.ebid.bid.service;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.util.Util;
import iljin.framework.ebid.bid.dto.BidProgressCustDto;
import iljin.framework.ebid.bid.dto.BidProgressDetailDto;
import iljin.framework.ebid.bid.dto.BidProgressDto;
import iljin.framework.ebid.bid.dto.BidProgressFileDto;
import iljin.framework.ebid.bid.dto.BidProgressTableDto;
import iljin.framework.ebid.bid.dto.ItemDto;
import iljin.framework.ebid.bid.dto.SubmitHistDto;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.PagaUtils;
import iljin.framework.ebid.etc.util.common.file.FileService;
import lombok.extern.slf4j.Slf4j;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class BidStatusService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TCoUserRepository tCoUserRepository;
    @Autowired
    Util util;

    @Autowired
    private FileService fileService;

    @Autowired
    private BidProgressService bidProgressService;

    @Value("${file.upload.directory}")
    private String uploadDirectory;


	/**
	 * 입찰진행 리스트
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResultBody statuslist(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody(); 
				
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = userOptional.get().getUserId();
		String interrelatedCode = userOptional.get().getInterrelatedCustCode();
		String userAuth = userOptional.get().getUserAuth();
		
		try {
			StringBuilder sbCount = new StringBuilder(
				" select count(1) from t_bi_info_mat tbim "
			);
			StringBuilder sbList = new StringBuilder(
				  "select	tbim.bi_no"
				+ ",		tbim.bi_name "
				+ ",		DATE_FORMAT(tbim.est_start_date, '%Y-%m-%d %H:%i') AS est_start_date "
				+ ",		DATE_FORMAT(tbim.est_close_date, '%Y-%m-%d %H:%i') AS est_close_date "
				+ ",		tbim.bi_mode "
				+ ",		tbim.ins_mode "
				+ ",		CASE	WHEN tbim.ing_tag = 'A1' AND tbim.est_close_date < sysdate() "
				+ "					THEN '입찰공고(개찰대상)' "
				+ "					WHEN tbim.ing_tag = 'A1' "
				+ "					THEN '입찰공고' "
				+ "					WHEN tbim.ing_tag = 'A3' "
				+ "					THEN '입찰공고(재)' "
				+ "					ELSE '개찰' "
				+ "			END AS ing_tag "
				+ ",		tcu1.user_name AS cuser "
				+ ",		tcu1.user_email AS cuser_email "
				+ ",		tcu2.user_name AS gongo_id "
				+ ",		tcu2.user_email AS gongo_email "
				+ ",		tbim.interrelated_cust_code "
				+ "FROM t_bi_info_mat tbim "
				+ "LEFT JOIN t_co_user tcu1 "
				+ "	ON tbim.create_user = tcu1.user_id "
				+ "LEFT JOIN t_co_user tcu2 "
				+ "	ON tbim.gongo_id = tcu2.user_id "
			);
			
			if (userAuth.equals("4")) {
				
				String addStr = "inner join t_co_user_interrelated tcui "
							+ "	on tbim.INTERRELATED_CUST_CODE = tcui.INTERRELATED_CUST_CODE "
							+ "	and tcui.USER_ID = :userId ";
				
				sbCount.append(addStr);
				sbList.append(addStr);
			}
			
			//조회조건
			StringBuilder sbWhere = new StringBuilder();
			
			sbWhere.append("where 1=1 ");
			
			//입찰번호
			if (!StringUtils.isEmpty(params.get("bidNo"))) {
				sbWhere.append(" and tbim.bi_no = :bidNo ");
			}
			
			//입찰명
			if (!StringUtils.isEmpty(params.get("bidName"))) {
				sbWhere.append(" and tbim.bi_name like concat('%',:bidName,'%') ");
			}
			
			//진행상태
			if((Boolean) params.get("rebidYn")) {
				sbWhere.append(" and tbim.ing_tag in ( 'A1', 'A2', 'A3' )");
			}else if((Boolean) params.get("dateOverYn")) {
				sbWhere.append(" and tbim.ing_tag in ( 'A1' )");
			}else if((Boolean) params.get("openBidYn")) {
				sbWhere.append(" and tbim.ing_tag in ( 'A2' )");
			}
			
			// 개찰대상 체크한 경우
			if ((Boolean) params.get("dateOverYn")) {
				sbWhere.append(" and tbim.est_close_date < sysdate() ");
			}
	
			if (!userAuth.equals("4")) {
				sbWhere.append("and tbim.interrelated_cust_code = :interrelatedCustCode ");
			}
			
			sbWhere.append(
					"and ( tbim.create_user = :userId "
				+	"	or tbim.open_att1 = :userId " 
				+	"	or tbim.open_att2 = :userId " 
				+	"	or tbim.gongo_id = :userId " 
				+	"	or tbim.est_bidder = :userId " 
				+	"	or tbim.est_opener = :userId ) "
			);
			
			sbList.append(sbWhere);
			sbCount.append(sbWhere);
	
			Query queryList = entityManager.createNativeQuery(sbList.toString());
			Query queryTotal = entityManager.createNativeQuery(sbCount.toString());
	
			if (!StringUtils.isEmpty(params.get("bidNo"))) {
				queryList.setParameter("bidNo", params.get("bidNo"));
				queryTotal.setParameter("bidNo", params.get("bidNo"));
			}
			if (!StringUtils.isEmpty(params.get("bidName"))) {
				queryList.setParameter("bidName", params.get("bidName"));
				queryTotal.setParameter("bidName", params.get("bidName"));
			}
			if (userAuth.equals("1") || userAuth.equals("2") || userAuth.equals("3")) {
				queryList.setParameter("interrelatedCustCode", interrelatedCode);
				queryTotal.setParameter("interrelatedCustCode", interrelatedCode);
			}
			
			queryList.setParameter("userId", userId);
			queryTotal.setParameter("userId", userId);
			
			Pageable pageable = PagaUtils.pageable(params);
			queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();
			List list = new JpaResultMapper().list(queryList, BidProgressDto.class);
	
			BigInteger count = (BigInteger) queryTotal.getSingleResult();
			Page listPage = new PageImpl(list, pageable, count.intValue());
			resultBody.setData(listPage);
			
		}catch(Exception e) {
			log.error("statuslist list error : {}", e);
			resultBody.setCode("999");
			resultBody.setMsg("입찰 진행 리스트를 가져오는것을 실패하였습니다.");	
		}
		
		return resultBody;
	}
	
	/**
	 * 입찰진행 상세
	 * @param param
	 * @return
	 */
	public ResultBody progresslistDetail(String param) {
		ResultBody resultBody = new ResultBody();
		
		StringBuilder sbList = new StringBuilder(
				"SELECT a.bi_no AS bi_no, a.bi_name AS bi_name, " +
						"CASE WHEN a.bi_mode = 'A' THEN '지명경쟁입찰' ELSE '일반경쟁입찰' END AS bi_mode, a.bi_mode AS bi_mode_code, "
						+
						"CASE WHEN a.ins_mode = '1' THEN '파일등록' ELSE '직접입력' END AS ins_mode, a.ins_mode AS ins_mode_code, "
						+
						"a.bid_join_spec AS bid_join_spec, a.special_cond AS special_cond, a.supply_cond AS supply_cond, "
						+
						"DATE_FORMAT(a.spot_date, '%Y-%m-%d %H:%i') AS spot_date, a.spot_area AS spot_area, " +
						"CASE WHEN a.succ_deci_meth = '1' THEN '최저가' WHEN a.succ_deci_meth = '2' THEN '최고가' " +
						"WHEN a.succ_deci_meth = '3' THEN '내부적격심사' WHEN a.succ_deci_meth = '4' THEN '최고가&내부적격심사' " +
						"ELSE '최저가&내부적격심사' END AS succ_deci_meth, a.succ_deci_meth AS succ_deci_meth_code, DATE_FORMAT(a.est_start_date, '%Y-%m-%d %H:%i') AS est_start_date, "
						+
						"DATE_FORMAT(a.est_close_date, '%Y-%m-%d %H:%i') AS est_close_date, b.user_name AS est_opener, a.est_opener AS est_opener_code, i.user_name AS cuser, a.create_user AS cuser_code, "
						+
						"DATE_FORMAT(a.est_open_date, '%Y-%m-%d %H:%i') AS est_open_date, c.user_name AS open_att1, a.open_att1 AS open_att1_code,"
						+
						"a.open_att1_sign AS open_att1_sign, d.user_name AS open_att2, a.open_att2 AS open_att2_code, a.open_att2_sign AS open_att2_sign, "
						+
						"a.ing_tag AS ing_tag, a.item_code AS item_code, f.item_name AS item_name, e.user_name AS gongo_id, a.gongo_id AS gongo_id_code, i.dept_name AS cuser_dept, a.pay_cond AS pay_cond, a.why_A3 AS why_A3, "
						+
						"a.why_A7 AS why_A7, a.bi_open AS bi_open, a.interrelated_cust_code AS interrelated_cust_code, h.interrelated_nm AS interrelated_nm, a.real_amt AS real_amt, a.amt_basis AS amt_basis, a.bd_amt AS bd_amt,"
						+
						"a.add_accept AS add_accept, a.mat_dept AS mat_dept, a.mat_proc AS mat_proc, a.mat_cls AS mat_cls, "
						+
						"a.mat_factory AS mat_factory, a.mat_factory_line AS mat_factory_line, a.mat_factory_cnt AS mat_factory_cnt "
						+
						"FROM t_bi_info_mat a " +
						"LEFT JOIN t_co_user b ON a.est_opener = b.user_id " +
						"LEFT JOIN t_co_user c ON a.open_att1 = c.user_id " +
						"LEFT JOIN t_co_user d ON a.open_att2 = d.user_id " +
						"LEFT JOIN t_co_user e ON a.gongo_id = e.user_id " +
						"LEFT JOIN t_co_item f ON a.item_code = f.item_code " +
						"LEFT JOIN t_co_user g ON a.gongo_id = g.user_id " +
						"LEFT JOIN t_co_user i ON a.create_user = i.user_id " +
						"JOIN t_co_interrelated h ON a.interrelated_cust_code = h.interrelated_cust_code " +
						"WHERE 1=1 ");

		StringBuilder sbTableList = new StringBuilder(
				"SELECT a.bi_no AS bi_no, a.seq AS seq, a.name AS name, a.ssize AS ssize, " +
						"a.order_qty AS order_qty, a.unitcode AS unitcode, ifnull(a.order_uc, 0) AS order_uc " +
						"FROM t_bi_spec_mat a " +
						"WHERE 1=1 ");

		StringBuilder sbFileList = new StringBuilder(
				"SELECT a.bi_no AS bi_no, a.file_flag AS file_flag, " +
						"CASE WHEN a.file_flag = 'K' THEN '세부내역' WHEN a.file_flag = '0' THEN '대내용' WHEN a.file_flag = '1' THEN '대외용' END AS file_flag_ko, "
						+
						"a.file_nm AS file_NM, a.file_path AS file_path " +
						"FROM t_bi_upload a " +
						"WHERE a.use_yn = 'Y' ");

		StringBuilder sbCustList = new StringBuilder(
				"SELECT a.bi_no AS bi_no, CAST(a.cust_code AS CHAR) AS cust_code, b.cust_name AS cust_name, " +
						"a.esmt_yn AS esmt_yn, c.file_nm AS file_nm, c.file_path AS file_path, a.etc_b_file AS etc_file, a.etc_b_file_path AS etc_path "
						+
						"FROM t_bi_info_mat_cust a " +
						"LEFT JOIN t_co_cust_master b ON a.cust_code = b.cust_code " +
						"LEFT JOIN t_bi_upload c ON a.file_id = c.file_id " +
						"WHERE 1=1 ");

		StringBuilder sbWhere = new StringBuilder();
		sbWhere.append(" and a.bi_no = :param");
		sbList.append(sbWhere);
		sbTableList.append(sbWhere);
		sbFileList.append(sbWhere);
		sbCustList.append(sbWhere);

		Query queryList = entityManager.createNativeQuery(sbList.toString());
		Query queryTableList = entityManager.createNativeQuery(sbTableList.toString());
		Query queryFileList = entityManager.createNativeQuery(sbFileList.toString());
		Query queryCustList = entityManager.createNativeQuery(sbCustList.toString());
		queryList.setParameter("param", param);
		queryTableList.setParameter("param", param);
		queryFileList.setParameter("param", param);
		queryCustList.setParameter("param", param);

		List<BidProgressDetailDto> resultList = new JpaResultMapper().list(queryList, BidProgressDetailDto.class);
		List<BidProgressTableDto> tableList = new JpaResultMapper().list(queryTableList, BidProgressTableDto.class);
		List<BidProgressFileDto> fileList = new JpaResultMapper().list(queryFileList, BidProgressFileDto.class);
		List<BidProgressCustDto> custList = new JpaResultMapper().list(queryCustList, BidProgressCustDto.class);
		List<List<?>> combinedResults = new ArrayList<>();
		combinedResults.add(resultList);
		combinedResults.add(tableList);
		combinedResults.add(fileList);
		combinedResults.add(custList);

		
		
		return resultBody;
//		return combinedResults;
	}

    @Transactional
    public ResultBody bidFailure(Map<String, String> params) {
        String biNo = params.get("biNo");

        System.out.println(11111111 + biNo);
        StringBuilder sbList = new StringBuilder(
                "UPDATE t_bi_info_mat set bid_open_date = sysdate()," +
                        "ing_tag = 'A7', why_a7 = :reason " +
                        "WHERE bi_no = :biNo");

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("biNo", biNo);
        queryList.setParameter("reason", (String) params.get("reason"));
        int rowsUpdated = queryList.executeUpdate();

        if (rowsUpdated > 0) {
            Map<String, String> logParams = new HashMap<>();
            logParams.put("msg", "[본사] 유찰");
            logParams.put("biNo", biNo);
            bidProgressService.updateLog(logParams);
        }

        bidProgressService.updateEmail(params);

        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    public Page submitHist(@RequestBody Map<String, Object> params) {
        String biNo = (String) params.get("biNo");
        String custCode = (String) params.get("custCode");

        StringBuilder sbCount = new StringBuilder("");
        StringBuilder sbList = new StringBuilder("");

        StringBuilder ins = new StringBuilder(
                "SELECT ins_mode from t_bi_info_mat where bi_no = :biNo");
        Query insList = entityManager.createNativeQuery(ins.toString());
        insList.setParameter("biNo", biNo);
        String insMode = (String) insList.getSingleResult();

        if (insMode.equals("1")) {
            sbCount.append(
                    "SELECT count(1) from t_bi_info_mat_cust_temp where bi_no = :biNo and cust_code = :custCode");
            sbList.append(
                    "SELECT '1' AS insMode, bi_order, esmt_curr, esmt_amt, DATE_FORMAT(submit_date, '%Y-%m-%d %H:%i') AS submit_date from t_bi_info_mat_cust_temp "
                            +
                            "where bi_no = :biNo and cust_code = :custCode");
        }

        else if (insMode.equals("2")) {
            sbCount.append(
                    "SELECT count(1) from t_bi_detail_mat_cust_temp a, t_bi_info_mat_cust b " +
                            "where a.bi_no = :biNo and a.cust_code = :custCode and (a.bi_no =b.bi_no and a.cust_code = b.cust_code)");
            sbList.append(
                    "SELECT '2' AS insMode, a.bi_order AS bi_order, 'KRW' AS esmt_curr, a.esmt_uc AS esmt_amt, DATE_FORMAT(b.submit_date, '%Y-%m-%d %H:%i') AS submit_date "
                            +
                            "from t_bi_detail_mat_cust_temp a, t_bi_info_mat_cust b " +
                            "where a.bi_no = :biNo and a.cust_code = :custCode and (a.bi_no =b.bi_no and a.cust_code = b.cust_code)");
        }
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        Query queryCountList = entityManager.createNativeQuery(sbCount.toString());
        queryList.setParameter("biNo", biNo);
        queryCountList.setParameter("biNo", biNo);
        queryList.setParameter("custCode", custCode);
        queryCountList.setParameter("custCode", custCode);

        Pageable pageable = PagaUtils.pageable(params);
        queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize()).getResultList();
        List list = new JpaResultMapper().list(queryList, SubmitHistDto.class);

        BigInteger count = (BigInteger) queryCountList.getSingleResult();
        return new PageImpl(list, pageable, count.intValue());
    }

    @Transactional
    public ResultBody rebid(@RequestBody Map<String, Object> params) { // rebid 페이지에서 disabled 조건인 칼럼 모두 제외

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getUsername();

        StringBuilder sbList = new StringBuilder( // 입찰 업데이트
                "UPDATE t_bi_info_mat SET bi_name = :biName,  " +
                        "bid_join_spec = :bidJoinSpec, special_cond = :specialCond, supply_cond = :supplyCond, " +
                        "spot_date = STR_TO_DATE(:spotDate, '%Y-%m-%d %H:%i'), spot_area = :spotArea, " +
                        "succ_deci_meth = :succDeciMethCode, amt_basis = :amtBasis, bd_amt = :bdAmt, " +
                        "update_user = :userId, update_date = sysdate(), pay_cond = :payCond, ing_tag = 'A3', why_a3 = :whyA3 "
                        +
                        "WHERE bi_no = :biNo");

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("biName", (String) params.get("biName"));
        queryList.setParameter("bidJoinSpec", (String) params.get("bidJoinSpec"));
        queryList.setParameter("specialCond", (String) params.get("specialCond"));
        queryList.setParameter("supplyCond", (String) params.get("supplyCond"));
        queryList.setParameter("spotDate", (String) params.get("spotDate"));
        queryList.setParameter("spotArea", (String) params.get("spotArea"));
        queryList.setParameter("succDeciMethCode", (String) params.get("succDeciMethCode"));
        queryList.setParameter("amtBasis", (String) params.get("amtBasis"));
        queryList.setParameter("bdAmt", params.get("bdAmt"));
        queryList.setParameter("userId", userId);
        queryList.setParameter("payCond", (String) params.get("payCond"));
        queryList.setParameter("whyA3", (String) params.get("whyA3"));
        queryList.setParameter("biNo", (String) params.get("biNo"));

        queryList.executeUpdate();

        StringBuilder sbList1 = new StringBuilder( // 입찰 hist 업데이트
                "INSERT into t_bi_info_mat_hist (bi_no, bi_name, bi_mode, ins_mode, bid_join_spec, special_cond, supply_cond, spot_date, "
                        +
                        "spot_area, succ_deci_meth, amt_basis, bd_amt, est_start_date, est_close_date, est_opener, est_bidder, "
                        +
                        "open_att1, open_att2, ing_tag, update_user, update_date, item_code, " +
                        "gongo_id, pay_cond, bi_open, mat_dept, mat_proc, mat_cls, mat_factory, mat_factory_line, mat_factory_cnt, why_a3) "
                        +
                        "values (:biNo, :biName, :biModeCode, :insModeCode, :bidJoinSpec, :specialCond, :supplyCond, " +
                        "STR_TO_DATE(:spotDate, '%Y-%m-%d %H:%i'), :spotArea, :succDeciMethCode, :amtBasis, :bdAmt, "
                        +
                        "est_start_date =STR_TO_DATE(:estStartDate, '%Y-%m-%d %H:%i'), est_close_date =STR_TO_DATE(:estCloseDate, '%Y-%m-%d %H:%i'), :estOpenerCode, :estBidderCode, "
                        +
                        ":openAtt1Code, :openAtt2Code, 'A3', :userId, sysdate(), :itemCode, :gongoIdCode, :payCond, 'N', :matDept, :matProc, :matCls, :matFactory, "
                        +
                        ":matFactoryLine, :matFactoryCnt, :whyA3)");

        Query queryList1 = entityManager.createNativeQuery(sbList1.toString());
        queryList1.setParameter("biNo", (String) params.get("biNo"));
        queryList1.setParameter("biName", (String) params.get("biName"));
        queryList1.setParameter("biModeCode", (String) params.get("biModeCode"));
        queryList1.setParameter("insModeCode", (String) params.get("insModeCode"));
        queryList1.setParameter("bidJoinSpec", (String) params.get("bidJoinSpec"));
        queryList1.setParameter("specialCond", (String) params.get("specialCond"));
        queryList1.setParameter("supplyCond", (String) params.get("supplyCond"));
        queryList1.setParameter("spotDate", (String) params.get("spotDate"));
        queryList1.setParameter("spotArea", (String) params.get("spotArea"));
        queryList1.setParameter("succDeciMethCode", (String) params.get("succDeciMethCode"));
        queryList1.setParameter("amtBasis", (String) params.get("amtBasis"));
        queryList1.setParameter("bdAmt", params.get("bdAmt"));
        queryList1.setParameter("estStartDate", (String) params.get("estStartDate"));
        queryList1.setParameter("estCloseDate", (String) params.get("estCloseDate"));
        queryList1.setParameter("estOpenerCode", (String) params.get("estOpenerCode"));
        queryList1.setParameter("estBidderCode", (String) params.get("estBidderCode"));
        queryList1.setParameter("openAtt1Code", (String) params.get("openAtt1Code"));
        queryList1.setParameter("openAtt2Code", (String) params.get("openAtt2Code"));
        queryList1.setParameter("userId", userId);
        queryList1.setParameter("itemCode", (String) params.get("itemCode"));
        queryList1.setParameter("gongoIdCode", (String) params.get("gongoIdCode"));
        queryList1.setParameter("payCond", (String) params.get("payCond"));
        queryList1.setParameter("matDept", (String) params.get("matDept"));
        queryList1.setParameter("matProc", (String) params.get("matProc"));
        queryList1.setParameter("matCls", (String) params.get("matCls"));
        queryList1.setParameter("matFactory", (String) params.get("matFactory"));
        queryList1.setParameter("matFactoryLine", (String) params.get("matFactoryLine"));
        queryList1.setParameter("matFactoryCnt", (String) params.get("matFactoryCnt"));
        queryList1.setParameter("whyA3", (String) params.get("whyA3"));

        int rowsUpdated = queryList1.executeUpdate();
        if (rowsUpdated > 0) {
            Map<String, String> logParams = new HashMap<>();
            logParams.put("msg", "[본사] 재입찰");
            logParams.put("biNo", (String) params.get("biNo"));
            bidProgressService.updateLog(logParams);

            Map<String, String> mailParams = new HashMap<>();
            mailParams.put("biNo", (String) params.get("biNo"));
            mailParams.put("type", (String) params.get("type"));
            mailParams.put("biName", (String) params.get("biName"));
            mailParams.put("reason", (String) params.get("whyA7"));
            mailParams.put("interNm", (String) params.get("interNm"));
            bidProgressService.updateEmail(mailParams);
        }

        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    @Transactional
    public ResultBody rebidCust(@RequestBody List<Map<String, Object>> params) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        String userId = principal.getUsername();
        if (params.size() > 0) {
            String biNo = (String) params.get(0).get("biNo");

            for (Map<String, Object> data : params) {
                StringBuilder sbList = new StringBuilder(
                        "UPDATE t_bi_info_mat_cust SET rebid_att = 'Y', esmt_yn = '0', esmt_amt = 0, update_user = :userId, update_date = sysdate() "
                                +
                                "where bi_no = :biNo and cust_code = :custCode");
                Query queryList = entityManager.createNativeQuery(sbList.toString());
                queryList.setParameter("userId", userId);
                queryList.setParameter("biNo", biNo);
                queryList.setParameter("custCode", (String) data.get("custCode"));
                queryList.executeUpdate();
            }
        }

        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    public List<ItemDto> itemlist(@RequestBody Map<String, Object> params) {
        StringBuilder itemlist = new StringBuilder(
                "SELECT a.bi_no, a.seq, a.order_qty, a.name, a.ssize, a.unitcode, b.esmt_uc, b.cust_code " +
                        "from t_bi_spec_mat a, t_bi_detail_mat_cust b " +
                        "where a.bi_no = :biNo and b.cust_code = :custCode " +
                        "and (a.bi_no = b.bi_no and a.seq = b.seq)");
        Query itemlistQ = entityManager.createNativeQuery(itemlist.toString());
        itemlistQ.setParameter("biNo", (String) params.get("biNo"));
        itemlistQ.setParameter("custCode", (String) params.get("custCode"));
        return new JpaResultMapper().list(itemlistQ, ItemDto.class);
    }

    public ResultBody bidSucc(@RequestBody Map<String, Object> params) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        String userId = principal.getUsername();

        StringBuilder sbList = new StringBuilder( // 입찰 업데이트
                "UPDATE t_bi_info_mat SET ing_tag = 'A5', update_user = :userId, update_date = sysdate(), add_aceept = :reason, "
                        +
                        "succ_amt = :esmtAmt where bi_no = :biNo ");
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("userId", userId);
        queryList.setParameter("reason", (String) params.get("reason"));
        queryList.setParameter("esmtAmt", params.get("esmtAmt"));
        queryList.setParameter("biNo", (String) params.get("biNo"));

        queryList.executeUpdate();

        StringBuilder sbList1 = new StringBuilder( // 입찰 hist 업데이트
                "INSERT into t_bi_info_mat_hist (bi_no, bi_name, bi_mode, ins_mode, bid_join_spec, special_cond, supply_cond, spot_date, "
                        +
                        "spot_area, succ_deci_meth, amt_basis, bd_amt, est_start_date, est_close_date, est_opener, est_bidder, "
                        +
                        "open_att1, open_att2, ing_tag, update_user, update_date, item_code, " +
                        "gongo_id, pay_cond, bi_open, mat_dept, mat_proc, mat_cls, mat_factory, mat_factory_line, mat_factory_cnt, add_accept, succ_amt) "
                        +
                        "values (:biNo, :biName, :biModeCode, :insModeCode, :bidJoinSpec, :specialCond, :supplyCond, " +
                        "STR_TO_DATE(:spotDate, '%Y-%m-%d %H:%i'), :spotArea, :succDeciMethCode, :amtBasis, :bdAmt, "
                        +
                        "est_start_date =STR_TO_DATE(:estStartDate, '%Y-%m-%d %H:%i'), est_close_date =STR_TO_DATE(:estCloseDate, '%Y-%m-%d %H:%i'), :estOpenerCode, :estBidderCode, "
                        +
                        ":openAtt1Code, :openAtt2Code, 'A5', :userId, sysdate(), :itemCode, :gongoIdCode, :payCond, 'Y', :matDept, :matProc, :matCls, :matFactory, "
                        +
                        ":matFactoryLine, :matFactoryCnt, :reason, :esmtAmt)");

        Query queryList1 = entityManager.createNativeQuery(sbList1.toString());
        queryList1.setParameter("biNo", (String) params.get("biNo"));
        queryList1.setParameter("biName", (String) params.get("biName"));
        queryList1.setParameter("biModeCode", (String) params.get("biModeCode"));
        queryList1.setParameter("insModeCode", (String) params.get("insModeCode"));
        queryList1.setParameter("bidJoinSpec", (String) params.get("bidJoinSpec"));
        queryList1.setParameter("specialCond", (String) params.get("specialCond"));
        queryList1.setParameter("supplyCond", (String) params.get("supplyCond"));
        queryList1.setParameter("spotDate", (String) params.get("spotDate"));
        queryList1.setParameter("spotArea", (String) params.get("spotArea"));
        queryList1.setParameter("succDeciMethCode", (String) params.get("succDeciMethCode"));
        queryList1.setParameter("amtBasis", (String) params.get("amtBasis"));
        queryList1.setParameter("bdAmt", params.get("bdAmt"));
        queryList1.setParameter("estStartDate", (String) params.get("estStartDate"));
        queryList1.setParameter("estCloseDate", (String) params.get("estCloseDate"));
        queryList1.setParameter("estOpenerCode", (String) params.get("estOpenerCode"));
        queryList1.setParameter("estBidderCode", (String) params.get("estBidderCode"));
        queryList1.setParameter("openAtt1Code", (String) params.get("openAtt1Code"));
        queryList1.setParameter("openAtt2Code", (String) params.get("openAtt2Code"));
        queryList1.setParameter("userId", userId);
        queryList1.setParameter("itemCode", (String) params.get("itemCode"));
        queryList1.setParameter("gongoIdCode", (String) params.get("gongoIdCode"));
        queryList1.setParameter("payCond", (String) params.get("payCond"));
        queryList1.setParameter("matDept", (String) params.get("matDept"));
        queryList1.setParameter("matProc", (String) params.get("matProc"));
        queryList1.setParameter("matCls", (String) params.get("matCls"));
        queryList1.setParameter("matFactory", (String) params.get("matFactory"));
        queryList1.setParameter("matFactoryLine", (String) params.get("matFactoryLine"));
        queryList1.setParameter("matFactoryCnt", (String) params.get("matFactoryCnt"));
        queryList1.setParameter("reason", (String) params.get("reason"));
        queryList1.setParameter("esmtAmt", params.get("esmtAmt"));

        queryList1.executeUpdate();

        StringBuilder sbList2 = new StringBuilder( // 업체정보 업데이트
                "UPDATE t_bi_info_mat_cust SET succ_yn ='Y', update_user = :userId, update_date = sysdate() " +
                        "where bi_no = :biNo and cust_code = :custCode");
        Query queryList2 = entityManager.createNativeQuery(sbList2.toString());
        queryList2.setParameter("userId", userId);
        queryList2.setParameter("biNo", (String) params.get("biNo"));
        queryList2.setParameter("custCode", (String) params.get("custCode"));

        queryList2.executeUpdate();

        StringBuilder sbList3 = new StringBuilder( // 업체정보차수 업데이트
                "UPDATE t_bi_info_mat_cust_temp SET succ_yn ='Y', update_user = :userId, update_date = sysdate() " +
                        "where bi_no = :biNo and cust_code = :custCode");
        Query queryList3 = entityManager.createNativeQuery(sbList3.toString());
        queryList3.setParameter("userId", userId);
        queryList3.setParameter("biNo", (String) params.get("biNo"));
        queryList3.setParameter("custCode", (String) params.get("custCode"));

        int q = queryList3.executeUpdate();
        if (q > 0) {
            Map<String, String> logParams = new HashMap<>();
            logParams.put("msg", "[본사] 낙찰");
            logParams.put("biNo", (String) params.get("biNo"));
            bidProgressService.updateLog(logParams);

            Map<String, String> mailParams = new HashMap<>();
            mailParams.put("biNo", (String) params.get("biNo"));
            mailParams.put("type", (String) params.get("type"));
            mailParams.put("biName", (String) params.get("biName"));
            mailParams.put("reason", (String) params.get("reason"));
            mailParams.put("interNm", (String) params.get("interNm"));
            bidProgressService.updateEmail(mailParams);
        }

        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    public void updateSign(@RequestBody Map<String, Object> params) {
        StringBuilder sbList = new StringBuilder(
            "UPDATE t_bi_info_mat SET ");
        if((Boolean) params.get("att1")){
            sbList.append("open_att1_sign = 'Y' ");
        }    
        if((Boolean) params.get("att1") && (Boolean) params.get("att2")){
            sbList.append(",");
        }  
        if((Boolean) params.get("att2")){
            sbList.append("open_att2_sign = 'Y' ");
        }
        sbList.append("where bi_no = :biNo");
        
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("biNo", (String) params.get("biNo"));
        queryList.executeUpdate();
    }

	public ResultBody bidOpening(Map<String, String> params) {
		// TODO Auto-generated method stub
		return null;
	}

}
