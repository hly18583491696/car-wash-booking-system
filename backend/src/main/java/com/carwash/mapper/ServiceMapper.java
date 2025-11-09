package com.carwash.mapper;

import com.carwash.entity.Service;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 服务项目数据访问层
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Mapper
public interface ServiceMapper extends BaseMapper<Service> {

    /**
     * 分页查询可用服务
     */
    @Select("SELECT * FROM services WHERE status = 1 AND deleted = 0 ORDER BY sort_order ASC, created_at DESC")
    IPage<Service> selectAvailableServices(Page<Service> page);

    /**
     * 根据分类查询服务
     */
    @Select("SELECT * FROM services WHERE category = #{category} AND status = 1 AND deleted = 0 ORDER BY sort_order ASC")
    List<Service> selectByCategory(@Param("category") String category);

    /**
     * 查询所有分类
     */
    @Select("SELECT DISTINCT category FROM services WHERE status = 1 AND deleted = 0 ORDER BY category")
    List<String> selectAllCategories();

    /**
     * 根据关键词搜索服务
     */
    @Select("SELECT * FROM services WHERE (name LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%')) AND status = 1 AND deleted = 0 ORDER BY sort_order ASC")
    List<Service> searchServices(@Param("keyword") String keyword);

    /**
     * 更新服务状态
     */
    @Update("UPDATE services SET status = #{status} WHERE id = #{serviceId}")
    int updateStatus(@Param("serviceId") Long serviceId, @Param("status") Integer status);

    /**
     * 统计总服务数
     */
    @Select("SELECT COUNT(*) FROM services WHERE deleted = 0")
    int countTotalServices();

    /**
     * 统计可用服务数
     */
    @Select("SELECT COUNT(*) FROM services WHERE status = 1 AND deleted = 0")
    int countAvailableServices();
}