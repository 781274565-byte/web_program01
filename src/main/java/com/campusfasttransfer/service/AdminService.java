package com.campusfasttransfer.service;

import com.campusfasttransfer.entity.FileRecord; //导入FileRecord实体类，后面要用它表示“文件记录”对象
import com.campusfasttransfer.entity.User; //导入User实体类，后面要用它表示“用户”对象
import com.campusfasttransfer.repository.FileRecordRepository; //导入文件记录的数据访问接口，用来查数据库里的文件数据
import com.campusfasttransfer.repository.UserRepository; //导入用户的数据访问接口，用来查数据库里的用户数据
import java.util.List; //导入List集合类型，因为后面方法返回的是多个用户或多个文件
import org.springframework.data.domain.Sort; //导入Spring Data的排序类，用来指定按什么字段排序
import org.springframework.stereotype.Service; //导入@Service注解
import org.springframework.transaction.annotation.Transactional; //导入事务注解，用来控制这个类的方法是否在事务中运行

@Service
@Transactional(readOnly = true) //默认运行在“只读事务”中
public class AdminService {

    private final UserRepository userRepository; //保存用户仓库对象
    private final FileRecordRepository fileRecordRepository; //保存文件仓库对象

    public AdminService(UserRepository userRepository, FileRecordRepository fileRecordRepository) {
        this.userRepository = userRepository;
        this.fileRecordRepository = fileRecordRepository;
    }

    public List<User> listUsers() { //返回用户列表
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")); //查询所有用户，并按创建时间从新到旧排列
    }

    public List<FileRecord> listFiles() { //返回文件记录列表
        return fileRecordRepository.findAll(Sort.by(Sort.Direction.DESC, "uploadedAt")); //查询所有文件，并按上传时间从新到旧排列
    }
}
