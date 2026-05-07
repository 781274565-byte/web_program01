//处理登录认证、用户名是否存在检查、以及新用户注册
package com.campusfasttransfer.service;

import com.campusfasttransfer.dto.RegisterForm; //导入注册表单类，注册时要用它接收前端传来的数据
import com.campusfasttransfer.entity.User; //导入用户实体类，表示数据库中的用户对象
import com.campusfasttransfer.repository.UserRepository; //导入用户数据访问层，用来查库和存库
import java.time.LocalDateTime; //导入时间类，用来记录注册时间
import java.util.Objects; //导入工具类，后面用它安全比较密码
import java.util.Optional; //导入Optional
import org.springframework.dao.DataIntegrityViolationException; //导入数据库约束异常类，like唯一键冲突时会抛出
import org.springframework.stereotype.Service; //导入Spring的@Service注解
import org.springframework.transaction.annotation.Transactional; //导入事务注解

@Service //业务层Bean
@Transactional //数据库操作默认放在事务里执行
public class AuthService {

    private static final String DEFAULT_ROLE = "USER"; //表示新注册用户默认角色是USER

    private final UserRepository userRepository; //声明一个只赋值一次的仓库对象，用来操作用户数据

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> authenticate(String username, String password) { //定义登录认证方法，传入用户名和密码，返回可能存在的用户
        return userRepository.findByUsername(username) //先按用户名查询用户
                .filter(user -> Objects.equals(password, user.getPassword())); //如果查到了用户，就继续比较输入密码和数据库密码是否相等,相等才保留用户，否则返回空
    }

    //定义检查用户名是否已存在的方法
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username); //调用仓库层直接返回“这个用户名是否存在”
    }

    //定义注册方法，参数是注册表单，返回注册成功后的用户对象
    public User register(RegisterForm form) {
        //检查表单中的用户名是否已存在
        if (userRepository.existsByUsername(form.getUsername())) {
            throw new RegistrationConflictException(
                    "DUPLICATE_USERNAME",
                    "Username already exists"
            );
        }
        //检查身份证号是否已存在
        if (userRepository.existsByIdentityNo(form.getIdentityNo())) {
            throw new RegistrationConflictException(
                    "DUPLICATE_IDENTITY_NO",
                    "Identity number already exists"
            );
        }

        User user = new User(); //新建一个用户实体对象
        user.setUsername(form.getUsername());
        user.setPassword(form.getPassword());
        user.setIdentityNo(form.getIdentityNo());
        user.setRole(DEFAULT_ROLE); //给新用户设置默认角色USER
        user.setCreatedAt(LocalDateTime.now()); //把当前时间设置为创建时间

        //尝试保存用户
        try {
            return userRepository.save(user); //把用户保存到数据库，并返回保存后的对象
        } catch (DataIntegrityViolationException ex) {
            throw new RegistrationConflictException(
                    "DUPLICATE_USER",
                    "User registration conflicts with an existing record"
            );
        }
    }
}
