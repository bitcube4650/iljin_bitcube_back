package iljin.framework.ebid.custom.service;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.GeneralDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    @PersistenceContext
    private EntityManager entityManager;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private TCoUserRepository tCoUserRepository;

	@Autowired
	private GeneralDao generalDao;

	public ResultBody interrelatedList() throws Exception {
		ResultBody resultBody = new ResultBody();

		List<Object> list = generalDao.selectGernalList("user.selectInterrelatedList", null);
		resultBody.setData(list);

		return resultBody;
	}

	public ResultBody userList(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();

		Page listPage = generalDao.selectGernalListPage("user.selectUserList", params);
		resultBody.setData(listPage);

		return resultBody;
	}

	/**
	 * @param params : userId
	 * @return
	 * @throws Exception
	 */
	public ResultBody userDetail(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		Map<String, Object> userDetail = (Map<String, Object>) generalDao.selectGernalObject("user.selectUserDetail", params);

		// 감사사용자의 경우 감사 계열사 조회
		String userAuthCode = (String) userDetail.get("userAuth");
		if("4".equals(userAuthCode)){
			List<Object> userInterrelated = generalDao.selectGernalList("selectInterrelatedListByUser", userDetail);
			userDetail.put("user_interrelated", userInterrelated);
		}

		resultBody.setData(userDetail);

		return resultBody;
	}

	@Transactional
	public ResultBody userSave(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		// 등록/수정자
		String createUser = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
		params.put("createUser", createUser);
		params.put("updateUser", createUser);
		// 저장
		if ((boolean) params.get("isCreate")) {
			// 비밀번호 암호화
			String userPwd = CommonUtils.getString(params.get("userPwd"), "");
			String encodedPassword = passwordEncoder.encode(userPwd);
			params.put("encodedPassword", encodedPassword);

			generalDao.insertGernal("insertUserSave", params);
		}
		// 수정
		else {
			generalDao.updateGernal("updateUserSave", params);
		}
		// 계열사 등록
		// 고유 키가 없기에 매번 지워야 한다.
		generalDao.deleteGernal("deleteUserInterrelated", params);
		// 감사사용자의 경우 감사계열사 정보를 저장 처리
		if ("4".equals(params.get("userAuth"))) {
			List<Map> list = (List) params.get("userInterrelatedList");
			for (Map<String, Object> dataMap : list) {
				//선택한 계열사만 인서트
				if (dataMap.get("check") != null && (boolean)dataMap.get("check") == true) {
					dataMap.put("interrelatedCustCode", dataMap.get("interrelatedCustCode"));
					dataMap.put("userId", params.get("userId"));
					generalDao.insertGernal("insertUserInterrelated", dataMap);
				}
			}
		}
		return resultBody;
	}
    // 비밀번호 체크
	public ResultBody pwdCheck(Map<String, Object> params) {
        ResultBody resultBody = new ResultBody();
		// 파라미터 정리
		String userId = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
		String pwd = CommonUtils.getString(params.get("pwd"), "");
		
		// db 비밀번호
		String dbPassword = "";
		Optional<TCoUser> userOptional = tCoUserRepository.findById(userId);
		if (userOptional.isPresent()) {
			dbPassword = userOptional.get().getUserPwd();
		}
		
		// 비밀번호 체크
		//boolean pwdCheck = userServiceImpl.checkPassword(userId, pwd);
		boolean pwdCheck = ((BCryptPasswordEncoder) passwordEncoder).matches(pwd, dbPassword);
		//
		if(!pwdCheck ) {
            resultBody.setCode("NO"); // 비밀번호 실패
		}
		
		return resultBody;
	}
    
    // 비밀번호 변경
    @Transactional
	public ResultBody saveChgPwd(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();

		// 비밀번호 암호화
		String chgPassword = CommonUtils.getString(params.get("chgPassword"), "");
		String encodedPassword = passwordEncoder.encode(chgPassword);
		params.put("userPwd", encodedPassword);

		String createUser = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
		params.put("userPwd", encodedPassword);

		generalDao.updateGernal("user.updateUserChgPwd",params);

		return resultBody;
	}
}
