package com.campusfasttransfer.service;

import com.campusfasttransfer.entity.DownloadLog; //导入DownloadLog实体类，后面记录下载日志时要创建这个对象
import com.campusfasttransfer.entity.FileRecord; //导入FileRecord实体类，表示文件记录对象
import com.campusfasttransfer.repository.DownloadLogRepository; //导入DownloadLogRepository，用于把下载日志保存到数据库
import com.campusfasttransfer.repository.FileRecordRepository; //导入FileRecordRepository，用于查询和保存文件记录
import java.io.IOException; //导入IOException，后面创建目录失败时要捕获这个异常
import java.nio.file.Files; //导入Files 工具类，用于创建目录、判断文件是否存在等
import java.nio.file.Path; //导入Path，表示文件路径对象
import java.nio.file.Paths; //导入Paths，用于把字符串路径转成Path对象
import java.time.LocalDateTime; //导入LocalDateTime，用于处理过期时间和下载时间
import java.util.Locale; //导入Locale，后面生成分享码转大写时使用固定区域设置
import java.util.Optional; //导入Optional，表示“可能有值，也可能没值”
import java.util.UUID; //导入UUID，后面用来生成随机分享码
import org.springframework.beans.factory.annotation.Value; //导入@Value，用于读取配置文件里的app.upload-dir
import org.springframework.core.io.FileSystemResource; //导入FileSystemResource，用于把磁盘上的文件包装成Spring可返回的资源对象
import org.springframework.core.io.Resource; //导入Resource，这是Spring对文件资源的统一抽象接口
import org.springframework.stereotype.Service; //导入@Service，把这个类标记为Spring的业务组件
import org.springframework.transaction.annotation.Transactional; //导入@Transactional，表示这个类里的数据库操作默认放在事务里
import org.springframework.util.StringUtils; //导入StringUtils，用于判断字符串是否有实际内容

@Service
@Transactional
public class ShareService {

    private final FileRecordRepository fileRecordRepository;//操作文件记录表
    private final DownloadLogRepository downloadLogRepository;//操作下载日志表
    private final Path uploadDirectory;//保存上传目录的路径对象

    public ShareService(FileRecordRepository fileRecordRepository,
                        DownloadLogRepository downloadLogRepository,
                        @Value("${app.upload-dir}") String uploadDir) {
        this.fileRecordRepository = fileRecordRepository;
        this.downloadLogRepository = downloadLogRepository;
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();//把配置中的上传目录字符串转成绝对路径
        try { //尝试创建上传目录
            Files.createDirectories(this.uploadDirectory);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to initialize upload directory", ex);
        }
    }

    public String enableShare(FileRecord record) { //传入一个文件记录对象，返回生成的分享码
        if (!isNotExpired(record)) { //检查文件是否没过期
            throw new IllegalStateException("Cannot enable sharing for an expired file.");
        }

        String shareCode = generateUniqueShareCode(); //生成唯一分享码
        record.setShareCode(shareCode); //写进record对象里
        record.setShared(true);
        fileRecordRepository.save(record); //把修改后的文件记录保存到数据库
        return shareCode;
    }

    public Optional<FileRecord> validateShare(String shareCode) {
        if (!StringUtils.hasText(shareCode)) { //判断传入的分享码是否为空或只有空白字符
            return Optional.empty();
        }

        return fileRecordRepository.findByShareCodeAndDeletedFalse(shareCode)
                .filter(FileRecord::isShared)//要求文件的isShared必须为true
                .filter(this::isNotExpired);//要求文件没有过期
    }
    //记录文件下载行为
    public void logDownload(Long fileId, String username) {
        DownloadLog downloadLog = new DownloadLog();
        downloadLog.setFileId(fileId);
        downloadLog.setDownloaderName(StringUtils.hasText(username) ? username : "unknown");
        downloadLog.setDownloadTime(LocalDateTime.now());
        downloadLogRepository.save(downloadLog);
    }

    public Resource loadSharedFile(FileRecord record) {
        Path filePath = resolveManagedPath(record) //解析真实文件路径
                .orElseThrow(() -> new IllegalStateException("Shared file is not available"));
        if (!Files.exists(filePath)) { //检查文件在磁盘上是否真的存在
            throw new IllegalStateException("Shared file is not available");
        }
        return new FileSystemResource(filePath);
    }
    //生成唯一分享码
    private String generateUniqueShareCode() {
        String shareCode;
        do {
            shareCode = UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 8) //取前8位
                    .toUpperCase(Locale.ROOT); //转成大写
        } while (fileRecordRepository.findByShareCodeAndDeletedFalse(shareCode).isPresent());//如果数据库里已经存在同样的分享码，就继续循环重新生成
        return shareCode;
    }
    //判断文件是否没过期
    private boolean isNotExpired(FileRecord record) {
        LocalDateTime expireTime = record.getExpireTime();
        return expireTime == null || expireTime.isAfter(LocalDateTime.now());
    }

    private Optional<Path> resolveManagedPath(FileRecord fileRecord) { //根据FileRecord解析出受管理的真实文件路径
        Path candidate;
        if (StringUtils.hasText(fileRecord.getStoredName())) { //storedName有值
            candidate = uploadDirectory.resolve(fileRecord.getStoredName()).normalize();
        } else if (StringUtils.hasText(fileRecord.getFilePath())) { //filePath有值
            candidate = Paths.get(fileRecord.getFilePath()).normalize();
            if (!candidate.isAbsolute()) { //判断这个路径是不是绝对路径
                candidate = uploadDirectory.resolve(candidate).normalize(); //当成相对上传目录的路径再拼接一次
            }
        } else {
            return Optional.empty();
        }

        if (!candidate.startsWith(uploadDirectory)) { //检查解析出的路径是否仍然位于上传目录里
            return Optional.empty();
        }
        return Optional.of(candidate);
    }
}
