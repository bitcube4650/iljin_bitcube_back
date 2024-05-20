package iljin.framework.ebid.custom.service;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import iljin.framework.ebid.etc.util.GeneralDao;
import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.util.Pair;
import iljin.framework.ebid.custom.dto.TCoUserDto;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.PagaUtils;
import lombok.extern.slf4j.Slf4j;

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
    public ResultBody save(Map<String, Object> params) {
        ResultBody resultBody = new ResultBody();
        StringBuilder sbQuery = null;
     // 저장
        if ((boolean) params.get("isCreate")) {
            sbQuery = new StringBuilder(
            " insert into t_co_user (user_id, user_pwd, user_name, interrelated_cust_code, user_auth, openauth, bidauth, user_hp, user_tel, user_email, user_position, dept_name, use_yn, create_user, create_date, update_user, update_date, pwd_edit_date, pwd_edit_yn) " +
            " values (:userId, :userPwd, :userName, :interrelatedCustCode, :userAuth, :openauth, :bidauth, :userHp, :userTel, :userEmail, :userPosition, :deptName, :useYn, :updateUser, now(), :updateUser, now(), now(), 'N')");
        } 
        // 수정
        else {
            sbQuery = new StringBuilder(
            " update t_co_user "
            + "set user_name = :userName"
            + ", interrelated_cust_code = :interrelatedCustCode"
            + ", user_auth = :userAuth"
            + ", openauth = :openauth"
            + ", bidauth = :bidauth"
            + ", user_hp = :userHp"
            + ", user_tel = :userTel"
            + ", user_email = :userEmail"
            + ", user_position = :userPosition"
            + ", dept_name = :deptName"
            + ", use_yn = :useYn"
            + ", update_user = :updateUser"
            + ", update_date = now() "
            + "where user_id = :userId");
        }
        Query query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("userId", params.get("userId"));
        if ((boolean) params.get("isCreate")) {
        	// 비밀번호 암호화
        	String userPwd = CommonUtils.getString(params.get("userPwd"), "");
        	String encodedPassword = passwordEncoder.encode(userPwd);
            query.setParameter("userPwd", encodedPassword);
        }
        query.setParameter("userName", params.get("userName"));
        query.setParameter("interrelatedCustCode", params.get("interrelatedCustCode"));
        query.setParameter("userAuth", params.get("userAuth"));
        query.setParameter("openauth", params.get("openauth"));
        query.setParameter("bidauth", params.get("bidauth"));
        query.setParameter("userHp", CommonUtils.getString(params.get("userHp")).replace("-",""));
        query.setParameter("userTel", CommonUtils.getString(params.get("userTel")).replace("-",""));
        query.setParameter("userEmail", params.get("userEmail"));
        query.setParameter("userPosition", params.get("userPosition"));
        query.setParameter("deptName", params.get("deptName"));
        query.setParameter("useYn", params.get("useYn"));
        String createUser = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        query.setParameter("updateUser", createUser);
        query.executeUpdate();

        // 고유 키가 없기에 매번 지워야 한다.
        sbQuery = new StringBuilder(" delete from t_co_user_interrelated where user_id = :userId");
        query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("userId", params.get("userId"));
        query.executeUpdate();
        // 감사사용자의 경우 감사계열사 정보를 저장 처리
        if ("4".equals(params.get("userAuth"))) {
            List<Map> list = (List) params.get("userInterrelatedList");
            for (Map<String, Object> data : list) {
                if (data.get("check") != null && (boolean)data.get("check") == true) {
                    sbQuery = new StringBuilder(" insert into t_co_user_interrelated (interrelated_cust_code, user_id) values (:interrelatedCustCode, :userId)");
                    query = entityManager.createNativeQuery(sbQuery.toString());
                    query.setParameter("interrelatedCustCode", data.get("key"));
                    query.setParameter("userId", params.get("userId"));
                    query.executeUpdate();
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
	public ResultBody saveChgPwd(Map<String, Object> params) {
        ResultBody resultBody = new ResultBody();
        StringBuilder sbQuery = new StringBuilder(
	            " update t_co_user "
	            + "set user_pwd = :userPwd"
	            + ", pwd_edit_yn = 'Y'"
	            + ", pwd_edit_date = now()"
	            + ", update_user = :updateUser"
	            + ", update_date = now() "
	            + "where user_id = :userId");
        Query query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("userId", params.get("userId"));
        // 비밀번호 암호화
        String chgPassword = CommonUtils.getString(params.get("chgPassword"), "");
        String encodedPassword = passwordEncoder.encode(chgPassword);
        query.setParameter("userPwd", encodedPassword);
        String createUser = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        query.setParameter("updateUser", createUser);
        query.executeUpdate();
        
		return resultBody;
	}
}
