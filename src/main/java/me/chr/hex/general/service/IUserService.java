package me.chr.hex.general.service;

import me.chr.hex.extend.DTO.UserDTO;
import me.chr.hex.general.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户实体表 服务类
 * </p>
 *
 * @author baomidou
 * @since 2026-03-11
 */
public interface IUserService extends IService<User> {

    List<User> batchCreate(List<UserDTO> userDTOList);
}
