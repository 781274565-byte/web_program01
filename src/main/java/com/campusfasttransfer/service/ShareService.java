package com.campusfasttransfer.service;

import com.campusfasttransfer.entity.DownloadLog;
import com.campusfasttransfer.entity.FileRecord;
import com.campusfasttransfer.repository.DownloadLogRepository;
import com.campusfasttransfer.repository.FileRecordRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class ShareService {

    private final FileRecordRepository fileRecordRepository;
    private final DownloadLogRepository downloadLogRepository;
    private final Path uploadDirectory;

    public ShareService(FileRecordRepository fileRecordRepository,
                        DownloadLogRepository downloadLogRepository,
                        @Value("${app.upload-dir}") String uploadDir) {
        this.fileRecordRepository = fileRecordRepository;
        this.downloadLogRepository = downloadLogRepository;
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDirectory);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to initialize upload directory", ex);
        }
    }

    public String enableShare(FileRecord record) {
        if (!isNotExpired(record)) {
            throw new IllegalStateException("Cannot enable sharing for an expired file.");
        }

        String shareCode = generateUniqueShareCode();
        record.setShareCode(shareCode);
        record.setShared(true);
        fileRecordRepository.save(record);
        return shareCode;
    }

    public Optional<FileRecord> validateShare(String shareCode) {
        if (!StringUtils.hasText(shareCode)) {
            return Optional.empty();
        }

        return fileRecordRepository.findByShareCodeAndDeletedFalse(shareCode)
                .filter(FileRecord::isShared)
                .filter(this::isNotExpired);
    }

    public void logDownload(Long fileId, String username) {
        DownloadLog downloadLog = new DownloadLog();
        downloadLog.setFileId(fileId);
        downloadLog.setDownloaderName(StringUtils.hasText(username) ? username : "unknown");
        downloadLog.setDownloadTime(LocalDateTime.now());
        downloadLogRepository.save(downloadLog);
    }

    public Resource loadSharedFile(FileRecord record) {
        Path filePath = resolveManagedPath(record)
                .orElseThrow(() -> new IllegalStateException("Shared file is not available"));
        if (!Files.exists(filePath)) {
            throw new IllegalStateException("Shared file is not available");
        }
        return new FileSystemResource(filePath);
    }

    private String generateUniqueShareCode() {
        String shareCode;
        do {
            shareCode = UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 8)
                    .toUpperCase(Locale.ROOT);
        } while (fileRecordRepository.findByShareCodeAndDeletedFalse(shareCode).isPresent());
        return shareCode;
    }

    private boolean isNotExpired(FileRecord record) {
        LocalDateTime expireTime = record.getExpireTime();
        return expireTime == null || expireTime.isAfter(LocalDateTime.now());
    }

    private Optional<Path> resolveManagedPath(FileRecord fileRecord) {
        Path candidate;
        if (StringUtils.hasText(fileRecord.getStoredName())) {
            candidate = uploadDirectory.resolve(fileRecord.getStoredName()).normalize();
        } else if (StringUtils.hasText(fileRecord.getFilePath())) {
            candidate = Paths.get(fileRecord.getFilePath()).normalize();
            if (!candidate.isAbsolute()) {
                candidate = uploadDirectory.resolve(candidate).normalize();
            }
        } else {
            return Optional.empty();
        }

        if (!candidate.startsWith(uploadDirectory)) {
            return Optional.empty();
        }
        return Optional.of(candidate);
    }
}
