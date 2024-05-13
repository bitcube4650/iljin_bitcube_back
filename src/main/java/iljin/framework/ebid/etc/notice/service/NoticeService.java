package iljin.framework.ebid.etc.notice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.ebid.bid.service.BidProgressService;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.GeneralDao;
import iljin.framework.ebid.etc.util.common.file.FileService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NoticeService {
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

	@Autowired
	GeneralDao generalDao;

	/**
	 * 공지사항 목록 조회
	 * @param params
	 * @param user
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@Transactional
	public Page noticeList(Map<String, Object> params, CustomUserDetails user){
		ResultBody resultBody = new ResultBody();

		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());

		params.put("custCode", user.getCustCode());	// 계열사 사용자 - 소속 계열사/ 협력사 사용자 - 본인 협력사
		params.put("custType", user.getCustType());	// 사용자 업체 타입
		params.put("userAuth", user.getUserAuth()); // 계열사사용자 권한
		params.put("userId", user.getUsername()); // 계열사사용자 권한
		
		try {
			Page listPage = generalDao.selectGernalListPage("etc.selectNoticeList", params);
			resultBody.setData(listPage);
			
			return listPage;
		} catch (Exception e) {
			e.printStackTrace();
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("An error occurred while updating the click count.");
			resultBody.setData(e.getMessage());
			
			return null;
		}

//		return resultBody;
	}

	// 조회수 +1
	@Transactional
	public ResultBody updateClickNum(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();

		try {
			generalDao.updateGernal("etc.updateNoticeBCount", params);
		} catch (Exception e) {
			e.printStackTrace();
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("An error occurred while updating the click count.");
			resultBody.setData(e.getMessage());
		}
		return resultBody;

	}

	// 공지사항 삭제
	@Transactional
	public ResultBody deleteNotice(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			// 계열사 대상 공지 삭제
			generalDao.deleteGernal("etc.deleteNoticeCust", params);
			
			// 공지 삭제
			generalDao.deleteGernal("etc.deleteNotice", params);

		} catch (Exception e) {
			e.printStackTrace();
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("삭제 중 문제가 발생하였습니다.");
			resultBody.setData(e.getMessage());
		}

		return resultBody;
	}

	// 공지사항 수정
	@SuppressWarnings("unchecked")
	@Transactional
	public ResultBody updateNotice(MultipartFile file, Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		String bno = CommonUtils.getString(params.get("bno"));
		String uploadedPath = null;
		String fileName = null;
		String preUploadedPath = CommonUtils.getString(params.get("bfilePath"));		// 기등록 첨부파일 경로
		String preFileName = CommonUtils.getString(params.get("bfile"));				// 기등록 첨부파일
		
		try {
			if (file != null) {// 새로 첨부된 파일이 있는 경우
				uploadedPath = fileService.uploadFile(file);
				fileName = file.getOriginalFilename();

			} else if (!"".equals(preUploadedPath) && !"".equals(preFileName)) {// 기존에 첨부했던 파일이 그대로 있는 경우
				uploadedPath = preUploadedPath;
				fileName = preFileName;
			}
			
			params.put("fileName", fileName);
			params.put("uploadedPath", uploadedPath);
			
			generalDao.updateGernal("etc.updateNotice", params);
			
			// 계열사 대상 공지 삭제
			generalDao.deleteGernal("etc.deleteNoticeCust", params);

			ArrayList<String> custCodeList = (ArrayList<String>) params.get("interrelatedCustCodeArr");// 등록할 공지 계열사

			// 수정된 공지 계열사 정보 INSERT
			for (int i = 0; i < custCodeList.size(); i++) {
				Map<String, Object> custParams = new HashMap<>();
				custParams.put("bno", bno);
				custParams.put("custCode", custCodeList.get(i));
				
				generalDao.insertGernal("etc.insertNoticeCust", custParams);
			}

		} catch (Exception e) {
			e.printStackTrace();
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("공지사항 수정 시 오류가 발생하였습니다.");
			resultBody.setData(e.getMessage());
		}
		return resultBody;
	}

	// 공지사항 등록
	@Transactional
	public ResultBody insertNotice(MultipartFile file, Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();

		try {
//			TCoBoardNotice notice = new TCoBoardNotice();
//			LocalDateTime currentDate = LocalDateTime.now();
//
//			// 받아온 파라미터
//			String bco = (String) params.get("bco");
//			String btitle = (String) params.get("btitle");
//			String bcontent = (String) params.get("bcontent");
//			String buserid = (String) params.get("buserid");
//			String uploadedPath = null;
//			String fileName = null;
//
//			if (file != null) {
//				// 첨부파일 등록
//				uploadedPath = fileService.uploadFile(file);
//
//				// 원래 파일명
//				fileName = file.getOriginalFilename();
//			}
//			params.put("fileName", fileName);
//			params.put("uploadedPath", uploadedPath);
//			// 파라미터 set
//			notice.setBCo(bco);
//			notice.setBTitle(btitle);
//			notice.setBContent(bcontent);
//			notice.setBUserid(buserid);
//			notice.setBDate(currentDate);
//			notice.setBCount(0);
//			notice.setBFile(fileName);
//			notice.setBFilePath(uploadedPath);
//
//			entityManager.persist(notice);
//
//			Integer bno = notice.getBNo();

//			if (bco.equals("CUST")) {// 계열사 공지인 경우
//				ArrayList<String> custCodeList = (ArrayList<String>) params.get("interrelatedCustCodeArr");// 등록할 공지 계열사
//																											// 정보
//
//				// 수정된 공지 계열사 정보 INSERT
//				for (int i = 0; i < custCodeList.size(); i++) {
//					Map<String, Object> custParams = new HashMap<>();
//					custParams.put("bno", bno);
//					custParams.put("custCode", custCodeList.get(i));
//
//					generalDao.insertGernal("etc.insertNoticeCust", custParams);
//				}
//			}

		} catch (Exception e) {
			e.printStackTrace();
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("An error occurred while inserting notice.");
			resultBody.setData(e.getMessage());
		}
		return resultBody;
	}

	// 첨부파일 다운로드
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
