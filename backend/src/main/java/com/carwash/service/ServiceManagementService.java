package com.carwash.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carwash.dto.ServiceRequest;
import com.carwash.dto.ServiceResponse;

import java.util.List;

/**
 * 服务管理服务接口
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
public interface ServiceManagementService {

    /**
     * 创建服务项目
     */
    Long createService(ServiceRequest request);

    /**
     * 更新服务项目
     */
    void updateService(Long serviceId, ServiceRequest request);

    /**
     * 删除服务项目
     */
    void deleteService(Long serviceId);

    /**
     * 永久删除服务项目（硬删除）
     * 完全从数据库中移除服务数据，此操作不可恢复
     */
    void permanentlyDeleteService(Long serviceId);

    /**
     * 根据ID获取服务项目
     */
    ServiceResponse getServiceById(Long serviceId);

    /**
     * 分页查询所有服务项目
     */
    IPage<ServiceResponse> getAllServices(Page<ServiceResponse> page);

    /**
     * 分页查询可用服务项目
     */
    IPage<ServiceResponse> getAvailableServices(Page<ServiceResponse> page);

    /**
     * 根据分类查询服务项目
     */
    List<ServiceResponse> getServicesByCategory(String category);

    /**
     * 搜索服务项目
     */
    List<ServiceResponse> searchServices(String keyword);

    /**
     * 获取所有服务分类
     */
    List<String> getAllCategories();

    /**
     * 更新服务状态
     */
    void updateServiceStatus(Long serviceId, Integer status);
}