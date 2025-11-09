package com.carwash.mapper;

import com.carwash.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户数据访问层
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名或手机号查询用户
     */
    @Select("SELECT * FROM users WHERE (username = #{identifier} OR phone = #{identifier}) AND deleted = 0")
    User findByUsernameOrPhone(@Param("identifier") String identifier);



    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM users WHERE phone = #{phone} AND deleted = 0")
    User findByPhone(@Param("phone") String phone);

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM users WHERE email = #{email} AND deleted = 0")
    User findByEmail(@Param("email") String email);

    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) FROM users WHERE username = #{username} AND deleted = 0")
    int countByUsername(@Param("username") String username);

    /**
     * 检查手机号是否存在
     */
    @Select("SELECT COUNT(*) FROM users WHERE phone = #{phone} AND deleted = 0")
    int countByPhone(@Param("phone") String phone);

    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) FROM users WHERE email = #{email} AND deleted = 0")
    int countByEmail(@Param("email") String email);

    /**
     * 统计总用户数
     */
    @Select("SELECT COUNT(*) FROM users WHERE deleted = 0")
    int countTotalUsers();

    /**
     * 统计今日新增用户数
     */
    @Select("SELECT COUNT(*) FROM users WHERE DATE(created_at) = CURDATE() AND deleted = 0")
    int countTodayNewUsers();

    /**
     * 统计月度新增用户数
     */
    @Select("SELECT COUNT(*) FROM users WHERE YEAR(created_at) = YEAR(CURDATE()) AND MONTH(created_at) = MONTH(CURDATE()) AND deleted = 0")
    int countMonthlyNewUsers();
}