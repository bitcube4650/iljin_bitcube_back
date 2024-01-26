package iljin.framework.core.security.role;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, RoleKey> {

//    @Query(value = "select r.* from a_user u inner join a_user_role ur on ur.user_id = u.id inner join a_role r on r.role_cd = ur.role where u.login_id = :loginId", nativeQuery = true)
    @Query(value = "select r.* from a_role r", nativeQuery = true)
    List<Role> findByLoginId(String loginId);

}
