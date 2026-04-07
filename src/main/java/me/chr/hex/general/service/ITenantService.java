package me.chr.hex.general.service;

import me.chr.hex.general.entity.Tenant;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 租户表（系统顶层隔离边界，无删除操作，通过状态控制） 服务类
 * </p>
 *
 * @author baomidou
 * @since 2026-04-10
 */
public interface ITenantService extends IService<Tenant> {

}
