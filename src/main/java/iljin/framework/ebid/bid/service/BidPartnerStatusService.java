package iljin.framework.ebid.bid.service;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.UserDto;
import iljin.framework.core.security.user.UserRepository;
import iljin.framework.core.security.user.UserRepositoryCustom;
import iljin.framework.core.util.Util;
import iljin.framework.ebid.bid.dto.BidProgressCustDto;
import iljin.framework.ebid.bid.dto.BidProgressDetailDto;
import iljin.framework.ebid.bid.dto.BidProgressDto;
import iljin.framework.ebid.bid.dto.BidProgressFileDto;
import iljin.framework.ebid.bid.dto.BidProgressTableDto;
import iljin.framework.ebid.bid.dto.CoUserInfoDto;
import iljin.framework.ebid.bid.dto.CurrDto;
import iljin.framework.ebid.bid.dto.EmailDto;
import iljin.framework.ebid.bid.dto.InterUserInfoDto;
import iljin.framework.ebid.bid.dto.InterrelatedCustDto;
import iljin.framework.ebid.bid.dto.SubmitHistDto;
import iljin.framework.ebid.bid.entity.TBiInfoMatCust;
import iljin.framework.ebid.bid.entity.TBiInfoMatCustID;
import iljin.framework.ebid.bid.entity.TBiLog;
import iljin.framework.ebid.bid.repository.TBiInfoMatCustRepository;
import iljin.framework.ebid.custom.entity.TCoCustUser;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoCustUserRepository;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.PagaUtils;
import iljin.framework.ebid.etc.util.common.file.FileService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import iljin.framework.ebid.etc.util.common.file.FileService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class BidPartnerStatusService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    Util util;

    @Autowired
    private FileService fileService;

    @Autowired
    private BidProgressService bidProgressService;
    
    @Autowired
    private TCoCustUserRepository tCoCustUserRepository; 
    
    @Autowired
    private TBiInfoMatCustRepository tBiInfoMatCustRepository;

    @Value("${file.upload.directory}")
    private String uploadDirectory;

    public Page statuslist(@RequestBody Map<String, Object> params) {

        StringBuilder sbCount = new StringBuilder(
                " select count(1) FROM t_bi_info_mat a LEFT JOIN t_co_user b ON a.create_user = b.user_id LEFT JOIN t_co_user c ON a.gongo_id = c.user_id, t_bi_info_mat_cust d  WHERE a.bi_no = d.bi_no and d.cust_code = :custCode  ");
        StringBuilder sbList = new StringBuilder(
                "SELECT a.bi_no AS bi_no, a.bi_name AS bi_name, " +
                        "DATE_FORMAT(a.est_start_date, '%Y-%m-%d %H:%i') AS est_start_date, " +
                        "DATE_FORMAT(a.est_close_date, '%Y-%m-%d %H:%i') AS est_close_date, " +
                        "CASE WHEN a.bi_mode = 'A' THEN '지명' ELSE '일반' END AS bi_mode, " +
                        "CASE WHEN a.ins_mode = '1' THEN '파일' ELSE '직접입력' END AS ins_mode, " +
                        "CASE WHEN a.ing_tag = 'A1' AND d.esmt_yn = '2' THEN '투찰' WHEN a.ing_tag = 'A3' THEN '재입찰' WHEN a.ing_tag = 'A1' THEN '입찰공고' END AS ing_tag, "
                        +
                        "b.user_name AS cuser, b.user_email AS cuser_email, " +
                        "c.user_name AS gongo_id, c.user_email AS gongo_email, " +
                        "a.interrelated_cust_code AS interrelated_cust_code " +
                        "FROM t_bi_info_mat a LEFT JOIN t_co_user b ON a.create_user = b.user_id LEFT JOIN t_co_user c ON a.gongo_id = c.user_id, t_bi_info_mat_cust d "
                        +
                        "WHERE a.bi_no = d.bi_no and d.cust_code = :custCode ");
        StringBuilder sbWhere = new StringBuilder();

        if (!StringUtils.isEmpty(params.get("bidNo"))) {
            sbWhere.append(" and a.bi_no = :bidNo ");
        }

        if (!StringUtils.isEmpty(params.get("bidName"))) {
            sbWhere.append(" and a.bi_name like concat('%',:bidName,'%') ");
        }
        // biMode
        if ((Boolean) params.get("bidModeA") || (Boolean) params.get("bidModeB")) {
            sbWhere.append(" and ( ");
            if ((Boolean) params.get("bidModeA") && !(Boolean) params.get("bidModeB")) {
                sbWhere.append(" a.bi_mode = 'A' ");
            }
            if (!(Boolean) params.get("bidModeA") && (Boolean) params.get("bidModeB")) {
                sbWhere.append(" a.bi_mode = 'B' ");
            }
            if ((Boolean) params.get("bidModeA") && (Boolean) params.get("bidModeB")) {
                sbWhere.append(" a.bi_mode = 'A' or a.bi_mode = 'B'");
            }
            if (!(Boolean) params.get("bidModeA") && !(Boolean) params.get("bidModeB")) {
                sbWhere.append(" a.bi_mode = 'C'");
            }
            sbWhere.append(" ) ");
        }

        // ingTag
        if ((Boolean) params.get("noticeYn") || (Boolean) params.get("participateYn")
                || (Boolean) (params.get("rebidYn"))) {

            sbWhere.append(" and ( ");
            if ((Boolean) params.get("noticeYn") && !(Boolean) params.get("participateYn")
                    && !(Boolean) (params.get("rebidYn"))) {
                sbWhere.append(" a.ing_tag = 'A1' ");
            }
            if ((Boolean) params.get("noticeYn") && (Boolean) params.get("participateYn")
                    && !(Boolean) (params.get("rebidYn"))) {
                sbWhere.append(" a.ing_tag = 'A1' or d.esmt_yn = '2' ");
            }
            if ((Boolean) params.get("noticeYn") && (Boolean) params.get("participateYn")
                    && (Boolean) (params.get("rebidYn"))) {
                sbWhere.append(" a.ing_tag = 'A1' or d.esmt_yn = '2' or a.ing_tag = 'A3' ");
            }
            if ((Boolean) params.get("noticeYn") && !(Boolean) params.get("participateYn")
                    && (Boolean) (params.get("rebidYn"))) {
                sbWhere.append(" a.ing_tag = 'A1' or a.ing_tag = 'A3' ");
            }
            if (!(Boolean) params.get("noticeYn") && (Boolean) params.get("participateYn")
                    && !(Boolean) (params.get("rebidYn"))) {
                sbWhere.append(" d.esmt_yn = '2' ");
            }
            if (!(Boolean) params.get("noticeYn") && (Boolean) params.get("participateYn")
                    && (Boolean) (params.get("rebidYn"))) {
                sbWhere.append(" d.esmt_yn = '2' or a.ing_tag = 'A3' ");
            }
            if (!(Boolean) params.get("noticeYn") && !(Boolean) params.get("participateYn")
                    && (Boolean) (params.get("rebidYn"))) {
                sbWhere.append(" a.ing_tag = 'A3' ");
            }
            sbWhere.append(" ) ");
        } else {// 아무것도 체크하지 않은 경우

            sbWhere.append(" and a.ing_tag = '99' ");
        }

        sbList.append(sbWhere);
        sbCount.append(sbWhere);

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        Query queryTotal = entityManager.createNativeQuery(sbCount.toString());
        queryList.setParameter("custCode", params.get("custCode"));
        queryTotal.setParameter("custCode", params.get("custCode"));

        if (!StringUtils.isEmpty(params.get("bidNo"))) {
            queryList.setParameter("bidNo", params.get("bidNo"));
            queryTotal.setParameter("bidNo", params.get("bidNo"));
        }
        if (!StringUtils.isEmpty(params.get("bidName"))) {
            queryList.setParameter("bidName", params.get("bidName"));
            queryTotal.setParameter("bidName", params.get("bidName"));
        }

        Pageable pageable = PagaUtils.pageable(params);
        queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize()).getResultList();
        List list = new JpaResultMapper().list(queryList, BidProgressDto.class);

        BigInteger count = (BigInteger) queryTotal.getSingleResult();
        return new PageImpl(list, pageable, count.intValue());
    }

    public void checkBid(@RequestBody Map<String, Object> params) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getUsername();

        String biNo = (String) params.get("biNo");
        String custCode = (String) params.get("custCode");

        StringBuilder checkBid = new StringBuilder(
                "SELECT esmt_yn from t_bi_info_mat_cust where bi_no = :biNo and cust_code = :custCode");

        Query checkQ = entityManager.createNativeQuery(checkBid.toString());
        checkQ.setParameter("biNo", biNo);
        checkQ.setParameter("custCode", custCode);

        String esmtYn = (String) checkQ.getSingleResult();

        if (esmtYn.equals("0") || esmtYn == null) {
            StringBuilder updateBid = new StringBuilder(
                    "UPDATE t_bi_info_mat_cust set esmt_yn = '1', rebid_att = 'N' " +
                            "submit_date = null, file_id =null, enx_qutn =null, " +
                            "semt_curr = null, etc_b_file =null, file_hash_value =null, " +
                            "etc_b_file_path = null " +
                            "where bi_no = :biNo and cust_code = :custCode");
            Query updateQ = entityManager.createNativeQuery(updateBid.toString());
            updateQ.setParameter("biNo", biNo);
            updateQ.setParameter("custCode", custCode);
            updateQ.executeUpdate();

            StringBuilder delBid = new StringBuilder(
                    "DELETE FROM t_bi_detail_mat_cust where bi_no = :biNo and cust_code = :custCode");
            Query delQ = entityManager.createNativeQuery(delBid.toString());
            delQ.setParameter("biNo", biNo);
            delQ.setParameter("custCode", custCode);
            delQ.executeUpdate();
        }

        Map<String, String> logParams = new HashMap<>();
        logParams.put("msg", "[업체]공고확인");
        logParams.put("biNo", biNo);
        logParams.put("userId", userId);
        bidProgressService.updateLog(logParams);
    }

    public List<CurrDto> currlist() {
        StringBuilder currlist = new StringBuilder(
                "SELECT code_val, code_name from t_co_code where col_code = 'T_CO_RATE'");
        Query currlistQ = entityManager.createNativeQuery(currlist.toString());
        return new JpaResultMapper().list(currlistQ, CurrDto.class);
    }

    //투찰
    @Transactional
	public ResultBody bidSubmitting(@RequestBody Map<String, Object> params, MultipartFile file1, MultipartFile file2) {
		System.out.println("service 들어옴");
		System.out.println(file1.getOriginalFilename());
		System.out.println(file2.getOriginalFilename());
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getUsername();
        String biNo = "";
        String insModeCode = "";//입력방식( 1-파일등록, 2-내역직접 )
        int amt = 0;//파일입력 견적금액
        String strAmt = "";
        LocalDateTime currentDate = LocalDateTime.now();//update 또는 insert 되는 현재시점
        
        //직접입력
        List<Map<String, Object>> itemList = null;//직접입력 품목
        String strItemList = "";//직접입력 품목당 가격을 "‡" 구분자와 같이 넣은 문자열
        
        //파일입력
        int fileId = 0;//t_bi_upload에 insert한 견적내역파일 id
        String fileHashValue = "";//파일입력의 경우 복호화때 필요한 key값
        String etcFileName = "";//파일입력의 경우 기타파일 이름
        String etcFilePath = "";//파일입력의 경우 기타파일 경로
        
        if (!StringUtils.isEmpty(params.get("biNo"))) {
        	biNo = (String) params.get("biNo");//입찰번호
        }
        
        if (!StringUtils.isEmpty(params.get("insModeCode"))) {
        	insModeCode = (String) params.get("insModeCode");//입력방식
        }
        
        if (!StringUtils.isEmpty(params.get("amt"))) {
        	amt =  (int) params.get("amt");//총 견적금액
        	strAmt = Integer.toString(amt);
        }
        
        if (!StringUtils.isEmpty(params.get("tableContent"))) {
        	itemList = (List<Map<String, Object>>) params.get("tableContent");//직접입력 품목
        	
        	for(int i = 0; i < itemList.size(); i++) {
        		Map<String,Object> item = itemList.get(i);
        		
        		int seq = (int) item.get("seq");
        		String esmtAmt = (String) item.get("esmtAmt");
        		
        		if(i > 0) {//구분자
        			strItemList += "‡";
        		}
        		//품목순번 = 가격
        		strItemList += (seq + "=" + esmtAmt);
            }
        }
        
        //암호화
        //여기에 암호화 처리
        //strItemList
        //strAmt
        
        //파일업로드
        //파일입력 방식의 견적내역파일 경로 암호화
        //견적내역파일 isnert
        

        //협력사 사용자 정보
        Optional<TCoCustUser> userOptional = tCoCustUserRepository.findById(userId);
        
        if(userOptional.isPresent()) {
        	
        	//협력사 코드
        	int custCode = userOptional.get().getCustCode();
        	System.out.println("협력사 코드 >> " + custCode);
        	try {
        		
        		TBiInfoMatCustID tBiInfoMatCustId = new TBiInfoMatCustID();
        		
        		tBiInfoMatCustId.setBiNo(biNo);
        		tBiInfoMatCustId.setCustCode(custCode);
        		
        		Optional<TBiInfoMatCust> optional = tBiInfoMatCustRepository.findById(tBiInfoMatCustId);
        		System.out.println("입찰협력사 있는지 여부 >> " + optional.isPresent());
        		//입찰_협력업체 테이블에 데이터가 들어가 있는지 확인
        		if(optional.isPresent()) {//이미 입찰_협력업체 테이블에 데이터가 있는 경우 update(지명경쟁 or 이미투찰함)
        			System.out.println("update 시작");
        			TBiInfoMatCust tBiInfoMatCust = optional.get();
        			
        			tBiInfoMatCust.setEsmtYn("2");//esmt_yn( 업체투찰 flag  0-업체지정, 1-업체공고확인, 2-업체투찰)
        			tBiInfoMatCust.setUpdateUser(userId);
        			tBiInfoMatCust.setUpdateDate(currentDate);
        			tBiInfoMatCust.setEncQutn(strAmt);
        			
        			if(insModeCode.equals("1")) {//파일등록 방식
        				
        				tBiInfoMatCust.setFileId(fileId);
        				tBiInfoMatCust.setFileHashValue(fileHashValue);
        				
        				if(!etcFilePath.equals("") &&  etcFilePath != null) {//기타첨부 파일이 있는 경우
        					
        					tBiInfoMatCust.setEtcBFile(etcFileName);
        					tBiInfoMatCust.setEtcBFilePath(etcFilePath);
        					
        				}
        			}else {//내역직접 방식
        				tBiInfoMatCust.setEncEsmtSpec(strItemList);
        			}
        			
        			
        		}else {//입찰_협력업체 테이블에 데이터가 없는 경우 insert
        			System.out.println("insert 시작");
        			TBiInfoMatCust tBiInfoMatCust = new TBiInfoMatCust();
        			
        			tBiInfoMatCust.setBiNo(biNo);
        			tBiInfoMatCust.setCustCode(custCode);
        			tBiInfoMatCust.setEsmtYn("2");
        			tBiInfoMatCust.setCreateUser(userId);
        			tBiInfoMatCust.setCreateDate(currentDate);
        			tBiInfoMatCust.setBiOrder(1);
        			tBiInfoMatCust.setEncQutn(strAmt);
        			
        			if(insModeCode.equals("1")) {//파일등록 방식
        				
        				tBiInfoMatCust.setFileId(fileId);
        				tBiInfoMatCust.setFileHashValue(fileHashValue);
        				
        				if(!etcFilePath.equals("") &&  etcFilePath != null) {//기타첨부 파일이 있는 경우
        					
        					tBiInfoMatCust.setEtcBFile(etcFileName);
        					tBiInfoMatCust.setEtcBFilePath(etcFilePath);
        					
        				}
        				
        			}else {//내역직접 방식
        				tBiInfoMatCust.setEncEsmtSpec(strItemList);
        			}
        			
        			entityManager.persist(tBiInfoMatCust);
        			
        		}
        		
        		//로그 남기기
        		TBiLog tBiLog = new TBiLog();
        		
        		tBiLog.setBiNo(biNo);
        		tBiLog.setUserId(userId);
        		tBiLog.setLogText("[업체]견적제출");
        		tBiLog.setCreateDate(currentDate);
        		
        		entityManager.persist(tBiLog);
        		
        	}catch(Exception e) {
        		
        	}
        	
        	
        }
        
        
        
		return null;
	}
}
