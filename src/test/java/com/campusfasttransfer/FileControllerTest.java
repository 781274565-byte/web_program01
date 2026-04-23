package com.campusfasttransfer;

import com.campusfasttransfer.entity.FileRecord;
import com.campusfasttransfer.entity.User;
import com.campusfasttransfer.service.AdminService;
import com.campusfasttransfer.service.AuthService;
import com.campusfasttransfer.service.FileService;
import com.campusfasttransfer.service.ShareService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private FileService fileService;

    @MockBean
    private ShareService shareService;

    @MockBean
    private AdminService adminService;

    @Test
    void dashboardRedirectsToLoginWhenSessionIsMissing() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void filesRedirectToLoginWhenSessionIsMissing() throws Exception {
        mockMvc.perform(get("/files"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void shareRedirectsToLoginWhenSessionIsMissing() throws Exception {
        mockMvc.perform(get("/share"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void adminRedirectsToLoginWhenSessionIsMissing() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(adminService);
    }

    @Test
    void adminRedirectsToDashboardWhenUserIsNotAdmin() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setRole("USER");

        mockMvc.perform(get("/admin").sessionAttr("currentUser", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verifyNoInteractions(adminService);
    }

    @Test
    void adminPageRendersUsersAndFilesForAdminSession() throws Exception {
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setRole("ADMIN");

        User listedUser = new User();
        listedUser.setId(2L);
        listedUser.setUsername("alice");
        listedUser.setRole("USER");

        FileRecord listedFile = new FileRecord();
        listedFile.setId(7L);
        listedFile.setOriginalName("report.pdf");
        listedFile.setOwnerId(2L);

        when(adminService.listUsers()).thenReturn(List.of(adminUser, listedUser));
        when(adminService.listFiles()).thenReturn(List.of(listedFile));

        mockMvc.perform(get("/admin").sessionAttr("currentUser", adminUser))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attribute("users", List.of(adminUser, listedUser)))
                .andExpect(model().attribute("files", List.of(listedFile)));
    }

    @Test
    void loginRedirectsToDashboardAndStoresCurrentUserInSession() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("secret");

        when(authService.authenticate("alice", "secret")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/login")
                        .param("username", "alice")
                        .param("password", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(request().sessionAttribute("currentUser", user));
    }

    @Test
    void registerRedirectsToLoginWithSuccessFlashMessage() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "alice")
                        .param("password", "secret")
                        .param("identityNo", "ID-123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("registrationSuccess", "Registration successful. Please sign in."));
    }

    @Test
    void filesPageRendersOwnedFilesAndUploadForm() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");

        FileRecord fileRecord = new FileRecord();
        fileRecord.setId(7L);
        fileRecord.setOriginalName("report.pdf");
        fileRecord.setStoredName("stored-report.pdf");
        fileRecord.setFilePath("uploads/stored-report.pdf");
        fileRecord.setFileSize(2048);
        fileRecord.setUploadedAt(LocalDateTime.of(2026, 4, 19, 10, 30));

        when(fileService.listOwnedFiles(1L)).thenReturn(List.of(fileRecord));

        mockMvc.perform(get("/files").sessionAttr("currentUser", user))
                .andExpect(status().isOk())
                .andExpect(view().name("my-files"))
                .andExpect(model().attributeExists("uploadForm"))
                .andExpect(model().attribute("files", List.of(fileRecord)));
    }

    @Test
    void enableShareRedirectsToSharePageWithGeneratedCode() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");

        FileRecord fileRecord = new FileRecord();
        fileRecord.setId(7L);
        fileRecord.setOriginalName("report.pdf");
        fileRecord.setOwnerId(1L);

        when(fileService.findOwnedFile(7L, 1L)).thenReturn(Optional.of(fileRecord));
        when(shareService.enableShare(fileRecord)).thenReturn("ABC12345");

        mockMvc.perform(post("/share/enable/7").sessionAttr("currentUser", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/share?code=ABC12345"));

        verify(shareService).enableShare(fileRecord);
    }

    @Test
    void enableShareRejectsExpiredOwnedFile() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");

        FileRecord fileRecord = new FileRecord();
        fileRecord.setId(8L);
        fileRecord.setOwnerId(1L);
        fileRecord.setExpireTime(LocalDateTime.now().minusMinutes(5));

        when(fileService.findOwnedFile(8L, 1L)).thenReturn(Optional.of(fileRecord));
        when(shareService.enableShare(fileRecord))
                .thenThrow(new IllegalStateException("Cannot enable sharing for an expired file."));

        mockMvc.perform(post("/share/enable/8").sessionAttr("currentUser", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/share?error=Cannot+enable+sharing+for+an+expired+file."));

        verify(shareService).enableShare(fileRecord);
    }

    @Test
    void shareLookupDisplaysValidSharedFile() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");

        FileRecord sharedFile = new FileRecord();
        sharedFile.setId(9L);
        sharedFile.setOriginalName("notes.pdf");
        sharedFile.setFileSize(4096L);
        sharedFile.setShareCode("ABC12345");
        sharedFile.setShared(true);

        when(shareService.validateShare("ABC12345")).thenReturn(Optional.of(sharedFile));

        mockMvc.perform(post("/share")
                        .param("shareCode", "ABC12345")
                        .sessionAttr("currentUser", user))
                .andExpect(status().isOk())
                .andExpect(view().name("share-access"))
                .andExpect(model().attribute("sharedFile", sharedFile))
                .andExpect(model().attribute("shareCode", "ABC12345"));
    }

    @Test
    void sharedDownloadReturnsAttachmentAndLogsCurrentUsername() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");

        FileRecord sharedFile = new FileRecord();
        sharedFile.setId(10L);
        sharedFile.setOriginalName("notes.pdf");
        sharedFile.setShareCode("ABC12345");
        sharedFile.setContentType("application/pdf");

        when(shareService.validateShare("ABC12345")).thenReturn(Optional.of(sharedFile));
        when(shareService.loadSharedFile(sharedFile))
                .thenReturn(new ByteArrayResource("shared file".getBytes(StandardCharsets.UTF_8)));

        mockMvc.perform(get("/share/download/ABC12345").sessionAttr("currentUser", user))
                .andExpect(status().isOk())
                .andExpect(content().bytes("shared file".getBytes(StandardCharsets.UTF_8)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        org.hamcrest.Matchers.containsString("attachment")
                ));

        verify(shareService).logDownload(10L, "alice");
    }

    @Test
    void sharedDownloadReturnsNotFoundWhenShareCodeIsInvalid() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");

        when(shareService.validateShare("MISSING")).thenReturn(Optional.empty());

        mockMvc.perform(get("/share/download/MISSING").sessionAttr("currentUser", user))
                .andExpect(status().isNotFound());

        verify(shareService, never()).logDownload(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void uploadRedirectsToFilesWhenFileIsSelected() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");

        FileRecord saved = new FileRecord();
        saved.setId(7L);
        when(fileService.saveUploadedFile(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(1L)))
                .thenReturn(saved);
        when(fileService.listOwnedFiles(1L)).thenReturn(List.of());

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "report.pdf",
                "application/pdf",
                "sample file".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/files/upload")
                        .file(multipartFile)
                        .param("expireTime", "2026-04-20T10:30")
                        .sessionAttr("currentUser", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/files"));

        verify(fileService).saveUploadedFile(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(1L));
    }

    @Test
    void downloadReturnsAttachmentForOwnedFile() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");

        FileRecord fileRecord = new FileRecord();
        fileRecord.setId(7L);
        fileRecord.setOriginalName("report.pdf");
        fileRecord.setStoredName("stored-report.pdf");
        fileRecord.setFilePath("uploads/stored-report.pdf");
        fileRecord.setContentType("application/pdf");
        fileRecord.setOwnerId(1L);

        when(fileService.findOwnedFile(7L, 1L)).thenReturn(Optional.of(fileRecord));
        when(fileService.loadOwnedFileAsResource(7L, 1L))
                .thenReturn(Optional.of(new ByteArrayResource("sample file".getBytes(StandardCharsets.UTF_8))));

        mockMvc.perform(get("/files/download/7").sessionAttr("currentUser", user))
                .andExpect(status().isOk())
                .andExpect(content().bytes("sample file".getBytes(StandardCharsets.UTF_8)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        org.hamcrest.Matchers.containsString("attachment")
                ));
    }

    @Test
    void downloadFallsBackToOctetStreamWhenContentTypeIsInvalid() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");

        FileRecord fileRecord = new FileRecord();
        fileRecord.setId(7L);
        fileRecord.setOriginalName("report.pdf");
        fileRecord.setStoredName("stored-report.pdf");
        fileRecord.setFilePath("uploads/stored-report.pdf");
        fileRecord.setContentType("bad type");
        fileRecord.setOwnerId(1L);

        when(fileService.findOwnedFile(7L, 1L)).thenReturn(Optional.of(fileRecord));
        when(fileService.loadOwnedFileAsResource(7L, 1L))
                .thenReturn(Optional.of(new ByteArrayResource("sample file".getBytes(StandardCharsets.UTF_8))));

        mockMvc.perform(get("/files/download/7").sessionAttr("currentUser", user))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string(
                        HttpHeaders.CONTENT_TYPE,
                        org.hamcrest.Matchers.startsWith("application/octet-stream")));
    }

    @Test
    void downloadReturnsNotFoundWhenOwnedFileIsMissing() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");

        when(fileService.findOwnedFile(99L, 1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/files/download/99").sessionAttr("currentUser", user))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSoftDeletesOwnedFileAndRedirectsToFiles() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");

        when(fileService.softDeleteOwnedFile(7L, 1L)).thenReturn(true);
        when(fileService.listOwnedFiles(1L)).thenReturn(List.of());

        mockMvc.perform(post("/files/delete/7").sessionAttr("currentUser", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/files"));

        verify(fileService).softDeleteOwnedFile(7L, 1L);
    }

    @Test
    void deleteStillRedirectsWhenOwnedFileIsMissing() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");

        when(fileService.softDeleteOwnedFile(99L, 1L)).thenReturn(false);

        mockMvc.perform(post("/files/delete/99").sessionAttr("currentUser", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/files"));

        verify(fileService).softDeleteOwnedFile(99L, 1L);
    }
}
