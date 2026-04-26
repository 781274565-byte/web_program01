//接收注册表单数据
package com.campusfasttransfer.dto;

import jakarta.validation.constraints.NotBlank;

public class RegisterForm {

    @NotBlank(message = "Username is required")
    private String username; //保存用户名

    @NotBlank(message = "Password is required")
    private String password; //保存密码

    @NotBlank(message = "Identity number is required")
    private String identityNo; //保存身份号

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
}
