package me.chr.hex.extend.mapper;

import me.chr.hex.extend.BO.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/3/12
 **/
@Mapper
public interface PermissionMapper {
    /** 用户→关系表→权限 **/
    @Select("SELECT t2.* FROM user2permission t1 " +
            "INNER join sys_permission t2 on t1.permission_id=t2.id " +
            "where t1.user_id = #{userId}")
    List<Permission> selectPermissionsByUserId(@Param("userId") String userId);

    /** 用户→关系表→角色→关系表→权限 **/
    @Select("select t1.role_id,t2.permission_id from user2role t1 " +
            "INNER JOIN role2permission t2 ON t1.role_id=t2.role_id " +
            "where t1.user_id=#{userId} ")
    List<HashMap<String,String>> selectRole2PermissionByUserId(@Param("userId") String userId);

    /** 用户→关系表→工作空间→关系表→权限 **/
    @Select("SELECT t1.workspace_id,t2.permission_id from user2workspace t1 " +
            "    inner JOIN workspace2permission t2 ON t1.workspace_id=t2.workspace_id " +
            "    where t1.user_id=#{userId} ")
    List<HashMap<String,String>> selectWorkspace2PermissionByUserId(@Param("userId") String userId);

    /** 用户→关系表→工作空间→关系表→角色→关系表→权限 **/
    @Select("SELECT t1.workspace_id,t2.role_id,t3.permission_id FROM user2workspace t1 " +
            "inner JOIN workspace2role t2 on t1.workspace_id=t2.workspace_id " +
            "inner JOIN role2permission t3 on t2.role_id=t3.role_id " +
            "WHERE t1.user_id=#{userId} ")
    List<HashMap<String,String>> selectWorkspace2Role2PermissionByUserId(@Param("userId") String userId);

}
