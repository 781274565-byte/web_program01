package com.campusfasttransfer.controller;

import com.campusfasttransfer.dto.UploadForm; //导入上传表单对象，接收页面提交的文件和过期时间
import com.campusfasttransfer.entity.FileRecord; //导入文件记录实体，对应数据库里的文件信息
import com.campusfasttransfer.entity.User; //导入用户实体，表示当前登录用户
import com.campusfasttransfer.service.FileService; //导入业务层服务，控制器通过它处理文件相关逻辑
import jakarta.servlet.http.HttpSession; //导入HttpSession，用于从会话里拿当前用户
import java.nio.charset.StandardCharsets; //导入字符集常量，下载文件名会用UTF-8编码
import java.util.Optional; //导入Optional，表示“可能有值，也可能没有值”
import org.springframework.core.io.Resource; //导入Spring的资源对象，用来表示要下载的文件
import org.springframework.http.ContentDisposition; //导入响应头构造工具，用于设置附件下载文件名
import org.springframework.http.HttpHeaders; //导入 HTTP 头常量类
import org.springframework.http.MediaType; //导入媒体类型类，like image/png、application/pdf
import org.springframework.http.ResponseEntity; //导入响应实体类，可以自定义状态码、响应头、响应体
import org.springframework.stereotype.Controller; //导入@Controller注解，表示这是个Spring MVC控制器
import org.springframework.ui.Model; //导入Model，用于把数据传给前端页面模板
import org.springframework.validation.BindingResult; //导入表单绑定结果对象，用于检查表单是否有错误
import org.springframework.util.StringUtils; //导入字符串工具类，这里用来判断字符串是否有内容
import org.springframework.web.bind.annotation.GetMapping; //导入处理GET请求的注解
import org.springframework.web.bind.annotation.ModelAttribute; //导入表单对象绑定注解
import org.springframework.web.bind.annotation.PathVariable; //导入路径变量注解，like /download/{id}里的id
import org.springframework.web.bind.annotation.PostMapping; //导入处理POST请求的注解

@Controller
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    
    //显示“my-files”页面
    @GetMapping("/files") //表示方法处理GET/files请求
    //接收会话和页面模型，返回视图名
    public String myFiles(HttpSession session, Model model) {
        User currentUser = currentUser(session); //从会话里取出当前登录用户
        populateFilesPage(model, currentUser, new UploadForm(), null);//把页面需要的数据统一塞进model
        return "my-files";
    }

    //处理上传
    @PostMapping("/files/upload") //表示方法处理POST/files/upload请求
    public String upload(@ModelAttribute("uploadForm") UploadForm uploadForm, //把前端表单数据绑定到UploadForm对象，并命名为uploadForm
                         BindingResult bindingResult, //用于接收绑定或校验错误
                         HttpSession session, //取得当前用户
                         Model model/*把失败时要回显的数据再传回页面 */) {
        User currentUser = currentUser(session);
        //表单绑定出错 or 用户根本没选文件 or 文件为空
        if (bindingResult.hasErrors() || uploadForm.getFile() == null || uploadForm.getFile().isEmpty()) {
            populateFilesPage(model, currentUser, uploadForm, "Please select a file to upload.");
            return "my-files";
        }
        
        //调用业务层真正保存文件：传入上传文件、过期时间、当前用户ID
        fileService.saveUploadedFile(uploadForm.getFile(), uploadForm.getExpireTime(), currentUser.getId());
        return "redirect:/files"; //重定向,防止用户刷新页面时重复执行上传
    }

    //下载文件
    @GetMapping("/files/download/{id}") //处理GET /files/download/某个id 请求
    //从路径里取出文件ID，返回一个带文件内容的HTTP响应
    public ResponseEntity<Resource> download(@PathVariable Long id, HttpSession session) {
        User currentUser = currentUser(session);
        //先查数据库，确认这个文件存在，且确实属于当前用户
        Optional<FileRecord> fileRecord = fileService.findOwnedFile(id, currentUser.getId());
        if (fileRecord.isEmpty()) {
            return ResponseEntity.notFound().build(); //返回404
        }

        //把实际磁盘文件加载成Resource对象
        Optional<Resource> resource = fileService.loadOwnedFileAsResource(id, currentUser.getId());
        if (resource.isEmpty()) {
            return ResponseEntity.notFound().build(); //如果数据库里有记录，但磁盘文件没找到，也返回404
        }

        return buildDownloadResponse(fileRecord.get(), resource.get()); //数据库记录和文件都存在时，调用私有方法统一生成下载响应
    }

    //删除文件
    @PostMapping("/files/delete/{id}") //处理POST /files/delete/某个id 请求
    //拿到要删除的文件ID和会话
    public String delete(@PathVariable Long id, HttpSession session) {
        User currentUser = currentUser(session);
        fileService.softDeleteOwnedFile(id, currentUser.getId()); //把数据库里的deleted标记设为true
        return "redirect:/files";
    }

    //组装下载响应
    private ResponseEntity<Resource> buildDownloadResponse(FileRecord fileRecord, Resource resource) {
        String contentType = fileRecord.getContentType(); //取出数据库里保存的文件MIME类型
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM; //给一个默认值：二进制流
        if (StringUtils.hasText(contentType)) {
            try {
                mediaType = MediaType.parseMediaType(contentType); //把字符串类型转成Spring的MediaType对象
            } catch (IllegalArgumentException ex) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }
        return ResponseEntity.ok() //返回一个200成功响应，并开始链式设置内容
                .contentType(mediaType) //设置响应的Content-Type
                .header(HttpHeaders.CONTENT_DISPOSITION, //设置Content-Disposition响应头，告诉浏览器这是下载附件
                        ContentDisposition.attachment() //声明这是附件下载
                                .filename(fileRecord.getOriginalName(), StandardCharsets.UTF_8) //设置下载时显示的原始文件名
                                .build()
                                .toString()) //转成字符串，作为响应头值
                .body(resource); //把真正的文件资源放进响应体返回给浏览器
    }

    //准备页面数据
    private void populateFilesPage(Model model, User currentUser, UploadForm uploadForm, String uploadError) {
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("uploadForm", uploadForm);
        model.addAttribute("files", fileService.listOwnedFiles(currentUser.getId())); //查询当前用户的所有未删除文件，并传给页面展示
        if (uploadError != null) { //如果有上传错误信息，就附带传给页面
            model.addAttribute("uploadError", uploadError);
        }
    }

    //获取当前用户
    private User currentUser(HttpSession session) { //从session里拿当前用户
        return (User) session.getAttribute("currentUser");
    }
}
