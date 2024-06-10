package iljin.framework.ebid.custom.service;

import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.GeneralDao;
import iljin.framework.ebid.etc.util.common.consts.DB;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustUserService {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private GeneralDao generalDao;

	@SuppressWarnings({ "unused", "rawtypes" })
	public ResultBody userList(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();

		Page listPage = generalDao.selectGernalListPage(DB.QRY_SELECT_CUST_USER_LIST, params);
		resultBody.setData(listPage);

		return resultBody;
	}

	@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
	public Map<String, Object> detail(Map<String, Object> params) throws Exception {
		Map<String, Object>  userMap = new HashedMap();
		
		userMap = (Map<String, Object>) generalDao.selectGernalObject(DB.QRY_SELECT_CUST_USER_DETAIL, params);

		return userMap;
	}

	@Transactional
	public ResultBody save(Map<String, Object> params, CustomUserDetails user) throws Exception {
		ResultBody resultBody = new ResultBody();
		
		String userPwd = CommonUtils.getString(params.get("userPwd"));
		
		if(!"".equals(userPwd)) {
			params.put("userPwd",	passwordEncoder.encode(userPwd));								// 비밀번호
		}
		params.put("updUserId",		user.getUsername());											// 수정자 및 등록자 userId
		params.put("userHp",		CommonUtils.getString(params.get("userHp")).replace("-", ""));	// 휴대폰번호
		params.put("userTel",		CommonUtils.getString(params.get("userTel")).replace("-", ""));	// 전화번
		
		if ((boolean) params.get("isCreate")) {
			params.put("userType",	"2");							// 업체사용자 권한('2': 일반사용자)
			params.put("useYn",		"Y");							// 사용여부 default '사용'
			params.put("custCode",	user.getCustCode());			// 업체코드

			// 사용자 등록
			generalDao.insertGernal(DB.QRY_INSERT_CUST_USER, params);
		} else {
			// 사용자 수정
			generalDao.updateGernal(DB.QRY_UPDATE_CUST_USER, params);
		}

		return resultBody;
	}

	@Transactional
	public ResultBody del(Map<String, Object> params, CustomUserDetails user) throws Exception {
		ResultBody resultBody = new ResultBody();

		params.put("useYn", "N");
		params.put("delUserId",	params.get("userId"));
		params.put("updUserId",	user.getUsername());
		params.put("custCode",	user.getCustCode());

		generalDao.updateGernal(DB.QRY_UPDATE_CUST_USER_USEYN, params);
		return resultBody;
	}
}
