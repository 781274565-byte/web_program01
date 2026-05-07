package com.campusfasttransfer.repository;

import com.campusfasttransfer.entity.User; //导入User实体类
import java.util.Optional; //导入Optional类
import org.springframework.data.jpa.repository.JpaRepository; //导入Spring Data JPA提供的JpaRepository接口

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username); //定义一个按用户名查询用户的方法

    boolean existsByUsername(String username); //定义一个检查用户名是否已存在的方法

    boolean existsByIdentityNo(String identityNo); //定义一个检查身份证是否已存在的方法
}
