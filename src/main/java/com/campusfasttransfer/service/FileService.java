//文件上传到磁盘,文件元数据保存到数据库.只允许文件主人查看/删除,防止非法路径访问
package com.campusfasttransfer.service;

import com.campusfasttransfer.entity.FileRecord; //导入FileRecord 实体类，表示文件记录对象
import com.campusfasttransfer.repository.FileRecordRepository; //导入FileRecordRepository，用于操作数据库里的文件记录
import java.io.IOException; //导入IOException，处理输入输出异常
import java.io.InputStream; //导入InputStream，表示文件输入流
import java.nio.file.Files; //导入Files，提供文件读写、创建目录等工具方法
import java.nio.file.Path; //导入Path，表示文件路径对象
import java.nio.file.Paths; //导入Paths，用于创建 Path
import java.nio.file.StandardCopyOption; //导入 StandardCopyOption，指定文件复制时的行为
import java.time.LocalDateTime; //导入LocalDateTime，表示日期时间
import java.util.List; //导入List，表示列表集合
import java.util.Optional; //导入Optional
import java.util.UUID; //导入UUID，用于生成唯一文件名
import org.springframework.beans.factory.annotation.Value; //导入 Value，从配置文件读取值
import org.springframework.core.io.FileSystemResource; //导入FileSystemResource，把磁盘文件包装成 Spring 资源
import org.springframework.core.io.Resource; //导入Resource，Spring 统一的资源接口
import org.springframework.stereotype.Service; //导入@Service，把这个类标记为业务层 Bean
import org.springframework.transaction.annotation.Transactional; //导入 @Transactional，让这个类的方法默认运行在事务中
import org.springframework.util.StringUtils; //导入StringUtils，用于判断字符串是否有内容
import org.springframework.web.multipart.MultipartFile; //导入MultipartFile，表示前端上传的文件

@Service
@Transactional
public class FileService {
    //构造方法
    private final FileRecordRepository fileRecordRepository; //保存仓库对象
    private final Path uploadDirectory; //保存上传目录路径

    public FileService(FileRecordRepository fileRecordRepository,
                       @Value("${app.upload-dir}") String uploadDir) {
        this.fileRecordRepository = fileRecordRepository;
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize(); //把上传目录字符串转成绝对路径，并做标准化处理
        try { //尝试初始化上传目录
            Files.createDirectories(this.uploadDirectory);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to initialize upload directory", ex);
        }
    }
    //查询方法
    public List<FileRecord> listOwnedFiles(Long ownerId) { //按用户ID查询该用户的文件列表
        return fileRecordRepository.findByOwnerIdAndDeletedFalseOrderByUploadedAtDesc(ownerId);//查出“属于该用户且未删除”的文件，并按上传时间倒序返回
    }

    public Optional<FileRecord> findOwnedFile(Long id, Long ownerId) { //按文件ID和用户ID查单个文件
        return fileRecordRepository.findByIdAndOwnerIdAndDeletedFalse(id, ownerId);
    }
    //保存上传文件    
    public FileRecord saveUploadedFile(MultipartFile file, LocalDateTime expireTime, Long ownerId) { //接收上传文件、过期时间、拥有者ID
        String originalName = normalizeOriginalName(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + "_" + sanitizeFileName(originalName); //生成保存到磁盘的文件名
        Path storedPath = uploadDirectory.resolve(storedName).normalize();//把保存文件名拼接到上传目录下，得到最终存储路径

        try (InputStream inputStream = file.getInputStream()) { //打开上传文件的输入流，结束后会自动关闭
            Files.copy(inputStream, storedPath, StandardCopyOption.REPLACE_EXISTING);//把上传内容复制到目标路径；如果已存在同名文件就覆盖
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store uploaded file", ex);
        }

        FileRecord record = new FileRecord(); //创建一个新的FileRecord实体对象，准备存数据库
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

        try { //尝试保存数据库记录
            return fileRecordRepository.save(record);
        } catch (RuntimeException ex) {
            try { //回滚清理
                Files.deleteIfExists(storedPath); //避免磁盘有文件但数据库没记录
            } catch (IOException cleanupEx) {
                ex.addSuppressed(cleanupEx);
            }
            throw ex;
        }
    }
    //读取与软删除
    public Optional<Resource> loadOwnedFileAsResource(Long id, Long ownerId) { //把文件加载成可下载资源
        return findOwnedFile(id, ownerId) //查这个文件是否属于当前用户
                .flatMap(fileRecord -> resolveManagedPath(fileRecord) //把数据库记录解析成安全路径
                        .filter(Files::exists)//过滤掉磁盘上不存在的文件
                        .map(FileSystemResource::new));
    }

    public boolean softDeleteOwnedFile(Long id, Long ownerId) { //按文件ID和用户ID做软删除
        return findOwnedFile(id, ownerId)
                .map(fileRecord -> {
                    fileRecord.setDeleted(true);
                    fileRecordRepository.save(fileRecord);
                    return true;
                })
                .orElse(false);
    }
    //文件名处理
    private String normalizeOriginalName(String originalName) {
        if (!StringUtils.hasText(originalName)) {
            return "uploaded-file";
        }
        String cleaned = originalName.replace("\\", "/");
        return cleaned.substring(cleaned.lastIndexOf('/') + 1); //只保留最后一级文件名，去掉可能带上的路径部分
    }

    private String sanitizeFileName(String originalName) { //清洗文件名
        return originalName.replaceAll("[^a-zA-Z0-9._-]", "_"); //把不是字母、数字、点、下划线、短横线的字符全部替换成_
    }
    //安全路径解析
    private Optional<Path> resolveManagedPath(FileRecord fileRecord) { //把数据库中的文件信息解析成受控路径
        Path candidate;  //候选路径
        if (StringUtils.hasText(fileRecord.getStoredName())) { //如果storedName有内容
            candidate = uploadDirectory.resolve(fileRecord.getStoredName()).normalize();//上传目录 +storedName
        } else if (StringUtils.hasText(fileRecord.getFilePath())) { //如果filePath有内容
            candidate = Paths.get(fileRecord.getFilePath()).normalize(); //把数据库里的filePath转成Path
            if (!candidate.isAbsolute()) { //路径不是绝对路径
                candidate = uploadDirectory.resolve(candidate).normalize(); //把它看成相对上传目录的路径，再拼成完整路径
            }
        } else {
            return Optional.empty(); //无法解析路径
        }

        if (!candidate.startsWith(uploadDirectory)) { //检查最终路径是否仍然在上传目录下面
            return Optional.empty();
        }
        return Optional.of(candidate);
    }
}
