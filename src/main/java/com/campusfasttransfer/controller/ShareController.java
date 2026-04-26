package com.campusfasttransfer.controller;

import com.campusfasttransfer.entity.FileRecord; //导入文件记录实体类
import com.campusfasttransfer.entity.User; //导入用户实体类
import com.campusfasttransfer.service.FileService; //导入文件业务服务类
import com.campusfasttransfer.service.ShareService; //导入分享业务服务类
import jakarta.servlet.http.HttpSession; //导入会话对象，用于获取当前登录用户
import java.nio.charset.StandardCharsets; //导入标准字符集
import java.util.Optional; //导入Optional，表示“可能有也可能没有”的结果
import org.springframework.core.io.Resource; //导入资源对象，表示可下载的文件
import org.springframework.http.ContentDisposition; //导入内容处置对象，用于设置附件下载头
import org.springframework.http.HttpHeaders;  //导入HTTP响应头常量
import org.springframework.http.MediaType; //导入媒体类型，用于设置Content-Type
import org.springframework.http.ResponseEntity; //导入响应实体，用于返回完整HTTP响应
import org.springframework.stereotype.Controller; //导入控制器注解
import org.springframework.ui.Model; //导入Model
import org.springframework.util.StringUtils; //导入字符串工具类，用于判断字符串是否有内容
import org.springframework.web.bind.annotation.GetMapping; //导入GET请求映射注解
import org.springframework.web.bind.annotation.PathVariable; //导入路径参数注解
import org.springframework.web.bind.annotation.PostMapping; //导入POST请求映射注解
import org.springframework.web.bind.annotation.RequestParam; //导入请求参数注解
import org.springframework.web.servlet.mvc.support.RedirectAttributes; //导入重定向参数对象

@Controller
public class ShareController {

    private final FileService fileService; //声明文件服务对象
    private final ShareService shareService; //声明分享服务对象

    public ShareController(FileService fileService, ShareService shareService) {
        this.fileService = fileService;
        this.shareService = shareService;
    }

    @GetMapping("/share") //处理访问/share的GET请求
    public String sharePage(@RequestParam(name = "code", required = false) String code,
                            @RequestParam(name = "error", required = false) String error,
                            HttpSession session, //接收当前用户的会话对象
                            Model model) {
        User currentUser = currentUser(session);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("shareCode", code);
        if (StringUtils.hasText(code)) {
            model.addAttribute("generatedShareCode", code);
        }
        if (StringUtils.hasText(error)) {
            model.addAttribute("shareError", error);
        }
        return "share-access";
    }

    @PostMapping("/share/enable/{id}") // 处理POST /share/enable/文件id 请求
    public String enableShare(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = currentUser(session);
        Optional<FileRecord> fileRecord = fileService.findOwnedFile(id, currentUser.getId()); //查询该文件是否存在并且属于当前用户
        if (fileRecord.isEmpty()) {
            redirectAttributes.addAttribute("error", "Unable to enable share for that file."); //重定向时带上错误信息
            return "redirect:/share";
        }

        try { //开始尝试执行开启分享逻辑
            String shareCode = shareService.enableShare(fileRecord.get()); //为该文件开启分享，并返回分享码
            redirectAttributes.addAttribute("code", shareCode); //把分享码作为参数带回页面
        } catch (IllegalStateException ex) {
            redirectAttributes.addAttribute("error", ex.getMessage()); //把异常信息作为错误提示传回页面
        }
        return "redirect:/share";
    }

    @PostMapping("/share") //处理提交分享码查询的POST请求
    public String lookupShare(@RequestParam(name = "shareCode", required = false) String shareCode, //接收用户输入的分享码
                              HttpSession session,
                              Model model) {
        User currentUser = currentUser(session);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("shareCode", shareCode);

        if (!StringUtils.hasText(shareCode)) {
            model.addAttribute("shareError", "Please enter a share code."); //提示用户输入分享码
            return "share-access"; //直接返回当前页面
        }

        Optional<FileRecord> fileRecord = shareService.validateShare(shareCode); //验证分享码是否有效，并查询对应文件
        if (fileRecord.isEmpty()) { //如果分享码无效or找不到对应文件
            model.addAttribute("shareError", "Share code is invalid or has expired.");
            return "share-access";
        }

        model.addAttribute("sharedFile", fileRecord.get());  //把查到的共享文件放到模型中
        return "share-access";
    }

    @GetMapping("/share/download/{shareCode}") //处理通过分享码下载文件的GET请求
    public ResponseEntity<Resource> downloadSharedFile(@PathVariable String shareCode, HttpSession session) { //下载共享文件的方法
        User currentUser = currentUser(session);
        Optional<FileRecord> fileRecord = shareService.validateShare(shareCode); //校验分享码并获取文件记录
        if (fileRecord.isEmpty()) {
            return ResponseEntity.notFound().build();  //返回404响应
        }

        Resource resource; //声明文件资源变量
        try { //尝试读取共享文件
            resource = shareService.loadSharedFile(fileRecord.get()); //加载实际文件资源
        } catch (IllegalStateException ex) {
            return ResponseEntity.notFound().build(); //返回404
        }

        shareService.logDownload(fileRecord.get().getId(), currentUser.getUsername()); //记录本次的下载日志
        return buildDownloadResponse(fileRecord.get(), resource);  //构建并返回下载响应
    }

     //私有方法：统一构造下载响应
    private ResponseEntity<Resource> buildDownloadResponse(FileRecord fileRecord, Resource resource) {
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM; //默认内容类型设为二进制流
        if (StringUtils.hasText(fileRecord.getContentType())) { //如果文件记录中保存了内容类型
            try { //尝试把字符串解析为MediaTyp
                mediaType = MediaType.parseMediaType(fileRecord.getContentType()); //解析文件内容类型
            } catch (IllegalArgumentException ex) { //格式不合法
                mediaType = MediaType.APPLICATION_OCTET_STREAM; //回退到默认二进制流类型
            }
        }

        return ResponseEntity.ok() //返回一个HTTP200OK响应
                .contentType(mediaType) //设置响应内容类型
                .header(HttpHeaders.CONTENT_DISPOSITION, //设置Content-Disposition响应头
                        ContentDisposition.attachment() //指定这是个附件下载
                                .filename(fileRecord.getOriginalName(), StandardCharsets.UTF_8) //设置下载文件名
                                .build() //构建ContentDisposition对象
                                .toString()) //转为字符串作为响应头值
                .body(resource);  //把文件资源放到响应体中
    }

    private User currentUser(HttpSession session) { //从session中获取当前用户
        return (User) session.getAttribute("currentUser"); //强转
    }
}
