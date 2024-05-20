package iljin.framework.ebid.etc.notice.service;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
public class FaqService {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	GeneralDao generalDao;

	// faq 목록 조회
	@SuppressWarnings("rawtypes")
	@Transactional
	public Page faqList(Map<String, Object> params) {

		ResultBody resultBody = new ResultBody();

		try {
			Page listPage = generalDao.selectGernalListPage(DB.QRY_SELECT_FAQ_LIST, params);
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
	

	//faq 저장
	@Transactional
	public void save(Map<String, Object> params, CustomUserDetails user) throws Exception {
		String updateInsert = CommonUtils.getString(params.get("updateInsert"));
		String userId = user.getUsername();
		
		params.put("userId" , userId);
		
		if(updateInsert.equals("update")) {
			// 수정
			generalDao.updateGernal(DB.QRY_INSERT_FAQ, params);
		}else {
			// 등록
			generalDao.insertGernal(DB.QRY_UPDATE_FAQ, params);
		}
	}

	//faq 삭제
	@Transactional
	public void delete(Map<String, Object> params) throws Exception {
		generalDao.deleteGernal(DB.QRY_DELETE_FAQ, params);
	}
}
