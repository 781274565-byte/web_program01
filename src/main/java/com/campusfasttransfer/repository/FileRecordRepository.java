package com.campusfasttransfer.repository;

import com.campusfasttransfer.entity.FileRecord; //导入FileRecord实体类
import java.util.List; //导入List集合类型
import java.util.Optional; //导入Optional类型
import org.springframework.data.jpa.repository.JpaRepository; //导入Spring Data JPA提供的JpaRepository接口

public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {

    List<FileRecord> findByOwnerIdAndDeletedFalseOrderByUploadedAtDesc(Long ownerId); //根据方法名自动生成查询

    Optional<FileRecord> findByIdAndDeletedFalse(Long id); //根据文件id查找未删除的文件记录

    Optional<FileRecord> findByShareCodeAndDeletedFalse(String shareCode); //根据分享码获取未删除的文件记录

    Optional<FileRecord> findByIdAndOwnerIdAndDeletedFalse(Long id, Long ownerId); //查询某个用户自己的某个未删除文件
}
