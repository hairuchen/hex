package me.chr.hex.general.service.impl;

import me.chr.hex.general.entity.User2dept;
import me.chr.hex.general.mapper.User2deptMapper;
import me.chr.hex.general.service.IUser2deptService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户与部门的归属关系表 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2026-03-11
 */
@Service
public class User2deptServiceImpl extends ServiceImpl<User2deptMapper, User2dept> implements IUser2deptService {

}
