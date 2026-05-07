package com.campusfasttransfer.entity;

import jakarta.persistence.Column; //导入@Column注解，用来配置数据库表字段
import jakarta.persistence.Entity; //导入@Entity注解，表示这是一个JPA实体类
import jakarta.persistence.GeneratedValue; //导入@GeneratedValue注解，用来说明主键生成方式
import jakarta.persistence.GenerationType; //导入主键生成策略枚举，比如自增
import jakarta.persistence.Id; //导入@Id注解，用来标记主键字段
import jakarta.persistence.Table; //导入@Table注解，用来指定实体对应哪张表
import java.time.LocalDateTime; //导入Java时间类，表示日期和时间

@Entity
@Table(name = "users")
public class User {

    @Id //主键
    @GeneratedValue(strategy = GenerationType.IDENTITY)//主键值由数据库自动生成
    private Long id;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "identity_no", nullable = false, length = 32)
    private String identityNo;

    @Column(nullable = false, length = 32)
    private String role; //角色:user or admin 

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; //创建时间

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIdentityNo() {
        return identityNo;
    }

    public void setIdentityNo(String identityNo) {
        this.identityNo = identityNo;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
