package iljin.framework.ebid.etc.notice.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.GeneralDao;
import iljin.framework.ebid.etc.util.common.consts.DB;
import iljin.framework.ebid.etc.util.common.file.FileService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NoticeService {
	@Autowired
	private TCoUserRepository tCoUserRepository;

	@Autowired
	private FileService fileService;

	@PersistenceContext
	private EntityManager entityManager;

	@Value("${file.upload.directory}")
	private String uploadDirectory;

	@Autowired
	GeneralDao generalDao;
	
	// 공지사항 목록 조회 및 상세 조회
	@SuppressWarnings("rawtypes")
	@Transactional
	public Page noticeList(Map<String, Object> params, CustomUserDetails user){
		ResultBody resultBody = new ResultBody();
		
		params.put("custCode", user.getCustCode());	// 계열사 사용자 - 소속 계열사/ 협력사 사용자 - 본인 협력사
		params.put("custType", user.getCustType());	// 사용자 업체 타입
		params.put("userAuth", user.getUserAuth()); // 계열사사용자 권한
		params.put("userId", user.getUsername()); // 계열사사용자 권한
		
		try {
			// 공지상세 조회시 조회수 +1
			String bno = CommonUtils.getString(params.get("bno"));
			if(!"".equals(bno)) {
				this.updateClickNum(params);
			}
			Page listPage = generalDao.selectGernalListPage(DB.QRY_SELECT_NOTICE_LIST, params);
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
	public void updateClickNum(Map<String, Object> params) throws Exception {
		generalDao.updateGernal(DB.QRY_UPDATE_NOTICE_BCNT, params);
	}

	// 공지사항 삭제
	@Transactional
	public void deleteNotice(Map<String, Object> params) throws Exception {
		// 공지 대상 계열사 정보 delete
		generalDao.deleteGernal(DB.QRY_DELETE_NOTICE_CUST, params);
		
		// 공지 삭제
		generalDao.deleteGernal(DB.QRY_DELETE_NOTICE, params);
	}
	
	// 공지사항 수정
	@SuppressWarnings("unchecked")
	@Transactional
	public void updateNotice(MultipartFile file, Map<String, Object> params) throws Exception {
		String bno = CommonUtils.getString(params.get("bno"));
		String uploadedPath = null;
		String fileName = null;
		String preUploadedPath = CommonUtils.getString(params.get("bfilePath"));		// 기등록 첨부파일 경로
		String preFileName = CommonUtils.getString(params.get("bfile"));				// 기등록 첨부파일
		
		if (file != null) {// 새로 첨부된 파일이 있는 경우
			uploadedPath = fileService.uploadFile(file);
			fileName = file.getOriginalFilename();

		} else if (!"".equals(preUploadedPath) && !"".equals(preFileName)) {// 기존에 첨부했던 파일이 그대로 있는 경우
			uploadedPath = preUploadedPath;
			fileName = preFileName;
		}
		
		params.put("fileName", fileName);
		params.put("uploadedPath", uploadedPath);
		
		// 공지 수정
		generalDao.updateGernal(DB.QRY_UPDATE_NOTICE, params);
		
		// 공지 대상 계열사 정보 delete
		generalDao.deleteGernal(DB.QRY_DELETE_NOTICE_CUST, params);

		ArrayList<String> custCodeList = (ArrayList<String>) params.get("interrelatedCustCodeArr");// 등록할 공지 계열사

		// 공지 대상 계열사 정보 INSERT
		for (int i = 0; i < custCodeList.size(); i++) {
			Map<String, Object> custParams = new HashMap<>();
			custParams.put("bno", bno);
			custParams.put("custCode", custCodeList.get(i));
			
			generalDao.insertGernal(DB.QRY_INSERT_NOTICE_CUST, custParams);
		}
	}

	// 공지사항 등록
	@SuppressWarnings("unchecked")
	@Transactional
	public void insertNotice(MultipartFile file, Map<String, Object> params) throws Exception {

		String bco = CommonUtils.getString(params.get("bco"));
		String uploadedPath = null;
		String fileName = null;
		
		if (file != null) {
			// 첨부파일 등록
			uploadedPath = fileService.uploadFile(file);
			fileName = file.getOriginalFilename();
		}
		params.put("fileName", fileName);
		params.put("uploadedPath", uploadedPath);
		
		generalDao.insertGernal(DB.QRY_INSERT_NOTICE, params);

		
		// 계열사 대상 공지인 경우
		if (bco.equals("CUST")) {
			ArrayList<String> custCodeList = (ArrayList<String>) params.get("interrelatedCustCodeArr");// 등록할 공지 계열사
			
			// 공지 계열사 정보 INSERT
			for (int i = 0; i < custCodeList.size(); i++) {
				Map<String, Object> custParams = new HashMap<>();
				custParams.put("bno", CommonUtils.getString(params.get("bno")));
				custParams.put("custCode", custCodeList.get(i));
				
				// 공지 대상 계열사 정보 insert
				generalDao.insertGernal(DB.QRY_INSERT_NOTICE_CUST, custParams);
			}
		}
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
