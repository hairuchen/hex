package me.chr.hex.general.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.BizException;
import me.chr.hex.extend.DTO.UserDTO;
import me.chr.hex.general.entity.User;
import me.chr.hex.general.mapper.UserMapper;
import me.chr.hex.general.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户实体表 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2026-03-11
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<User> batchCreate(List<UserDTO> userDTOList) {
        try {
            // 1.幂等校验
            Set<String> usernameSet = userDTOList.stream()
                    .map(UserDTO::getUsername)
                    .collect(Collectors.toSet());
            if (usernameSet.size()!=userDTOList.size()){
                throw new BizException("有重复数据项,请检查!");
            }
            // 2.数据库校验
            List<User> existUserList = this.list(new LambdaQueryWrapper<User>().in(User::getUsername, usernameSet));
            if (!existUserList.isEmpty()) {
                String duplicateNames = existUserList.stream()
                        .map(User::getUsername)
                        .collect(Collectors.joining(", "));
                throw new BizException("以下用户名已存在，无法重复创建: " + duplicateNames);
            }

            // 3.持久化
            List<User> userList = new ArrayList<>();
            for (UserDTO userDTO : userDTOList) {
                String encryptedPassword = passwordEncoder.encode(userDTO.getPassword());
                User user = userDTO.ToUser(encryptedPassword,null);
                userList.add(user);
            }
            this.saveBatch(userList);
            return userList;
        }catch (Exception e){
            throw new BizException("创建用户失败:"+e.getMessage());
        }
    }
}
