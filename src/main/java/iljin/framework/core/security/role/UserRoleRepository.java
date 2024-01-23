package iljin.framework.core.security.role;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleKey> {

    Optional<UserRole> findByRole(RoleType role);
    List<UserRole> findAll();
    Optional<UserRole> findRoleById(UserRoleKey userRoleKey);
    List<UserRole> findRolesByUser_LoginId(String loginId);
    Optional<UserRole> findRoleByUser_LoginId(String loginId);
    List<UserRole> findRolesByUser_UserName(String userName);

    @Query(value="" +
            "INSERT INTO A_USER_ROLE (USER_ID, COMP_CD, ROLE, CREATED_BY, CREATION_DATE, MODIFIED_BY, MODIFIED_DATE)" +
            "       VALUES(:userId, :compCd, :role, 1, NOW(), 1, NOW())", nativeQuery=true)
    void insertNewUserRole(Long userId, String compCd, String role);

    @Query(value="DELETE FROM A_USER_ROLE WHERE ROLE = :role and COMP_CD = :compCd" ,nativeQuery = true)
    void deleteByRoleAndCompCd(String role,String compCd);
}
