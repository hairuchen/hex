package me.chr.hex.general.service.impl;

import me.chr.hex.general.entity.Tenant;
import me.chr.hex.general.mapper.TenantMapper;
import me.chr.hex.general.service.ITenantService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 租户表（系统顶层隔离边界，无删除操作，通过状态控制） 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2026-04-10
 */
@Service
public class TenantServiceImpl extends ServiceImpl<TenantMapper, Tenant> implements ITenantService {

}
