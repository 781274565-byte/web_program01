package com.campusfasttransfer.service;

import com.campusfasttransfer.entity.FileRecord;
import com.campusfasttransfer.repository.FileRecordRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class FileService {

    private final FileRecordRepository fileRecordRepository;
    private final Path uploadDirectory;

    public FileService(FileRecordRepository fileRecordRepository,
                       @Value("${app.upload-dir}") String uploadDir) {
        this.fileRecordRepository = fileRecordRepository;
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDirectory);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to initialize upload directory", ex);
        }
    }

    public List<FileRecord> listOwnedFiles(Long ownerId) {
        return fileRecordRepository.findByOwnerIdAndDeletedFalseOrderByUploadedAtDesc(ownerId);
    }

    public Optional<FileRecord> findOwnedFile(Long id, Long ownerId) {
        return fileRecordRepository.findByIdAndOwnerIdAndDeletedFalse(id, ownerId);
    }

    public FileRecord saveUploadedFile(MultipartFile file, LocalDateTime expireTime, Long ownerId) {
        String originalName = normalizeOriginalName(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + "_" + sanitizeFileName(originalName);
        Path storedPath = uploadDirectory.resolve(storedName).normalize();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, storedPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store uploaded file", ex);
        }

        FileRecord record = new FileRecord();
        record.setOriginalName(originalName);
        record.setStoredName(storedName);
        record.setFilePath(storedPath.toString());
        record.setFileSize(file.getSize());
        record.setContentType(file.getContentType());
        record.setExpireTime(expireTime);
        record.setShared(false);
        record.setOwnerId(ownerId);
        record.setUploadedAt(LocalDateTime.now());
        record.setDeleted(false);

        try {
            return fileRecordRepository.save(record);
        } catch (RuntimeException ex) {
            try {
                Files.deleteIfExists(storedPath);
            } catch (IOException cleanupEx) {
                ex.addSuppressed(cleanupEx);
            }
            throw ex;
        }
    }

    public Optional<Resource> loadOwnedFileAsResource(Long id, Long ownerId) {
        return findOwnedFile(id, ownerId)
                .flatMap(fileRecord -> resolveManagedPath(fileRecord)
                        .filter(Files::exists)
                        .map(FileSystemResource::new));
    }

    public boolean softDeleteOwnedFile(Long id, Long ownerId) {
        return findOwnedFile(id, ownerId)
                .map(fileRecord -> {
                    fileRecord.setDeleted(true);
                    fileRecordRepository.save(fileRecord);
                    return true;
                })
                .orElse(false);
    }

    private String normalizeOriginalName(String originalName) {
        if (!StringUtils.hasText(originalName)) {
            return "uploaded-file";
        }
        String cleaned = originalName.replace("\\", "/");
        return cleaned.substring(cleaned.lastIndexOf('/') + 1);
    }

    private String sanitizeFileName(String originalName) {
        return originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
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
