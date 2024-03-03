package iljin.framework.ebid.etc.notice.service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transaction;
import javax.transaction.Transactional;

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
import org.springframework.web.multipart.MultipartFile;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.notice.dto.NoticeDto;
import iljin.framework.ebid.etc.notice.dto.TCoBoardCustDto;
import iljin.framework.ebid.etc.notice.entity.TCoBoardCustCode;
import iljin.framework.ebid.etc.notice.entity.TCoBoardCustID;
import iljin.framework.ebid.etc.notice.entity.TCoBoardNotice;
import iljin.framework.ebid.etc.notice.repository.TCoBoardCustRepository;
import iljin.framework.ebid.etc.util.PagaUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NoticeService {
	
	@Autowired
	private TCoBoardCustRepository tCoBoardCustCodeRepository; 
	
	@Autowired
    private TCoUserRepository tCoUserRepository;
	
	@PersistenceContext
    private EntityManager entityManager;
	
	@Value("${file.upload.directory}")
    private String uploadDirectory;

	
	//공지사항 목록 조회
	@Transactional
	public Page noticeList(Map<String, Object> params) {
  
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userAuth = "";

        if (userOptional.isPresent()) {//계열사인 경우
        	// userAuth(1 = 시스템관리자, 2 = 각사관리자, 3 = 일반사용자, 4 = 감사사용자)
        	userAuth = userOptional.get().getUserAuth();
        }
 
		try {
			StringBuilder sbCount = new StringBuilder(" select count(1) "
									        		 +" from ( "
													 	     + " ( "
													 	    	 + " select tcbn.b_no , "
													 	    		    + " tcbn.b_userid , "
													 	    		    + " tcbn.b_title , "
													 	    		    + " tcbn.b_date , "
													 	    		    + " tcbn.b_count , "
													 	    		    + " tcbn.b_file , "
													 	    		    + " tcbn.b_content , "
													 	    		    + " tcbn.b_file_path , "
													 	    		    + " tcbn.b_co , "
													 	    	        + " tcu.user_name as userName "
															     + " from t_co_board_notice tcbn "
															     + " left outer join t_co_user tcu "
															     + " on (tcbn.b_userid = tcu.user_id) "
															     + " where tcbn.b_co = 'ALL' "
														     + " ) "
															 + " union all "
															 + " ("
															     + " select distinct "
															     		+ " tcbn2.b_no , "
															    	    + " tcbn2.b_userid , "
															    	    + " tcbn2.b_title , "
															    	    + " tcbn2.b_date , "
															    	    + " tcbn2.b_count , "
															    	    + " tcbn2.b_file , "
															    	    + " tcbn2.b_content , "
															    	    + " tcbn2.b_file_path , "
															    	    + " tcbn2.b_co , "
															    	    + " tcu2.user_name as userName "
															     + " from t_co_board_notice tcbn2 "
															     + " inner join t_co_board_cust tcbc "
															     + " on(tcbn2.b_no = tcbc.b_no) "
															     + " left outer join t_co_user tcu2 "
															     + " on (tcbn2.b_userid = tcu2.user_id) "
															     + " where tcbn2.b_co = 'CUST' "
													 );
			if(!userAuth.equals("1")) {
				//시스템관리자가 아닌 경우 계열사 공지 조건 추가
				//협력사는 custCode가 빈문자열로 검색되어 계열사 공지가 출력안됨
				sbCount.append(
																   " and tcbc.interrelated_cust_code = :custCode "
							  );
			}
			
			sbCount.append(
															   " ) "
														 + " ) rst "
													+ " where 1=1 "
						  );
			
											
								
			StringBuilder sbList = new StringBuilder(" select rst.b_no , "
														  + " rst.b_userid , "
														  + " rst.b_title , "
														  + " DATE_FORMAT( rst.b_date , '%Y-%m-%d %H:%i') as bDate, "
														  + " rst.b_count , "
														  + " rst.b_file , "
														  + " rst.b_content , "
														  + " rst.b_file_path , "
														  + " rst.b_co , "
														  + " rst.userName , "
														  + " row_number() over( order by rst.b_date desc ) as rowNo "
									        		 +" from ( "
													 	     + " ( "
													 	    	 + " select tcbn.b_no , "
													 	    		    + " tcbn.b_userid , "
													 	    		    + " tcbn.b_title , "
													 	    		    + " tcbn.b_date , "
													 	    		    + " tcbn.b_count , "
													 	    		    + " tcbn.b_file , "
													 	    		    + " tcbn.b_content , "
													 	    		    + " tcbn.b_file_path , "
													 	    		    + " tcbn.b_co , "
													 	    	        + " tcu.user_name as userName "
															     + " from t_co_board_notice tcbn "
															     + " left outer join t_co_user tcu "
															     + " on (tcbn.b_userid = tcu.user_id) "
															     + " where tcbn.b_co = 'ALL' "
														     + " ) "
															 + " union all "
															 + " ("
															     + " select distinct "
															     	 	+ " tcbn2.b_no , "
															    	    + " tcbn2.b_userid , "
															    	    + " tcbn2.b_title , "
															    	    + " tcbn2.b_date , "
															    	    + " tcbn2.b_count , "
															    	    + " tcbn2.b_file , "
															    	    + " tcbn2.b_content , "
															    	    + " tcbn2.b_file_path , "
															    	    + " tcbn2.b_co , "
															    	    + " tcu2.user_name as userName "
															     + " from t_co_board_notice tcbn2 "
															     + " inner join t_co_board_cust tcbc "
															     + " on(tcbn2.b_no = tcbc.b_no) "
															     + " left outer join t_co_user tcu2 "
															     + " on (tcbn2.b_userid = tcu2.user_id) "
															     + " where tcbn2.b_co = 'CUST' "
													 );
			if(!userAuth.equals("1")) {
				//시스템관리자가 아닌 경우 계열사 공지 조건 추가
				//협력사는 custCode가 빈문자열로 검색되어 계열사 공지가 출력안됨
				sbList.append(
																   " and tcbc.interrelated_cust_code = :custCode "
							  );
			}
			
			sbList.append(
															   " ) "
														 + " ) rst "
													+ " where 1=1 "
						  );
			StringBuilder sbWhere = new StringBuilder();
			
			if (!StringUtils.isEmpty(params.get("title"))) {
			sbWhere.append(" and rst.b_title like concat('%',:title,'%')");
			}
			if (!StringUtils.isEmpty(params.get("content"))) {
			sbWhere.append(" and rst.b_content like concat('%',:content,'%')");
			}
			if (!StringUtils.isEmpty(params.get("userName"))) {
			sbWhere.append(" and rst.userName like concat('%',:userName,'%')");
			}
			
			sbList.append(sbWhere);
			sbList.append(" order by b_date desc");
			Query queryList = entityManager.createNativeQuery(sbList.toString());
			sbCount.append(sbWhere);
			Query queryTotal = entityManager.createNativeQuery(sbCount.toString());
			
			if(!userAuth.equals("1")) {//시스템관리자가 아닌 경우
				//어느 계열사인지(협력사의 경우 빈 문자열 검색)
				queryList.setParameter("custCode", params.get("custCode"));
				queryTotal.setParameter("custCode", params.get("custCode"));
			}
			
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
        TCoUser user = tCoUserRepository.findById(principal.getUsername()).get();//로그인한 유저정보
        
	    try {
	
	    	//받아온 파라미터
	        int bno = (int) params.get("bno");
	        String bco = (String) params.get("bco");
	        String btitle = (String) params.get("btitle");
	        String bcontent = (String) params.get("bcontent");
	        String buserid = user.getUserId();
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
	            
	            if(file != null) {
	            	//첨부파일 등록
	    	    	uploadedPath = uploadFile(file, uploadDirectory);
	    	
	                //원래 파일명
	                fileName = file.getOriginalFilename();
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
	public ResultBody insertNotice(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		try {
			TCoBoardNotice notice = new TCoBoardNotice();
			LocalDateTime currentDate = LocalDateTime.now();
			
	    	//받아온 파라미터
	        String bco = (String) params.get("bco");
	        String btitle = (String) params.get("btitle");
	        String bcontent = (String) params.get("bcontent");
	        String buserid = (String) params.get("buserid");
	        
	        //파라미터 set
	        notice.setBCo(bco);
            notice.setBTitle(btitle);
            notice.setBContent(bcontent);
            notice.setBUserid(buserid);
            notice.setBDate(currentDate);
            notice.setBCount(0);
	       
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

	// 파일 업로드 메서드
    public static String uploadFile(MultipartFile file, String uploadDirectory) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }

        // 현재 날짜를 기준으로 연도와 월 폴더 생성
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
        Date currentDate = new Date();
        String year = yearFormat.format(currentDate);
        String month = monthFormat.format(currentDate);

        // 현재 연도 폴더 생성
        Path yearPath = Paths.get(uploadDirectory, year);
        if (!Files.exists(yearPath)) {
            Files.createDirectories(yearPath);
        }

        // 현재 월 폴더 생성
        Path monthPath = Paths.get(yearPath.toString(), month);
        if (!Files.exists(monthPath)) {
            Files.createDirectories(monthPath);
        }

        // 파일명에 UUID를 사용하여 고유성 확보
        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

        // 파일 저장 경로 설정
        Path filePath = Paths.get(monthPath.toString(), uniqueFileName);

        // 파일 저장
        Files.copy(file.getInputStream(), filePath);

        // 파일 저장 경로 반환
        return filePath.toString();
    }


}
