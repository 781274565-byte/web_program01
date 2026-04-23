package com.campusfasttransfer;

import com.campusfasttransfer.entity.FileRecord;
import com.campusfasttransfer.repository.DownloadLogRepository;
import com.campusfasttransfer.repository.FileRecordRepository;
import com.campusfasttransfer.service.ShareService;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShareServiceTest {

    @Mock
    private FileRecordRepository fileRecordRepository;

    @Mock
    private DownloadLogRepository downloadLogRepository;

    @TempDir
    Path tempDir;

    @Test
    void validateShareRejectsExpiredSharedFile() {
        ShareService shareService = new ShareService(fileRecordRepository, downloadLogRepository, tempDir.toString());

        FileRecord record = new FileRecord();
        record.setId(1L);
        record.setShareCode("ABC123");
        record.setShared(true);
        record.setDeleted(false);
        record.setExpireTime(LocalDateTime.now().minusMinutes(1));

        when(fileRecordRepository.findByShareCodeAndDeletedFalse("ABC123")).thenReturn(Optional.of(record));

        assertTrue(shareService.validateShare("ABC123").isEmpty());
    }

    @Test
    void enableShareRejectsExpiredFile() {
        ShareService shareService = new ShareService(fileRecordRepository, downloadLogRepository, tempDir.toString());

        FileRecord record = new FileRecord();
        record.setId(2L);
        record.setExpireTime(LocalDateTime.now().minusMinutes(1));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> shareService.enableShare(record));

        assertEquals("Cannot enable sharing for an expired file.", exception.getMessage());
    }
}

@SpringBootTest
@Transactional
class ShareServicePersistenceTest {

    @Autowired
    private ShareService shareService;

    @Autowired
    private DownloadLogRepository downloadLogRepository;

    @Autowired
    private FileRecordRepository fileRecordRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void logDownloadPersistsSixtyFourCharacterUsername() {
        Long ownerId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?",
                Long.class,
                "admin"
        );

        FileRecord fileRecord = new FileRecord();
        fileRecord.setOriginalName("shared.txt");
        fileRecord.setStoredName("shared.txt");
        fileRecord.setFilePath("uploads/shared.txt");
        fileRecord.setFileSize(10L);
        fileRecord.setContentType("text/plain");
        fileRecord.setOwnerId(ownerId);
        fileRecord.setUploadedAt(LocalDateTime.now());
        fileRecord.setDeleted(false);
        fileRecord.setShared(true);
        fileRecord = fileRecordRepository.save(fileRecord);

        String longUsername = "u".repeat(64);
        assertEquals(64, longUsername.length());

        shareService.logDownload(fileRecord.getId(), longUsername);

        assertEquals(
                longUsername,
                downloadLogRepository.findByFileIdOrderByDownloadTimeDesc(fileRecord.getId()).getFirst().getDownloaderName()
        );
    }
}
