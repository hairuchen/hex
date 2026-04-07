package me.chr.hex.general.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.BizException;
import me.chr.hex.extend.DTO.RoleDTO;
import me.chr.hex.general.entity.SysRole;
import me.chr.hex.general.mapper.SysRoleMapper;
import me.chr.hex.general.service.ISysRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 角色服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2026-04-10
 */
@Slf4j
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Override
    public List<SysRole> batchCreate(List<RoleDTO> roleDTOList) {
        try {
            // 持久化
            List<SysRole> roleList= new ArrayList<>();
            for (RoleDTO roleDTO : roleDTOList) {
                SysRole role = roleDTO.toSysRole();
                roleList.add(role);
            }
            this.saveBatch(roleList);
            return roleList;
        }catch (Exception e){
            log.error("创建角色失败!"+e);
            throw new BizException("创建角色失败!");
        }
    }
}
