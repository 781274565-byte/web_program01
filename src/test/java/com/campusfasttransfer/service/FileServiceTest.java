package com.campusfasttransfer.service;

import com.campusfasttransfer.entity.FileRecord;
import com.campusfasttransfer.repository.FileRecordRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileRecordRepository fileRecordRepository;

    @TempDir
    Path tempDir;

    @Test
    void saveUploadedFileRemovesCopiedFileWhenPersistenceFails() throws IOException {
        FileService fileService = new FileService(fileRecordRepository, tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "report.pdf",
                "application/pdf",
                "sample file".getBytes()
        );

        doThrow(new RuntimeException("db failure")).when(fileRecordRepository).save(any(FileRecord.class));

        assertThrows(RuntimeException.class, () -> fileService.saveUploadedFile(file, LocalDateTime.now(), 1L));

        try (var files = Files.list(tempDir)) {
            assertTrue(files.findAny().isEmpty());
        }
    }

    @Test
    void loadOwnedFileAsResourceRejectsPathsOutsideUploadDirectory() throws IOException {
        FileService fileService = new FileService(fileRecordRepository, tempDir.toString());

        Path outsideFile = Files.createTempFile("outside", ".txt");
        FileRecord record = new FileRecord();
        record.setId(1L);
        record.setOwnerId(1L);
        record.setStoredName("");
        record.setFilePath(outsideFile.toAbsolutePath().toString());
        record.setDeleted(false);

        when(fileRecordRepository.findByIdAndOwnerIdAndDeletedFalse(1L, 1L)).thenReturn(Optional.of(record));

        Optional<Resource> resource = fileService.loadOwnedFileAsResource(1L, 1L);

        assertTrue(resource.isEmpty());
    }
}
