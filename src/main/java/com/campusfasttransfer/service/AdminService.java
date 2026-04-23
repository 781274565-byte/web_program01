package com.campusfasttransfer.service;

import com.campusfasttransfer.entity.FileRecord;
import com.campusfasttransfer.entity.User;
import com.campusfasttransfer.repository.FileRecordRepository;
import com.campusfasttransfer.repository.UserRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final FileRecordRepository fileRecordRepository;

    public AdminService(UserRepository userRepository, FileRecordRepository fileRecordRepository) {
        this.userRepository = userRepository;
        this.fileRecordRepository = fileRecordRepository;
    }

    public List<User> listUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public List<FileRecord> listFiles() {
        return fileRecordRepository.findAll(Sort.by(Sort.Direction.DESC, "uploadedAt"));
    }
}
