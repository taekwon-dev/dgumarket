package com.springboot.dgumarket.repository.member;

import com.springboot.dgumarket.model.LoggedLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

/**
 * Created by TK YOUN (2020-11-01 오후 1:44)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
public interface LoggedLoginRepository extends JpaRepository<LoggedLogin, Integer> {


    Boolean existsByRefreshTokenAndStatus(String refreshToken, int status);

    @Transactional
    @Query(value = "SELECT member_id FROM logged_logins WHERE refresh_token =:refresh_token", nativeQuery = true)
    int findMemberIdbyRefreshToken(@Param("refresh_token") String refresh_token);

    @Transactional
    @Modifying
    @Query(value = "UPDATE logged_logins SET status = 1 WHERE member_id =:member_id AND refresh_token =:refresh_token", nativeQuery = true)
    int addBlacklist(@Param("member_id") int member_id, @Param("refresh_token") String refresh_token);

    @Transactional
    @Modifying
    @Query(value = "UPDATE logged_logins SET status = 1 WHERE access_token =:access_token", nativeQuery = true)
    int addLogoutBlacklist(@Param("access_token") String access_token);


}
