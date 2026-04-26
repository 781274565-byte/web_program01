//定义一个“上传表单对象”，用来接收前端提交的两个字段: file & expireTime
package com.campusfasttransfer.dto;

import java.time.LocalDateTime; //导入 LocalDateTime 类，用来表示“日期+时间”,like 1111-11-11 11:11
import org.springframework.format.annotation.DateTimeFormat; //导入Spring的日期格式注解，告诉Spring前端传来的时间字符串应该按什么格式转换成Java时间对象
import org.springframework.web.multipart.MultipartFile; //导入Spring提供的MultipartFile，它专门用来接收上传的文件

public class UploadForm {

    private MultipartFile file; //保存用户上传的文件

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") //指定expireTime的接收格式
    private LocalDateTime expireTime; //过期时间

    public MultipartFile getFile() { //获取file的值
        return file;
    }

    public void setFile(MultipartFile file) { //给file赋值
        this.file = file;
    }

    public LocalDateTime getExpireTime() { //获取过期时间
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) { //设置过期时间
        this.expireTime = expireTime;
    }
}
