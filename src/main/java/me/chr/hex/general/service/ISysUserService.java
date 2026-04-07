package me.chr.hex.general.service;

import me.chr.hex.extend.DTO.UserDTO;
import me.chr.hex.general.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author baomidou
 * @since 2026-04-10
 */
public interface ISysUserService extends IService<SysUser> {

    List<SysUser> batchCreate(List<UserDTO> userDTOList);

}
