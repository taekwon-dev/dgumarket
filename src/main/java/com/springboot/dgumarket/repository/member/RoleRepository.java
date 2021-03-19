package com.springboot.dgumarket.repository.member;

import com.springboot.dgumarket.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by TK YOUN (2020-10-20 오전 8:23)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Role findByName(String rolename);

    Role findById(int id);
}
