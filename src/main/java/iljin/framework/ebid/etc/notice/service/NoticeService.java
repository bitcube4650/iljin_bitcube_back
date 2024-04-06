package iljin.framework.ebid.etc.notice.service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.ebid.bid.dto.InterUserInfoDto;
import iljin.framework.ebid.bid.service.BidProgressService;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.notice.dto.NoticeDto;
import iljin.framework.ebid.etc.notice.entity.TCoBoardCustCode;
import iljin.framework.ebid.etc.notice.entity.TCoBoardNotice;
import iljin.framework.ebid.etc.notice.repository.TCoBoardCustRepository;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.PagaUtils;
import iljin.framework.ebid.etc.util.common.file.FileService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NoticeService {
	
	@Autowired
	private TCoBoardCustRepository tCoBoardCustCodeRepository; 
	
	@Autowired
    private TCoUserRepository tCoUserRepository;
	
	@Autowired
	private FileService fileService;
	
	@Autowired
    private BidProgressService bidProgressService;
	
	@PersistenceContext
    private EntityManager entityManager;
	
	@Value("${file.upload.directory}")
    private String uploadDirectory;

	
	//공지사항 목록 조회
	@Transactional
	public Page noticeList(Map<String, Object> params, CustomUserDetails user) {
  
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = principal.getUsername();
		String userAuth = "";// userAuth(1 = 시스템관리자, 2 = 각사관리자, 3 = 일반사용자, 4 = 감사사용자)
		String interrelatedCode = "";
		String company = "";//계열사(inter)인지 협력사(cust)인지
		String requestPage = "";//공지사항 페이지에서 요청했는지, 메인화면에서 요청했는지(계열사 메인에서 요청했으면 - main)
								//계열사 시스템 관리자가 로그인했으면 메인화면에는 공통공지와 로그인 한 유저의 계열사와 관련된 계열사 공지만 출력(메인화면에는 유저 계열사 관련해서 정보들이 나오니 일관성 유지를 위해)
								//계열사 시스템 관리자가 로그인했으면 공지사항 목록페이지에는 공통공지와 모든 계열사 공지가 출력 
		
		List<InterUserInfoDto> userInfoList = new ArrayList<>(); 
		List<String> custCodes = new ArrayList<>();
		if (!StringUtils.isEmpty(params.get("requestPage"))) {
			requestPage = (String) params.get("requestPage");
		}

		StringBuilder sbFrom = new StringBuilder();
		StringBuilder sbWhere = new StringBuilder();
		
		// from 절
		String sbFromStr = "";
		sbFromStr += "FROM t_co_board_notice tcbn\n";
		if(userOptional.isPresent() && "1".equals(userOptional.get().getUserAuth())) {		// 계열사 관리자 중 시스템 관리자
			
		} else {																			// 협력사 사용자 및 계열사 관리자(각사관리자, 일반관리자, 감사사용자)
			sbFromStr += "LEFT OUTER JOIN t_co_board_cust tcbc\n";
			sbFromStr += "	ON tcbn.B_NO = tcbc.B_NO\n";
			
			if(!userOptional.isPresent()) {													// 협력사 사용자
				
				sbFromStr += "LEFT OUTER JOIN t_co_cust_ir tcci\n";
				sbFromStr += "	ON tcbc.INTERRELATED_CUST_CODE = tcci.INTERRELATED_CUST_CODE\n";
			}
		}

		sbFromStr += "LEFT OUTER JOIN t_co_user tcu\n";
		sbFromStr += "	ON tcbn.B_USERID = tcu.USER_ID\n";
		sbFromStr += "WHERE 1=1\n";

		sbFrom.append(sbFromStr);
		
		// where 절
		String sbWhereStr = "";
		if(!"".equals(CommonUtils.getString(params.get("title")))) {
			sbWhereStr += "	AND tcbn.B_TITLE LIKE CONCAT('%',:title,'%')\n";
		}
		if(!"".equals(CommonUtils.getString(params.get("content")))) {
			sbWhereStr += "	AND tcbn.B_CONTENT LIKE CONCAT('%',:content,'%')\n";
		}
		if(!"".equals(CommonUtils.getString(params.get("userName")))) {
			sbWhereStr += "	AND tcu.USER_NAME LIKE CONCAT('%',:userName,'%')\n";
		}
		if(!"".equals(CommonUtils.getString(params.get("bno")))) {
			sbWhereStr += "	AND tcbn.B_NO = :bno\n";
		}
		
		if(userOptional.isPresent() && "1".equals(userOptional.get().getUserAuth())) {
			// 계열사 관리자 중 시스템 관리자는 모든 공지 조회
		} else {																			// 협력사 사용자 및 계열사 관리자(각사관리자, 일반관리자, 감사사용자)
			sbWhereStr += "	AND (tcbn.B_CO = 'ALL' OR (tcbn.B_CO = 'CUST'";
			
			if(!userOptional.isPresent()) {													// 협력사 사용자
				// 본인이 속한 협력사의 계열사만 보이도록 처리
				sbWhereStr += " AND tcbc.INTERRELATED_CUST_CODE IN (SELECT INTERRELATED_CUST_CODE from t_co_cust_ir tcci  where CUST_CODE = '"+ user.getCustCode() +"')";
			} else {
				// 감사사용자는 매핑된 계열사 모두 보이도록 처리
				if("4".equals(userOptional.get().getUserAuth())){
					sbWhereStr += " and tcbc.INTERRELATED_CUST_CODE IN (SELECT INTERRELATED_CUST_CODE from t_co_user_interrelated tcui where tcui.USER_ID = '"+ userOptional.get().getUserId()+"')";
				} else {
				// 그 외 계약사 관리자(각사관리자, 일반관리자) 본인 소속 계열사 공지 조회
					sbWhereStr += " and tcbc.INTERRELATED_CUST_CODE = '"+ userOptional.get().getInterrelatedCustCode() +"'";
				}
			}
			sbWhereStr += "	)\n";
			sbWhereStr += ")\n";
		}
		sbWhereStr += "GROUP BY tcbn.B_NO\n";
		sbWhere.append(sbWhereStr);
			
			

		try {
			StringBuilder sbList = new StringBuilder("SELECT tcbn.b_no as bNo\n"
													+",	tcbn.b_userid as bUserid\n"
													+",	tcbn.b_title as bTitle\n"
													+",	DATE_FORMAT(tcbn.b_date , '%Y-%m-%d %H:%i') as bDate\n"
													+",	tcbn.b_count as bCount\n"
													+",	tcbn.b_file as bFile\n"
													+",	tcbn.b_content as bContent\n"
													+",	tcbn.b_file_path as bFilePath\n"
													+",	tcbn.b_co as bCo\n"
													+",	tcu.user_name as bUserName\n"
													+",	row_number() over( order by tcbn.b_date desc ) as rowNo\n"
													+sbFrom
													+sbWhere
													+"ORDER BY tcbn.B_DATE desc"
			);

			StringBuilder sbCount = new StringBuilder("SELECT COUNT(1)\n"
														+"FROM ("
														+sbList
														+") A"
			);
			
			Query queryList = entityManager.createNativeQuery(sbList.toString());
			Query queryTotal = entityManager.createNativeQuery(sbCount.toString());

			//공지사항 제목
			if (!StringUtils.isEmpty(params.get("title"))) {
				queryList.setParameter("title", params.get("title"));
				queryTotal.setParameter("title", params.get("title"));
			}
			
			//공지사항 내용
			if (!StringUtils.isEmpty(params.get("content"))) {
				queryList.setParameter("content", params.get("content"));
				queryTotal.setParameter("content", params.get("content"));
			}
			//공지사항 작성자
			if (!StringUtils.isEmpty(params.get("userName"))) {
				queryList.setParameter("userName", params.get("userName"));
				queryTotal.setParameter("userName", params.get("userName"));
			}
			//공지사항 id
			if (!StringUtils.isEmpty(params.get("bno"))) {
				queryList.setParameter("bno", params.get("bno"));
				queryTotal.setParameter("bno", params.get("bno"));
			}
			
			Pageable pageable = PagaUtils.pageable(params);
			queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();
			List list = new JpaResultMapper().list(queryList, NoticeDto.class);
			
			BigInteger count = (BigInteger) queryTotal.getSingleResult();
			return new PageImpl(list, pageable, count.intValue());
		}catch(Exception e) {
			e.printStackTrace(); 
			return new PageImpl(new ArrayList<>());
		}
		
        
    }

	//조회수 +1
	@Transactional
	public ResultBody updateClickNum(Map<String, Object> params) {

		ResultBody resultBody = new ResultBody();

	    try {
	        int bno = (int) params.get("bno");

	        TCoBoardNotice notice = entityManager.find(TCoBoardNotice.class, bno);
	        if (notice != null) {
	            int beforeCnt = notice.getBCount();
	            int afterCnt = beforeCnt + 1;
	            notice.setBCount(afterCnt);
	        } else {
	            resultBody.setCode("NOT_FOUND");
	            resultBody.setStatus(404);
	            resultBody.setMsg("Notice not found with bno: " + bno);
	        }

	    } catch (Exception e) {
	    	e.printStackTrace();
	        resultBody.setCode("ERROR");
	        resultBody.setStatus(500);
	        resultBody.setMsg("An error occurred while updating the click count.");
	        resultBody.setData(e.getMessage()); 
	    }
		return resultBody;
		
	}

	//해당 공지사항 공개되는 계열사 리스트 조회
	@Transactional
	public List<TCoBoardCustCode> selectGroupList(Map<String, Object> params) {
		List<TCoBoardCustCode> list = new ArrayList<>();

		try {
			//공지사항 id
	        int bno = (int) params.get("bno");
	        //공지계열사 정보
			list = tCoBoardCustCodeRepository.findBybNo(bno);
			
												
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}

	//공지사항 삭제
	@Transactional
	public ResultBody deleteNotice(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();

		
		try {
			//공지사항 id
	        int bno = (int) params.get("bno");

	        //공지사항 정보
	        TCoBoardNotice notice = entityManager.find(TCoBoardNotice.class, bno);
	        //공지계열사 정보
	        List<TCoBoardCustCode> list = tCoBoardCustCodeRepository.findBybNo(bno);
	        
	        if (notice != null) {
	        	//공지사항 정보 삭제
	            entityManager.remove(notice);
	            //공지계열사 정보 삭제
	            tCoBoardCustCodeRepository.deleteAll(list);
	            
	        } else {
	            resultBody.setCode("NOT_FOUND");
	            resultBody.setStatus(404);
	            resultBody.setMsg("Notice not found with bno: " + bno);
	        }

	    } catch (Exception e) {
	    	e.printStackTrace();
	        resultBody.setCode("ERROR");
	        resultBody.setStatus(500);
	        resultBody.setMsg("An error occurred while deleting the notice.");
	        resultBody.setData(e.getMessage()); 
	    }

		
		return resultBody;
	}
	
	//공지사항 수정
	@Transactional
	public ResultBody updateNotice(MultipartFile file, Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> tCoUser = tCoUserRepository.findById(principal.getUsername());
        TCoUser user = null;
        
        if(tCoUser.isPresent()) {
        	user = tCoUserRepository.findById(principal.getUsername()).get();//로그인한 유저정보
        }
        
	    try {
	
	    	//받아온 파라미터
	        int bno = (int) params.get("bno");
	        String bco = (String) params.get("bco");
	        String btitle = (String) params.get("btitle");
	        String bcontent = (String) params.get("bcontent");
	        String buserid = user.getUserId();
	        String preUploadedPath = (String) params.get("bfilePath");
	        String preFileName = (String) params.get("bfile");
	        String uploadedPath = null;
	        String fileName = null;
	        
	        @SuppressWarnings("unchecked")
			ArrayList<String> custCodeList=  (ArrayList<String>) params.get("interrelatedCustCodeArr");//수정된 공지 계열사 정보
	
	        //공지되는 계열사 수정
	        //기존 공지하는 계열사 정보 DELETE
	    	List<TCoBoardCustCode> list = tCoBoardCustCodeRepository.findBybNo(bno);
	    	tCoBoardCustCodeRepository.deleteAll(list);

	    	//수정된 공지 계열사 정보 INSERT
	    	for(int i = 0 ; i < custCodeList.size() ; i++) {

	    		TCoBoardCustCode newGroup = new TCoBoardCustCode();
	    		newGroup.setBNo(bno);
	    		newGroup.setInterrelatedCustCode(custCodeList.get(i));
	    		
	    		entityManager.persist(newGroup);
	    		
	    	}
	        
	        //공지사항 UPDATE
	        TCoBoardNotice notice = entityManager.find(TCoBoardNotice.class, bno);
	        
	        if (notice != null) {
	        	//파라미터 set
	            notice.setBTitle(btitle);
	            notice.setBContent(bcontent);
	            notice.setBCo(bco);
	            notice.setBUserid(buserid);
	            
	            if(file != null) {//새로 첨부된 파일이 있는 경우
	            	
	            	//첨부파일 등록
	    	    	uploadedPath = fileService.uploadFile(file);
	    	
	                //원래 파일명
	                fileName = file.getOriginalFilename();
	                
	            }else if(preUploadedPath != null && preFileName != null) {//기존에 첨부했던 파일이 그대로 있는 경우
	            	
	            	uploadedPath = preUploadedPath;
	            	fileName = preFileName;
	    
	            }
	            
	            notice.setBFile(fileName);
	            notice.setBFilePath(uploadedPath);
	            
	            LocalDateTime currentDate = LocalDateTime.now();
	            notice.setBDate(currentDate);
	 
	        } else {
	            resultBody.setCode("NOT_FOUND");
	            resultBody.setStatus(404);
	            resultBody.setMsg("Notice not found with bno: " + bno);
	        }
	       

	    } catch (Exception e) {
	    	e.printStackTrace();
	        resultBody.setCode("ERROR");
	        resultBody.setStatus(500);
	        resultBody.setMsg("An error occurred while updating notice.");
	        resultBody.setData(e.getMessage()); 
	    }
		return resultBody;
	}

	//공지사항 등록
	@Transactional
	public ResultBody insertNotice(MultipartFile file, Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		try {
			TCoBoardNotice notice = new TCoBoardNotice();
			LocalDateTime currentDate = LocalDateTime.now();
			
	    	//받아온 파라미터
	        String bco = (String) params.get("bco");
	        String btitle = (String) params.get("btitle");
	        String bcontent = (String) params.get("bcontent");
	        String buserid = (String) params.get("buserid");
	        String uploadedPath = null;
	        String fileName = null;
	        
	        if(file != null) {
            	//첨부파일 등록
    	    	uploadedPath = fileService.uploadFile(file);
    	
                //원래 파일명
                fileName = file.getOriginalFilename();
            }
	        
	        //파라미터 set
	        notice.setBCo(bco);
            notice.setBTitle(btitle);
            notice.setBContent(bcontent);
            notice.setBUserid(buserid);
            notice.setBDate(currentDate);
            notice.setBCount(0);
            notice.setBFile(fileName);
            notice.setBFilePath(uploadedPath);
	       
            entityManager.persist(notice);
            
            Integer bno = notice.getBNo();
	        
	        if( bco.equals("CUST") ) {//계열사 공지인 경우
	        	@SuppressWarnings("unchecked")
				ArrayList<String> custCodeList = (ArrayList<String>) params.get("interrelatedCustCodeArr");//등록할 공지 계열사 정보
	        	
	        	//수정된 공지 계열사 정보 INSERT
		    	for(int i = 0 ; i < custCodeList.size() ; i++) {

		    		TCoBoardCustCode newGroup = new TCoBoardCustCode();
		    		newGroup.setBNo(bno);
		    		newGroup.setInterrelatedCustCode(custCodeList.get(i));
		    		
		    		entityManager.persist(newGroup);
		    		
		    	}
	        }
	        

	    } catch (Exception e) {
	    	e.printStackTrace();
	        resultBody.setCode("ERROR");
	        resultBody.setStatus(500);
	        resultBody.setMsg("An error occurred while inserting notice.");
	        resultBody.setData(e.getMessage()); 
	    }
		return resultBody;
	}

	//첨부파일 다운로드
	public ByteArrayResource downloadFile(Map<String, Object> params) {
		
		String filePath = (String) params.get("fileId");
		ByteArrayResource fileResource = null;
		
		try {
			fileResource = fileService.downloadFile(filePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return fileResource;
	}


}
