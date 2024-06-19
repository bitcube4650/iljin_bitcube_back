package iljin.framework.ebid.bid.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.core.util.Util;
import iljin.framework.ebid.bid.dto.InterUserInfoDto;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.GeneralDao;
import iljin.framework.ebid.etc.util.common.file.FileService;
import iljin.framework.ebid.etc.util.common.message.MessageService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BidProgressService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TCoUserRepository tCoUserRepository;
    @Autowired
    Util util;

    @Autowired
    private FileService fileService;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    GeneralDao generalDao;

    @Value("${file.upload.directory}")
    private String uploadDirectory;

    public ResultBody custList(@RequestBody Map<String, Object> params) throws Exception {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());

        String interrelatedCode = userOptional.get().getInterrelatedCustCode();
        params.put("interrelatedCode", interrelatedCode);
		ResultBody resultBody = new ResultBody();
		Page listPage = generalDao.selectGernalListPage("bid.selectCustList", params);
		
		resultBody.setData(listPage);
       return resultBody;
    }

    public ResultBody pastBidList(@RequestBody Map<String, Object> params) throws Exception {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());

        String interrelatedCode = userOptional.get().getInterrelatedCustCode();
        String userId = principal.getUsername();
        params.put("interrelatedCode", interrelatedCode);
        params.put("userId", userId);
        
		ResultBody resultBody = new ResultBody();
		Page listPage = generalDao.selectGernalListPage("bid.selectPastBidList", params);
		
		resultBody.setData(listPage);

        return resultBody;
    }

	public ResultBody progressList(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		try {
			UserDetails			principal	= (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			Optional<TCoUser>	userOptional= tCoUserRepository.findById(principal.getUsername());
			
			String userAuth			= userOptional.get().getUserAuth();
			String interrelatedCode	= userOptional.get().getInterrelatedCustCode();
			String userId			= principal.getUsername();
			
			/*
			params.put("biNo"					, params.get("biNo"));
			params.put("biName"				, params.get("biName"));
			*/
			params.put("interrelatedCustCode"	, interrelatedCode);
			params.put("userId"					, userId);
			params.put("userAuth"				, userAuth);
			
			if (userAuth.equals("4")) {
				
				List<Object> userInfoList = generalDao.selectGernalList("bid.selectInterCustCode", params);
				List<String> custCodes = new ArrayList<>();
				for (Object userInfo : userInfoList) {
					Map<String,Object> userInfoMap = (Map<String, Object>) userInfo;
					custCodes.add(userInfoMap.get("interrelatedCustCode").toString());
				}
				
				params.put("custCodes", custCodes);
			}
			
			Page listPage = generalDao.selectGernalListPage("bid.selectProgressList", params);
			resultBody.setData(listPage);
			
		} catch (Exception e) {
			log.error("BidProgressService selectProgressList error : ", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰 계획 리스트를 가져오는것을 실패하였습니다.");
			
		}
		
		return resultBody;
	}

    public ResultBody progresslistDetail(@RequestBody Map<String,Object> params) throws Exception {

    	Map<String,Object> paramMap = new HashMap<>();
    	paramMap.put("biNo", params.get("biNo"));
		List<Object> selectProgressDetailList = generalDao.selectGernalList("bid.selectProgressDetailList", paramMap);
		List<Object> selectProgressDetailTableList = generalDao.selectGernalList("bid.selectProgressDetailTableList", paramMap);
		List<Object> selectProgressDetaiFileList = generalDao.selectGernalList("bid.selectProgressDetaiFileList", paramMap);
		List<Object> selectProgressDetaiCustList = generalDao.selectGernalList("bid.selectProgressDetaiCustList", paramMap);
        
		List<Object> result = new ArrayList<>();
        result.add(selectProgressDetailList);
        result.add(selectProgressDetailTableList);
        result.add(selectProgressDetaiFileList);
        result.add(selectProgressDetaiCustList);
        
		int selectProgressDetaiCustListSize = selectProgressDetaiCustList.size(); 
        StringBuilder usemailIds = new StringBuilder();
        
        if(selectProgressDetaiCustListSize >0) {
  
	        String usemailIdFilter = "";
	        for (int i = 0; i < selectProgressDetaiCustListSize; i++) {
	           Map<String,Object> selectProgressDetaiCustListMap = (Map<String, Object>) selectProgressDetaiCustList.get(i);
	
	            if (i < selectProgressDetaiCustListSize - 1) {
	            	usemailIdFilter +=(selectProgressDetaiCustListMap.get("usemailId").toString()+ ",");
	            }else {
	            	usemailIdFilter += (selectProgressDetaiCustListMap.get("usemailId").toString());
	            }
	        }
            
	    	paramMap.put("usemailIds", usemailIdFilter.split(","));
	    	List<Object> selectProgressDetaiCustUserList = generalDao.selectGernalList("bid.selectProgressDetaiCustUserList", paramMap);
	        result.add(selectProgressDetaiCustUserList);
        }

        
		ResultBody resultBody = new ResultBody();
		resultBody.setData(result);
        return resultBody;
    }

    public ResultBody userList(@RequestBody Map<String, Object> params) throws Exception {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        String userId = principal.getUsername();
        params.put("userId", userId);
        
        ResultBody resultBody = new ResultBody();
		Page listPage = generalDao.selectGernalListPage("bid.selectUserList", params);
		resultBody.setData(listPage);
		
        return resultBody;

    }
    
    public List<?> findInterCustCode(String param) {
        StringBuilder sbList = new StringBuilder(
                "SELECT a.user_id AS user_id, a.interrelated_cust_code AS interrelated_cust_code " +
                        "FROM t_co_user_interrelated a " +
                        "WHERE a.user_id = :param");

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("param", param);
        return new JpaResultMapper().list(queryList, InterUserInfoDto.class);
    }
    

    @Transactional
    public ResultBody bidNotice(Map<String, Object> params) throws Exception {
        String biNo = (String) params.get("biNo");


        int rowsUpdated = generalDao.updateGernal("bid.updateTBiInfoMatA1", params);
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getUsername();

        if (rowsUpdated > 0) {
            Map<String, String> logParams = new HashMap<>();
            logParams.put("msg", "[본사]입찰공고");
            logParams.put("biNo", biNo);
            logParams.put("userId", userId);
            updateLog(logParams);
        }

		params.put("userId", userId);
		List<Object> sendList = generalDao.selectGernalList("bid.selectBidNoticeEmailList", params);
		
		if(sendList.size() > 0) {

			//문자 : 지명경쟁입찰일 때만
			/*
			if( "A".equals(CommonUtils.getString(params.get("biModeCode")))) {			
				
				try{
					for(Object sendObject : sendList) {
						Map<String,Object> sendMap = (Map<String, Object>) sendObject;
						messageService.send("일진그룹", sendMap.get("userHp").toString(),sendMap.get("userName").toString(), "[일진그룹 전자입찰시스템] 참여하신 입찰("+biNo+")이 공고되었습니다.\r\n확인바랍니다.", biNo);
					}
				}catch(Exception e) {
					log.error("noticeBid send message error : {}", e);
				}
			 }
			*/
			
			Map<String, Object> emailParam = new HashMap<String, Object>();

			emailParam.put("type", "notice");
			emailParam.put("biName", params.get("biName"));
			emailParam.put("interNm", params.get("interNm"));
			emailParam.put("reason", "");
			emailParam.put("sendList", sendList);
			emailParam.put("biNo", biNo);
			this.updateEmail(emailParam);
		}

		
        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    @Transactional
    public ResultBody updateBid(Map<String, Object> params,  
    		MultipartFile insFile, 
    		List<MultipartFile> innerFiles, 
    		List<MultipartFile> outerFiles
    		) throws Exception {

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getUsername();
        Map<String, Object> bidContent = (Map<String, Object>) params.get("bidContent");
        String biNo = (String) bidContent.get("biNo");
        String bdAmtStr = (String) bidContent.get("bdAmt");
        
        BigDecimal bdAmt = null;
        if (!bdAmtStr.isEmpty()) {
            bdAmt = new BigDecimal(bdAmtStr);
        }
        
        //입찰 업데이트
        bidContent.put("bdAmt", bdAmt);
        bidContent.put("userId", userId);
        generalDao.updateGernal("bid.updateTBiInfoMat", bidContent);
        

        //입찰 히스토리 insert
        bidContent.put("type", "update");
        generalDao.insertGernal("bid.insertTBiInfoMatHist", bidContent);
        
        //재 insert로 인해 미리 delete 
        generalDao.deleteGernal("bid.deleteTBiInfoMatCust", bidContent);
        
        // 지명경쟁 협력사 등록
        if( "A".equals(CommonUtils.getString(bidContent.get("biModeCode"))) ) {
	        Map<String, Object> custMap = new HashMap<String, Object>();
            List<Map<String, Object >> custContent = (List<Map<String, Object>>) params.get("custContent");
            List<Map<String, Object >> custUserInfo = (List<Map<String, Object>>) bidContent.get("custUserInfo");
            custMap.put("biNo", biNo);
            custMap.put("userId", userId);
                for (Map<String, Object> data : custContent) {
                	String custCode = data.get("custCode").toString();
                	
                	Optional<Map<String, Object>> userInfoOptional = custUserInfo.stream()
                            .filter(userInfo -> custCode.equals(userInfo.get("custCode")))
                            .findFirst();
                    
                    Map<String, Object> userInfo = userInfoOptional.get();
                    String usemailId = (String) userInfo.get("usemailId");
                    
                    custMap.put("custCode",custCode);
                    custMap.put("usemailId", usemailId);
                    
        	        //지명경쟁 협력사 insert
        	        generalDao.insertGernal("bid.insertTBiInfoMatCust", custMap);

                }

            }
    		
        	Map<String, Object> emailMap = new HashMap<String, Object>();
	        emailMap.put("userId", userId);
	        emailMap.put("gongoIdCode", (String) bidContent.get("gongoIdCode"));

	        //공고자 Email정도 조회
	        List<Object> sendList = generalDao.selectGernalList("bid.selectGongoEmailList", emailMap);
	        
            
			if(sendList.size() > 0) {
				
	            // 계열사명 가져오기
				String interNm = (String) generalDao.selectGernalObject("bid.selectInterrelatedNm", emailMap);
				
	            emailMap.put("type", "update");
	            emailMap.put("biName", (String) bidContent.get("biName"));
	            emailMap.put("interNm", interNm); // 계열사명
	            emailMap.put("reason", "");	// 입찰계획은 사유 없음
	            emailMap.put("sendList", sendList);	// 수신자 리스트
	            emailMap.put("biNo", biNo);	// 수신자 리스트
	            
	            this.updateEmail(emailMap);				
			}

        
        Map<String,Object> changeFileCheck = (Map<String, Object>) bidContent.get("changeFileCheck");
    	Map<String,Object> fileMap = new HashMap<>();
    	fileMap.put("biNo", biNo);
		
		int innerFilesSize = innerFiles.size();
		int outerFilesSize = outerFiles.size();
		
        // 첨부파일 대내용
		if(bidContent.containsKey("delInnerFilesAll")){
			
         	fileMap.put("fileFlag", "0");
            generalDao.deleteGernal("bid.deleteTBiUpload", fileMap);
            
		}else {
			if(innerFilesSize > 0) {
				
				for(MultipartFile innerFile : innerFiles) {
					this.saveFileBid(innerFile, biNo, "0", "0");
				}

			}
			
			if(bidContent.containsKey("delInnerFiles")) {
	         	fileMap.put("fileFlag", "0");
	         	fileMap.put("delInnerFiles", bidContent.get("delInnerFiles") );
	            generalDao.deleteGernal("bid.deleteTBiUpload", fileMap);
				
			}
		}
		
        // 첨부파일 대외용
		if(bidContent.containsKey("delOuterFilesAll")){
			
         	fileMap.put("fileFlag", "1");
            generalDao.deleteGernal("bid.deleteTBiUpload", fileMap);
            
		}else {
			if(outerFilesSize > 0) {
				
				for(MultipartFile outerFile : outerFiles) {
					this.saveFileBid(outerFile, biNo, "0", "1");
				}

			}
			
			if(bidContent.containsKey("delOuterFiles")) {
	         	fileMap.put("fileFlag", "1");
	         	fileMap.put("delOuterFiles", bidContent.get("delOuterFiles") );
	            generalDao.deleteGernal("bid.deleteTBiUpload", fileMap);
				
			}
		}
		
		
		/*
	
        if( innerFilesSize > 0) {
         	fileMap.put("fileFlag", "0");
            generalDao.deleteGernal("bid.deleteTBiUpload", fileMap);
        	 for(MultipartFile innerFile : innerFiles ) {
        		 this.saveFileBid(innerFile, biNo, "0", "0");
        	 }
        } 
        // 첨부파일 대외용
        if(outterFilesSize > 0) {
         	fileMap.put("fileFlag", "1");
            generalDao.deleteGernal("bid.deleteTBiUpload", fileMap);
        	 for(MultipartFile outerFile : outerFiles ) {
        		 this.saveFileBid(outerFile, biNo, "0", "1");
        		 
        	 }
        }
        */

        // 내역방식 - 파일등록
        if( "1".equals(CommonUtils.getString(bidContent.get("insModeCode")))){

            // 파일 등록 시 내역직접등록 내역은 삭제
            generalDao.deleteGernal("bid.deleteTBiSpecMat", fileMap);
            
        	String insFileCheck = bidContent.get("insFileCheck").toString();
        	
        	// Y는 기존에 있는 파일 그대로 저장되는 거라 따로 수정할 필요 없어서 Y 아닌 경우만 처리
        	if(!"Y".equals(insFileCheck)){
        		
            	fileMap.put("fileFlag", "K");
                generalDao.deleteGernal("bid.deleteTBiUpload", fileMap);
                
	        	if("C".equals(insFileCheck)) {
	            	this.saveFileBid(insFile, biNo, "0", "K");
	        	}
        	}

        } 
        // 내역방식 - 내역직접등록
        else if( "2".equals(CommonUtils.getString(bidContent.get("insModeCode"))) ) {
            
            //내역직접등록 시 파일직접등록입력 시 등록된 파일 삭제
        	fileMap.put("fileFlag", "K");
            generalDao.deleteGernal("bid.deleteTBiUpload", fileMap);
            
            int orderUc = 0;
            int orderQty = 0;
            
            generalDao.deleteGernal("bid.deleteTBiSpecMat", fileMap);
            
            List<Map<String, Object>> tableContent = (List<Map<String, Object>>)params.get("tableContent");
            Map<String,Object> tableContentMap = new HashMap<>();
            tableContentMap.put("biNo", biNo);
            tableContentMap.put("userId", userId);
            for (Map<String, Object> data : tableContent) {
            	// orderUc 값이 null이거나 비어 있는 경우 0으로 초기화
                orderUc = !StringUtils.isEmpty(data.get("orderUc")) ? Integer.parseInt(data.get("orderUc").toString()) : 0;
                // orderQty 값이 null이거나 비어 있는 경우 0으로 초기화
                orderQty = !StringUtils.isEmpty(data.get("orderQty")) ? Integer.parseInt(data.get("orderQty").toString()) : 0;
                
	            tableContentMap.put("seq", data.get("seq"));
	            tableContentMap.put("name", (String) data.get("name"));
	            tableContentMap.put("ssize", (String) data.get("ssize"));
	            tableContentMap.put("unitcode", (String) data.get("unitcode"));
	            tableContentMap.put("orderUc", orderUc);
	            tableContentMap.put("orderQty", orderQty);
                
                generalDao.insertGernal("bid.insertTBiSpectMat", tableContentMap);
            }
        }
        

        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    public String newBiNo() {

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());

        String interrelatedCode = userOptional.get().getInterrelatedCustCode();
        String biNoHeader = "";

        switch (interrelatedCode) {
            case "01":
                biNoHeader = "E";
                break;
            case "02":
                biNoHeader = "C";
                break;
            case "03":
                biNoHeader = "D";
                break;
            case "04":
                biNoHeader = "A";
                break;
            case "05":
                biNoHeader = "M";
                break;
            case "06":
                biNoHeader = "S";
                break;
            case "07":
                biNoHeader = "J";
                break;
            case "08":
                biNoHeader = "P";
                break;
            case "09":
                biNoHeader = "G";
                break;
            case "10":
                biNoHeader = "L";
                break;
            case "11":
                biNoHeader = "Z";
                break;
            case "12":
                biNoHeader = "T";
                break;
            case "13":
                biNoHeader = "K";
                break;
            case "14":
                biNoHeader = "N";
        }

        LocalDate currentDate = LocalDate.now();
        String biNoYear = currentDate.format(DateTimeFormatter.ofPattern("yyyy"));
        String biNoMonth = currentDate.format(DateTimeFormatter.ofPattern("MM"));

        String combinedBiNo = biNoHeader + biNoYear + biNoMonth;

        StringBuilder binoList = new StringBuilder( // 입찰번호 seq 조회
                "SELECT CONCAT(MAX(CAST(SUBSTRING(bi_no, LENGTH(bi_no) - 2) AS UNSIGNED) + 1)) FROM t_bi_info_mat WHERE bi_no LIKE concat(:combinedBiNo,'%')");
        Query biNoQ = entityManager.createNativeQuery(binoList.toString());
        biNoQ.setParameter("combinedBiNo", combinedBiNo);
        String seq = (String) biNoQ.getSingleResult();

        if (seq == null) {
            seq = "001";
        } else {
            // 3자리로 포맷팅
            seq = String.format("%03d", Integer.parseInt(seq));
        }
        String biNo = combinedBiNo + seq;
        return biNo;
    }
    
    /**
     * @param bidContent	입찰정보
     * @param custContent	지명경쟁 협력사 리스트
     * @param updateEmail	지명경쟁 협력사의 모든 대상자 이메일
     * @param tableContent	내역직접등록
     * @param insFile		파일직접등록
     * @param innerFile		대내용
     * @param outerFile		대외용
     * @return
     * @throws Exception 
     */
    @Transactional
    public ResultBody insertBid(Map<String, Object> params,  
    		MultipartFile insFile, 
    		List<MultipartFile> innerFiles, 
    		List<MultipartFile> outerFiles) throws Exception {
    	// 세션 가져오기
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        String userId = principal.getUsername();
        
        Map<String, Object> bidContent = (Map<String, Object>) params.get("bidContent");
        
        ResultBody resultBody = new ResultBody();
        
        if(!userId.equals(bidContent.get("userId").toString())) {
        	resultBody.setCode("LOGOUT");;
        }else {
        
	        // 입찰번호 생성
	        String biNo = this.newBiNo(); 
	        params.put("biNo", biNo);
	        // 예산금액...?
	        String bdAmtStr = (String) bidContent.get("bdAmt");
	        
	        BigDecimal bdAmt = null;
	        if (!bdAmtStr.isEmpty()) {
	            bdAmt = new BigDecimal(bdAmtStr);
	        }
	        bidContent.put("biNo", biNo);
	        bidContent.put("bdAmt", bdAmt);
	        bidContent.put("userId", userId);
	        bidContent.put("type", "insert");
	        //입찰정보 insert
	        generalDao.insertGernal("bid.insertTBiInfoMat", bidContent);
	        
	        //입찰 히스토리 insert
	        generalDao.insertGernal("bid.insertTBiInfoMatHist", bidContent);
	        
	        // 지명경쟁 협력사 등록

	        if( "A".equals(CommonUtils.getString(bidContent.get("biModeCode"))) ) {
		        Map<String, Object> custMap = new HashMap<String, Object>();
	            List<Map<String, Object >> custContent = (List<Map<String, Object>>) params.get("custContent");
	            List<Map<String, Object >> custUserInfo = (List<Map<String, Object>>) bidContent.get("custUserInfo");
                custMap.put("biNo", biNo);
                custMap.put("userId", userId);
	                for (Map<String, Object> data : custContent) {
	                	String custCode = (String) data.get("custCode");
	                	
	                	Optional<Map<String, Object>> userInfoOptional = custUserInfo.stream()
	                            .filter(userInfo -> custCode.equals(userInfo.get("custCode")))
	                            .findFirst();
	                    
	                    Map<String, Object> userInfo = userInfoOptional.get();
	                    String usemailId = (String) userInfo.get("usemailId");
	                    
	                    custMap.put("custCode",custCode);
	                    custMap.put("usemailId", usemailId);
	                    
	        	        //지명경쟁 협력사 insert
	        	        generalDao.insertGernal("bid.insertTBiInfoMatCust", custMap);
	        	        
	                }
	        }

	        Map<String, Object> emailMap = new HashMap<String, Object>();
	        emailMap.put("userId", userId);
	        emailMap.put("gongoIdCode", (String) bidContent.get("gongoIdCode"));

	        List<Object> sendList = generalDao.selectGernalList("bid.selectGongoEmailList", emailMap);
	        
			if(sendList.size() > 0) {

		        // 계열사명 가져오기
				String interNm = (String) generalDao.selectGernalObject("bid.selectInterrelatedNm", emailMap);
				
		        emailMap.put("type", "insert"); // 입찰 이벤트 타입
		        emailMap.put("biName", (String) bidContent.get("biName")); //입찰 이름
		        emailMap.put("interNm", interNm); // 계열사명
		        emailMap.put("reason", "");	// 입찰계획은 사유 없음
		        emailMap.put("sendList", sendList);	// 수신자 리스트
		        emailMap.put("biNo", biNo);	// 수신자 리스트
		        
		        this.updateEmail(emailMap);
			}
	
			int innerFilesSize = innerFiles.size();
			int outerFilesSize = outerFiles.size();
			
			//첨부파일 대내용
	        if( innerFilesSize > 0) {
	        	 for(MultipartFile innerFile : innerFiles ) {
	        		 this.saveFileBid(innerFile, biNo, "0", "0");
	        	 }
	        } 
	        // 첨부파일 대외용
	        if(outerFilesSize > 0) {
	        	 for(MultipartFile outerFile : outerFiles ) {
	        		 this.saveFileBid(outerFile, biNo, "0", "1");
	        	 }
	        }
	        
	        // 내역방식 - 파일등록
	        if( "1".equals(CommonUtils.getString(bidContent.get("insModeCode")))){

	            // 파일직접입력 
	            this.saveFileBid(insFile, biNo, "0", "K");
	        } 
	        // 내역방식 - 내역직접등록
	        else if( "2".equals(CommonUtils.getString(bidContent.get("insModeCode"))) ) {
	        	//this.updateBidItem();
	            int orderUc = 0;
	            int orderQty = 0;
	
	            List<Map<String, Object>> tableContent = (List<Map<String, Object>>)params.get("tableContent");
	            Map<String,Object> tableContentMap = new HashMap<>();
	            tableContentMap.put("biNo", biNo);
	            tableContentMap.put("userId", userId);
	            
	            for (Map<String, Object> data : tableContent) {
	            	// orderUc 값이 null이거나 비어 있는 경우 0으로 초기화
	                orderUc = !StringUtils.isEmpty(data.get("orderUc")) ? Integer.parseInt(data.get("orderUc").toString()) : 0;
	                // orderQty 값이 null이거나 비어 있는 경우 0으로 초기화
	                orderQty = !StringUtils.isEmpty(data.get("orderQty")) ? Integer.parseInt(data.get("orderQty").toString()) : 0;
	                	                
		            tableContentMap.put("seq", data.get("seq"));
		            tableContentMap.put("name", (String) data.get("name"));
		            tableContentMap.put("ssize", (String) data.get("ssize"));
		            tableContentMap.put("unitcode", (String) data.get("unitcode"));
		            tableContentMap.put("orderUc", orderUc);
		            tableContentMap.put("orderQty", orderQty);
		            	                
	                generalDao.insertGernal("bid.insertTBiSpectMat", tableContentMap);
	            }
	        }
        }

        return resultBody;
    }
    private void saveFileBid( MultipartFile file, String biNo, String fCustCode, String fileFlag ) throws Exception {
        String filePath = null;
        String fileNm = null;
        Map<String,Object> fileMap = new HashMap<>();
        if (file != null) {
            // 첨부파일 등록
            filePath = fileService.uploadEncryptedFile(file);

            // 원래 파일명
            fileNm = file.getOriginalFilename();
            
            fileMap.put("biNo", biNo);
            fileMap.put("fileFlag", fileFlag);
            fileMap.put("fCustCode", fCustCode);
            fileMap.put("fileNm", fileNm);
            fileMap.put("filePath", filePath);
            
            generalDao.insertGernal("bid.insertTBiUpload", fileMap);
        }
    }

    @Transactional
    public ResultBody delete(Map<String, Object> params) throws Exception {
    	
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getUsername();
        String biNo = (String) params.get("biNo");
        ResultBody resultBody = new ResultBody();
        
        params.put("userId", userId);
        // 입찰서내용 삭제가 아닌 ING_TAG 를 D 로 업데이트 처리
        generalDao.updateGernal("bid.deleteTBiInfoMat", params);

    	params.put("userId", params.get("cuserCode"));
		
        List<Object> sendList = generalDao.selectGernalList("bid.selectGongoEmailList", params);
        
		if(sendList.size() > 0) {
	        Map<String, Object> emailMap = new HashMap<String, Object>();
	        emailMap.put("type", "del");
	        emailMap.put("biName", (String) params.get("biName"));
	        emailMap.put("reason", (String) params.get("reason")); // 삭제사유
	        emailMap.put("interNm", params.get("interNm"));
	        emailMap.put("sendList", sendList);	// 수신자 리스트
	        emailMap.put("biNo", biNo);	// 수신자 리스트
	        
	        this.updateEmail(emailMap);
		}

        return resultBody;
    }
    
	/**
	 * 메일전송
	 * @param params : (String) type, (String) biName, (String) reason, (String) interNm, (List<SendDto>) sendList
	 * @throws Exception 
	 */
	public void updateEmail(Map<String, Object> params) throws Exception {
		List<Map<String,Object>> sendList = (List<Map<String, Object>>) params.get("sendList");		//수신인/발송인 메일 리스트

		//메일 내용 셋팅
		Map<String, String> emailContent = this.emailContent(params);
		params.put("title", emailContent.get("title"));
		params.put("content", emailContent.get("content"));
		if (sendList != null) {
			for (Map<String,Object> recvInfo : sendList) {
				params.put("userEmail", recvInfo.get("userEmail"));
				params.put("fromMail", recvInfo.get("fromEmail"));
				generalDao.insertGernal("bid.insertTEmail", params);
			}
		}
	}
		
	//메일 제목 및 내용 셋팅
	public Map<String, String> emailContent(Map<String, Object> params){
		Map<String, String> result = new HashMap<String, String>();
		
		String type = CommonUtils.getString(params.get("type"));			// del : 입찰삭제 , notice : 입찰공고, insert: 입찰등록, fail: 유찰, rebid:재입찰,succ:낙찰
		String biName = CommonUtils.getString(params.get("biName"));		//입찰명
		String reason = CommonUtils.getString(params.get("reason"));		//사유
		String interNm = CommonUtils.getString(params.get("interNm"));		//계열사명
		
		String title = "";
		String content = "";
		
		//입찰 계획 삭제
		if(type.equals("del")) {
			title = "[일진그룹 e-bidding] 입찰 계획 삭제(" + biName + ")";
			content = "입찰명 [" + biName + "] 입찰계획을\n삭제하였습니다.\n아래 삭제사유를 확인해 주십시오.\n\n"+
						"-삭제사유\n" + reason;
			
		//입찰 공고
		}else if(type.equals("notice")) {
			title = "[일진그룹 e-bidding] 입찰 공고(" + biName + ")";
			content = "[" + interNm + "]에서 입찰공고 하였습니다.\n입찰명은 [" + biName + "] 입니다.\n\n";

			
		//입찰 계획 등록
		}else if(type.equals("insert")) {
			title = "[일진그룹 e-bidding] 계획 등록(" + biName + ")";
			content = "[" + interNm + "]에서 입찰계획을 등록하였습니다.\n입찰명은 [" + biName + "] 입니다.\n\n";

		//입찰 계획 수정
		}else if(type.equals("update")) {
			title = "[일진그룹 e-bidding] 계획 수정(" + biName + ")";
			content = "[" + interNm + "]에서 입찰계획을 수정하였습니다.\n입찰명은 [" + biName + "] 입니다.\n\n";
	
		//입찰 유찰처리
		}else if(type.equals("fail")) {
			title = "[일진그룹 e-bidding] 유찰 처리(" + biName + ")";
			content = "입찰명 [" + biName + "]를 유찰처리 하였습니다.\n" +
					"아래 유찰사유를 확인해 주십시오.\n\n"+
					"-유찰사유\n" + reason;
			
		//재입찰
		}else if(type.equals("rebid")) {
			title = "[일진그룹 e-bidding] 재입찰(" + biName + ")";
			content = "입찰명 [" + biName + "]이 재입찰되었습니다.\n" +
					"아래 재입찰사유를 확인해 주시고 e-bidding 시스템에 로그인하여 다시 한번 투찰해 주십시오\n\n"+
					"-재입찰사유\n" + reason;
			
		//낙찰
		}else if(type.equals("succ")) {
			title = "[일진그룹 e-bidding] 낙찰(" + biName + ")";
			content = "입찰명 [" + biName + "]에 업체선정 되었습니다.\n" +
					"자세한 사항은 e-bidding 시스템에  로그인하여 입찰내용 확인 및 낙찰확인을 하시기 바랍니다.\n(낙찰확인은 계약과 관련없는 내부절차 입니다.)\n\n"+
					"-추가합의사항\n" + reason;
		
		//입찰독촉메일
		}else if(type.equals("push")) {
			title = "[일진그룹 e-bidding] 입찰 마감임박";
			content = "["+interNm+"]에서 공고한 [" + biName + "] 입찰 마감시간이 다가오고 있습니다.\r\n"
					+ "마감시간 전 전자입찰 e-bidding 시스템에 접속하여 투찰을 진행해 주십시오\r\n"
					+ "투찰기간 : "+ CommonUtils.getString(params.get("estStartDate")) + " ~ " + CommonUtils.getString(params.get("estCloseDate"));
		}
		
		result.put("title", title);
		result.put("content", content);
		return result;
	}

    /**
     * 입찰 로그
     * @param params
     */
    @Transactional
    public void updateLog(Map<String, String> params) {
        String msg = params.get("msg");
        String biNo = params.get("biNo");
        String userId = params.get("userId");

        StringBuilder sbList = new StringBuilder(
                "INSERT INTO t_bi_log (bi_no, user_id, log_text, create_date) VALUES " +
                        "(:biNo, :userId, :msg, sysdate())");

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("msg", msg);
        queryList.setParameter("biNo", biNo);
        queryList.setParameter("userId", userId);
        queryList.executeUpdate();
    }

    // 첨부파일 다운로드
    public ByteArrayResource downloadFile(Map<String, Object> params) {

        String filePath = (String) params.get("fileId");
        ByteArrayResource fileResource = null;

        try {
            fileResource = fileService.downloadDecryptedFile(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileResource;
    }
    
    public ResultBody progressCodeList() throws Exception {
        
		ResultBody resultBody = new ResultBody();

		List<Object> list = generalDao.selectGernalList("bid.selectProgressCodeList", null);
		
		resultBody.setData(list);
       return resultBody;
       
    }
    

}