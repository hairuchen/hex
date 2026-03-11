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
    /** 用户→关系表→菜单 **/
    @Select("SELECT t2.* FROM user2menu t1 " +
            "INNER join \"menu\" t2 on t1.menu_id=t2.id " +
            "where t1.user_id = #{userId}")
    List<Permission> selectMenusByUserId(@Param("userId") String userId);

    /** 用户→关系表→角色→关系表→菜单 **/
    @Select("select t1.role_id,t2.menu_id from user2role t1 " +
            "INNER JOIN role2menu t2 ON t1.role_id=t2.role_id " +
            "where t1.user_id=#{userId} ")
    List<HashMap<String,String>> selectRole2MenuByUserId(@Param("userId") String userId);

    /** 用户→关系表→部门→关系表→菜单 **/
    @Select("SELECT t1.dept_id,t2.menu_id from user2dept t1 " +
            "    inner JOIN dept2menu t2 ON t1.dept_id=t2.dept_id " +
            "    where t1.user_id=#{userId} ")
    List<HashMap<String,String>> selectDept2MenuByUserId(@Param("userId") String userId);

    /** 用户→关系表→部门→关系表→角色→关系表→菜单 **/
    @Select("SELECT t1.dept_id,t2.role_id,t3.menu_id FROM user2dept t1 " +
            "inner JOIN dept2role t2 on t1.dept_id=t2.dept_id " +
            "inner JOIN role2menu t3 on t2.role_id=t3.role_id " +
            "WHERE t1.user_id=#{userId} ")
    List<HashMap<String,String>> selectDept2Role2MenuByUserId(@Param("userId") String userId);

}
