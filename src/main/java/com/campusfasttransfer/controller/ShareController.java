package com.campusfasttransfer.controller;

import com.campusfasttransfer.entity.FileRecord;
import com.campusfasttransfer.entity.User;
import com.campusfasttransfer.service.FileService;
import com.campusfasttransfer.service.ShareService;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ShareController {

    private final FileService fileService;
    private final ShareService shareService;

    public ShareController(FileService fileService, ShareService shareService) {
        this.fileService = fileService;
        this.shareService = shareService;
    }

    @GetMapping("/share")
    public String sharePage(@RequestParam(name = "code", required = false) String code,
                            @RequestParam(name = "error", required = false) String error,
                            HttpSession session,
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

    @PostMapping("/share/enable/{id}")
    public String enableShare(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = currentUser(session);
        Optional<FileRecord> fileRecord = fileService.findOwnedFile(id, currentUser.getId());
        if (fileRecord.isEmpty()) {
            redirectAttributes.addAttribute("error", "Unable to enable share for that file.");
            return "redirect:/share";
        }

        try {
            String shareCode = shareService.enableShare(fileRecord.get());
            redirectAttributes.addAttribute("code", shareCode);
        } catch (IllegalStateException ex) {
            redirectAttributes.addAttribute("error", ex.getMessage());
        }
        return "redirect:/share";
    }

    @PostMapping("/share")
    public String lookupShare(@RequestParam(name = "shareCode", required = false) String shareCode,
                              HttpSession session,
                              Model model) {
        User currentUser = currentUser(session);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("shareCode", shareCode);

        if (!StringUtils.hasText(shareCode)) {
            model.addAttribute("shareError", "Please enter a share code.");
            return "share-access";
        }

        Optional<FileRecord> fileRecord = shareService.validateShare(shareCode);
        if (fileRecord.isEmpty()) {
            model.addAttribute("shareError", "Share code is invalid or has expired.");
            return "share-access";
        }

        model.addAttribute("sharedFile", fileRecord.get());
        return "share-access";
    }

    @GetMapping("/share/download/{shareCode}")
    public ResponseEntity<Resource> downloadSharedFile(@PathVariable String shareCode, HttpSession session) {
        User currentUser = currentUser(session);
        Optional<FileRecord> fileRecord = shareService.validateShare(shareCode);
        if (fileRecord.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource;
        try {
            resource = shareService.loadSharedFile(fileRecord.get());
        } catch (IllegalStateException ex) {
            return ResponseEntity.notFound().build();
        }

        shareService.logDownload(fileRecord.get().getId(), currentUser.getUsername());
        return buildDownloadResponse(fileRecord.get(), resource);
    }

    private ResponseEntity<Resource> buildDownloadResponse(FileRecord fileRecord, Resource resource) {
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (StringUtils.hasText(fileRecord.getContentType())) {
            try {
                mediaType = MediaType.parseMediaType(fileRecord.getContentType());
            } catch (IllegalArgumentException ex) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(fileRecord.getOriginalName(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(resource);
    }

    private User currentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }
}
