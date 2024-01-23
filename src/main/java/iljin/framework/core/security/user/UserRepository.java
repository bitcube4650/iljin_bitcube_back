package iljin.framework.core.security.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);
    List<User> findAllByLoginIdContains(String loginId);
    Optional<User> findByLoginId(String loginId);

    @Modifying
    @Transactional
    void deleteByLoginId(String loginId);
    List<User> findAll();

    @Modifying
    @Transactional
    @Query(value = "update A_USER a set a.attribute_1 = :token where a.login_id = :loginId", nativeQuery = true)
    int updatePushToken(String token, String loginId);
}
