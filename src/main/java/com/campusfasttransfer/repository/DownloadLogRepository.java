package com.campusfasttransfer.repository;

import com.campusfasttransfer.entity.DownloadLog; //导入DownloadLog实体类
import java.util.List; //导入List集合类型
import org.springframework.data.jpa.repository.JpaRepository; //导入Spring Data JPA提供的JpaRepository接口
public interface DownloadLogRepository extends JpaRepository<DownloadLog, Long> {

    List<DownloadLog> findByFileIdOrderByDownloadTimeDesc(Long fileId); //查询文件fileId对应的所有下载日志，并按下载时间从新到旧排序返回
}