package com.campusfasttransfer.controller;

import com.campusfasttransfer.dto.UploadForm;
import com.campusfasttransfer.entity.FileRecord;
import com.campusfasttransfer.entity.User;
import com.campusfasttransfer.service.FileService;
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
import org.springframework.validation.BindingResult;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/files")
    public String myFiles(HttpSession session, Model model) {
        User currentUser = currentUser(session);
        populateFilesPage(model, currentUser, new UploadForm(), null);
        return "my-files";
    }

    @PostMapping("/files/upload")
    public String upload(@ModelAttribute("uploadForm") UploadForm uploadForm,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model) {
        User currentUser = currentUser(session);
        if (bindingResult.hasErrors() || uploadForm.getFile() == null || uploadForm.getFile().isEmpty()) {
            populateFilesPage(model, currentUser, uploadForm, "Please select a file to upload.");
            return "my-files";
        }

        fileService.saveUploadedFile(uploadForm.getFile(), uploadForm.getExpireTime(), currentUser.getId());
        return "redirect:/files";
    }

    @GetMapping("/files/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id, HttpSession session) {
        User currentUser = currentUser(session);
        Optional<FileRecord> fileRecord = fileService.findOwnedFile(id, currentUser.getId());
        if (fileRecord.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Resource> resource = fileService.loadOwnedFileAsResource(id, currentUser.getId());
        if (resource.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return buildDownloadResponse(fileRecord.get(), resource.get());
    }

    @PostMapping("/files/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        User currentUser = currentUser(session);
        fileService.softDeleteOwnedFile(id, currentUser.getId());
        return "redirect:/files";
    }

    private ResponseEntity<Resource> buildDownloadResponse(FileRecord fileRecord, Resource resource) {
        String contentType = fileRecord.getContentType();
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (StringUtils.hasText(contentType)) {
            try {
                mediaType = MediaType.parseMediaType(contentType);
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

    private void populateFilesPage(Model model, User currentUser, UploadForm uploadForm, String uploadError) {
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("uploadForm", uploadForm);
        model.addAttribute("files", fileService.listOwnedFiles(currentUser.getId()));
        if (uploadError != null) {
            model.addAttribute("uploadError", uploadError);
        }
    }

    private User currentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }
}
