package com.campusfasttransfer.repository;

import com.campusfasttransfer.entity.DownloadLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DownloadLogRepository extends JpaRepository<DownloadLog, Long> {

    List<DownloadLog> findByFileIdOrderByDownloadTimeDesc(Long fileId);
}