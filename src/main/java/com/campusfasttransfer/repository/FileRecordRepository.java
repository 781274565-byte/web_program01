package com.campusfasttransfer.repository;

import com.campusfasttransfer.entity.FileRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {

    List<FileRecord> findByOwnerIdAndDeletedFalseOrderByUploadedAtDesc(Long ownerId);

    Optional<FileRecord> findByIdAndDeletedFalse(Long id);

    Optional<FileRecord> findByShareCodeAndDeletedFalse(String shareCode);

    Optional<FileRecord> findByIdAndOwnerIdAndDeletedFalse(Long id, Long ownerId);
}
