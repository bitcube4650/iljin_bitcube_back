package iljin.framework.ebid.custom.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.GeneralDao;
import iljin.framework.ebid.etc.util.common.consts.DB;
import iljin.framework.ebid.etc.util.common.file.FileService;
import iljin.framework.ebid.etc.util.common.mail.service.MailService;
import iljin.framework.ebid.etc.util.common.message.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustService {
	@Autowired
	private FileService fileService;
	@Autowired
	private MailService mailService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private MessageService messageService;
	@Autowired
	private GeneralDao generalDao;
	
	@SuppressWarnings({ "rawtypes", "unused" })
	public Page custList(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();

		Page listPage = generalDao.selectGernalListPage("cust.selectTCoCustList", params);
		
		return listPage;
	}

	@SuppressWarnings({ "unused", "rawtypes" })
	public Page otherCustList(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();

		Page listPage = generalDao.selectGernalListPage("cust.selectOtherCustList", params);

		return listPage;
	}
	
	@SuppressWarnings("unchecked")
	public ResultBody custDetail(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		
		Map<String, Object> custObj = (Map<String, Object>) (generalDao.selectGernalList("cust.selectTCoCustDetail", params)).get(0);
		resultBody.setData(custObj);
		
		return resultBody;
	}
	
	// 업체 승인
	@Transactional
	public void approval(Map<String, Object> params, CustomUserDetails user) throws Exception {
		params.put("userId", user.getUsername());
		params.put("certYn", "Y");
		// 업체 승인 처리
		generalDao.updateGernal("cust.updateTCoCustMasterCert", params);

		// 업체 이력 등록
		insertHistory(params);

		// 업체 승인 메일 저장 처리
		mailService.saveMailInfo("[일진그룹 e-bidding] 회원가입 승인",
						"[" + user.getCustName() + "] 계열사에서 [" + params.get("custName") + "] 업체 승인처리 되었습니다.\n"
						+ "<b>e-bidding 시스템</b>에 로그인하고 입찰업무를 처리해 주십시오\n"
						+ "입찰 업무는 로그인 후 하단에 입찰업무 안내를 참고하시거나 공지메뉴의 매뉴얼을 참조해 주십시오\n" + "\n" + "감사합니다.\n",
				(String) params.get("userEmail"));

		try {
			// 업체 승인 문자 발송
			messageService.send("일진그룹", CommonUtils.getString(params.get("userHp")).replaceAll("-", ""), (String) params.get("userName"), "[일진그룹 전자입찰시스템] 요청하신 일진그룹 전자입찰 시스템 회원가입이 승인되었습니다.");
		} catch (Exception e) {
			log.error("approval send message error : {}", e);
		}
	}

	@Transactional
	public void back(Map<String, Object> params, CustomUserDetails user) throws Exception {
		params.put("userId", user.getUsername());
		params.put("certYn", "D");
		// 업체 반려 처리
		generalDao.updateGernal("cust.updateTCoCustMasterCert", params);

		// 업체 이력 등록
		insertHistory(params);

		// 업체 삭제 처리
		generalDao.deleteGernal("cust.deleteTCoCustMaster", params);
		// 매핑 삭제 처리
		generalDao.deleteGernal("cust.deleteTCoCustIr", params);
		// 업체 사용자 삭제 처리
		generalDao.deleteGernal("cust.deleteTCoCustUser", params);
		
		// 반려 메일 저장 처리
		mailService.saveMailInfo("[일진그룹 e-bidding] 회원가입 반려",
				"[" + user.getCustName() + "] 계열사에서 [" + params.get("custName") + "] 업체 반려처리 되었습니다.\n"
				+ "아래 반려 사유를 확인해 주십시오\n" + "\n" + "감사합니다.\n" + "\n" + "- 반려사유\n" + params.get("etc"),
				(String) params.get("userEmail"));
	}
	
	@Transactional
	public void del(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		params.put("certYn", "D");
		generalDao.deleteGernal("cust.updateTCoCustMasterCert", params);

		// 협력사 이력 등록
		insertHistory(params);

		// 사용자 삭제 처리
		params.put("useYn", "N");
		generalDao.updateGernal("user.updateTCoCustUserUseYn", params);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public void save(Map<String, Object> params, MultipartFile regnumFile, MultipartFile bFile, CustomUserDetails user) throws Exception {
		String custCode = CommonUtils.getString(params.get("custCode"));
		
		// 첨부파일 업로드
		if (regnumFile != null) {
			params.put("regnumPath",	fileService.uploadFile(regnumFile));
			params.put("regnumFile",	regnumFile.getOriginalFilename());
		}
		if (bFile != null) {
			params.put("bFilePath",		fileService.uploadFile(bFile, "N"));
			params.put("bFile",			bFile.getOriginalFilename());
		}
		
		params.put("regnum",			CommonUtils.getString(params.get("regnum1")) + CommonUtils.getString(params.get("regnum2")) + CommonUtils.getString(params.get("regnum3")));		// 사업자등록번호
		params.put("presJuminNo",		CommonUtils.getString(params.get("presJuminNo1")) + CommonUtils.getString(params.get("presJuminNo2")));			// 법인번호
		params.put("tel",				CommonUtils.getString(params.get("tel")).replaceAll("-", ""));			// 업체 전화번호
		params.put("fax",				CommonUtils.getString(params.get("fax")).replaceAll("-", ""));			// 업체 팩스번호
		params.put("capital",			CommonUtils.getString(params.get("capital")).replaceAll(",", ""));		// 자본금
		params.put("userTel",			CommonUtils.getString(params.get("userTel")).replaceAll("-", ""));		// 사용자 연락처
		
		String userHp = CommonUtils.getString(params.get("userHp")).replaceAll("-", "");
		params.put("userHp",			userHp);															// 사용자 휴대폰번호
		params.put("updUserId",			user == null ? "" : user.getUsername());							// 등록자 및 수정자

		// 업체 등록
		if("".equals(custCode)) {
			params.put("userPwd",		passwordEncoder.encode((String) params.get("userPwd")));			// 비밀번호
			params.put("certYn",		user == null ? "N" : "Y");											// 승인요청 : 승인
			params.put("userType",		"1");																// 사용자 관리자 권한
			params.put("useYn",			"Y");																// 사용여부
			
			// 업체 등록
			generalDao.insertGernal("cust.insertTCoCustMaster", params);

			// 업체 이력 등록
			insertHistory(params);

			// 계열사_협력사_매핑 등록
			generalDao.insertGernal("cust.mergeTCoCustIR", params);

			// 업체 관리자 계정 생성
			generalDao.insertGernal("user.insertTCoCustUser", params);
			
			// 회원가입을 통한 업체등록인 경우
			if (user == null) {
				String title = "[일진그룹 e-bidding] 신규업체 승인 요청";
				String content = "[" + params.get("custName") + "] 신규업체 승인 요청이 왔습니다.\n"
						+ "e-bidding 시스템에 로그인하고 업체정보의 업체승인 페이지에서 \n" + "업체 정보를 확인하십시오\n" + "처리는 3일 이내 처리해야 합니다..\n" + "\n"
						+ "감사합니다.";
				
				// 회원가입시 승인요청 메일 수신자 리스트(계열사의 각사관리자 권한 사용자)
				List<Object> comAdmUserList = generalDao.selectGernalList("user.selectComAdminUserList", params);
				
				for(Object obj : comAdmUserList) {
					Map<String, Object> userMap = (Map<String, Object>) obj;
					// 회원가입 승인요청 메일 저장 처리
					mailService.saveMailInfo(title, content, CommonUtils.getString(userMap.get("userEmail")));
				}
				
			} else {
				// 회원가입 승인 메일 저장 처리
				mailService.saveMailInfo("[일진그룹 e-bidding] 회원가입 승인",
						"[" + user.getCustName() + "] 계열사에서 [" + params.get("custName") + "] 업체 승인처리 되었습니다.\n"
								+ "<b>e-bidding 시스템</b>에 로그인하고 입찰업무를 처리해 주십시오\n"
								+ "입찰 업무는 로그인 후 하단에 입찰업무 안내를 참고하시거나 공지메뉴의 매뉴얼을 참조해 주십시오\n" + "\n" + "감사합니다.\n",
						(String) params.get("userEmail"));

				try {
					// 문자 발송
					messageService.send("일진그룹", userHp, (String) params.get("userName"),
							"[일진그룹 전자입찰시스템] 요청하신 일진그룹 전자입찰 시스템 회원가입이 승인되었습니다.");
				} catch (Exception e) {
					log.error("insert send message error : {}", e);
				}
			}
		} else {
			// 업체 수정

			// 업체 정보 update
			generalDao.updateGernal("cust.updateTCoCustMaster", params);

			// 업체 이력 등록
			insertHistory(params);

			// 계열사 사용자 수정만 해당
			if ("inter".equals(user.getCustType())) {
				params.put("interrelatedCustCode", user.getCustCode());
				
				// 계열사_협력사_매핑 수정
				generalDao.insertGernal("cust.mergeTCoCustIR", params);
				
				// 사용자정보 수정
				generalDao.updateGernal("user.updateTCoCustUser", params);
			}
		}
	}

    public ResultBody idcheck(Map<String, Object> params) throws Exception {
        ResultBody resultBody = new ResultBody();
        
        int cnt = CommonUtils.getInt(generalDao.selectGernalCount(DB.QRY_SELECT_DUP_USER_CNT, params));
        if (cnt > 0) {
            resultBody.setCode("DUP"); // 아이디중복됨
        }
        return resultBody;
    }
    public ResultBody pwdcheck(Map<String, Object> params) {
        ResultBody resultBody = new ResultBody();
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!((BCryptPasswordEncoder) passwordEncoder).matches((String) params.get("userPwd"), user.getPassword())) {
            resultBody.setCode("notmatch");
        }
        return resultBody;
    }

	// 업체 이력 insert
	public void insertHistory(Map<String, Object> params) throws Exception {
		generalDao.insertGernal("cust.insertTCoCustHistory", params);
	}
}
