package iljin.framework.core.security.user;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Optional;

@Repository
public class UserRepositoryCustomImpl implements UserRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<UserDto> findByLoginId(String loginId) {
        StringBuilder sb = new StringBuilder();
        sb.append("" +
                "SELECT T.COMP_CD AS LOGIN_COMP_CD" +
                "       ,A.COMP_NM AS LOGIN_COMP_NM" +
                "       ,T.DEPT_CD AS LOGIN_DEPT_CD" +
                "       ,A.DEPT_NM AS LOGIN_DEPT_NM" +
                "       ,A.JOB_CD AS LOGIN_JOB_CD" +
                "       ,A.JOB_NM AS LOGIN_JOB_NM" +
                "       ,A.DUT_CD AS LOGIN_DUT_CD" +
                "       ,A.DUT_NM AS LOGIN_DUT_NM" +
                "       ,T.LOGIN_ID" +
                "       ,T.USER_NAME" +
                "       ,T.ENABLE_FLAG" +
                "       ,T.ATTRIBUTE_1" +
                "       ,T.ATTRIBUTE_2" +
                "       ,T.ATTRIBUTE_3" +
                "       ,T.ATTRIBUTE_4" +
                "       ,T.ATTRIBUTE_5" +
                "  FROM A_USER T" +
                "       LEFT OUTER JOIN TB_MST_EMP A ON A.COMP_CD = T.COMP_CD AND A.EMP_NO = T.LOGIN_ID" +
                " WHERE T.LOGIN_ID = :loginId");
        Query query = entityManager.createNativeQuery(sb.toString());
        query.setParameter("loginId", loginId);

        return Optional.ofNullable(new JpaResultMapper().list(query, UserDto.class).stream().findFirst().orElse(null));
    }
}
