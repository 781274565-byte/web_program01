//接收用户登录时提交的用户名和密码
package com.campusfasttransfer.dto;

import jakarta.validation.constraints.NotBlank; //导入NotBlank注解:这个注解用于校验字符串不能为空，而且不能只是空格

public class LoginForm {

    @NotBlank(message = "Username is required") //用户名不能为空，如果为空，就有报错信息
    private String username; //保存用户名

    @NotBlank(message = "Password is required")
    private String password; //保存密码

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
}
