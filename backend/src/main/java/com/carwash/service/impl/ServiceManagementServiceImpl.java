package com.carwash.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carwash.common.BusinessException;
import com.carwash.common.result.ResultCode;
import com.carwash.dto.ServiceRequest;
import com.carwash.dto.ServiceResponse;
import com.carwash.entity.Service;
import com.carwash.mapper.ServiceMapper;
import com.carwash.service.ServiceManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务管理服务实现类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@org.springframework.stereotype.Service
public class ServiceManagementServiceImpl implements ServiceManagementService {

    private static final Logger log = LoggerFactory.getLogger(ServiceManagementServiceImpl.class);

    @Autowired
    private ServiceMapper serviceMapper;

    @Override
    @Transactional
    public Long createService(ServiceRequest request) {
        log.info("创建服务项目开始，服务名称: {}", request.getName());

        Service service = new Service();
        BeanUtils.copyProperties(request, service);
        service.setStatus(1); // 默认上架状态

        int result = serviceMapper.insert(service);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "创建服务项目失败");
        }

        log.info("服务项目创建成功，服务ID: {}", service.getId());
        return service.getId();
    }

    @Override
    @Transactional
    public void updateService(Long serviceId, ServiceRequest request) {
        log.info("更新服务项目开始，服务ID: {}", serviceId);

        Service service = serviceMapper.selectById(serviceId);
        if (service == null) {
            throw new BusinessException(ResultCode.SERVICE_NOT_FOUND);
        }

        BeanUtils.copyProperties(request, service);
        service.setId(serviceId);

        int result = serviceMapper.updateById(service);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "更新服务项目失败");
        }

        log.info("服务项目更新成功，服务ID: {}", serviceId);
    }

    @Override
    @Transactional
    public void deleteService(Long serviceId) {
        log.info("删除服务项目开始，服务ID: {}", serviceId);

        Service service = serviceMapper.selectById(serviceId);
        if (service == null) {
            throw new BusinessException(ResultCode.SERVICE_NOT_FOUND);
        }

        int result = serviceMapper.deleteById(serviceId);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "删除服务项目失败");
        }

        log.info("服务项目删除成功，服务ID: {}", serviceId);
    }

    @Override
    public ServiceResponse getServiceById(Long serviceId) {
        Service service = serviceMapper.selectById(serviceId);
        if (service == null) {
            throw new BusinessException(ResultCode.SERVICE_NOT_FOUND);
        }

        ServiceResponse response = new ServiceResponse();
        BeanUtils.copyProperties(service, response);
        return response;
    }

    @Override
    public IPage<ServiceResponse> getAllServices(Page<ServiceResponse> page) {
        QueryWrapper<Service> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort_order").orderByDesc("created_at");

        IPage<Service> servicePage = serviceMapper.selectPage(new Page<>(page.getCurrent(), page.getSize()), queryWrapper);
        
        return servicePage.convert(service -> {
            ServiceResponse response = new ServiceResponse();
            BeanUtils.copyProperties(service, response);
            return response;
        });
    }

    @Override
    public IPage<ServiceResponse> getAvailableServices(Page<ServiceResponse> page) {
        IPage<Service> servicePage = serviceMapper.selectAvailableServices(new Page<>(page.getCurrent(), page.getSize()));
        
        return servicePage.convert(service -> {
            ServiceResponse response = new ServiceResponse();
            BeanUtils.copyProperties(service, response);
            return response;
        });
    }

    @Override
    public List<ServiceResponse> getServicesByCategory(String category) {
        List<Service> services = serviceMapper.selectByCategory(category);
        
        return services.stream().map(service -> {
            ServiceResponse response = new ServiceResponse();
            BeanUtils.copyProperties(service, response);
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ServiceResponse> searchServices(String keyword) {
        List<Service> services = serviceMapper.searchServices(keyword);
        
        return services.stream().map(service -> {
            ServiceResponse response = new ServiceResponse();
            BeanUtils.copyProperties(service, response);
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> getAllCategories() {
        return serviceMapper.selectAllCategories();
    }

    @Override
    @Transactional
    public void updateServiceStatus(Long serviceId, Integer status) {
        log.info("更新服务状态开始，服务ID: {}, 状态: {}", serviceId, status);

        Service service = serviceMapper.selectById(serviceId);
        if (service == null) {
            throw new BusinessException(ResultCode.SERVICE_NOT_FOUND);
        }

        int result = serviceMapper.updateStatus(serviceId, status);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "更新服务状态失败");
        }

        log.info("服务状态更新成功，服务ID: {}", serviceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void permanentlyDeleteService(Long serviceId) {
        log.info("永久删除服务项目（硬删除），服务ID: {}", serviceId);

        // 验证服务ID
        if (serviceId == null) {
            log.error("服务ID为空");
            throw new BusinessException(ResultCode.PARAM_ERROR, "服务ID不能为空");
        }

        // 检查服务是否存在（包括已软删除的服务）
        Service service = serviceMapper.selectById(serviceId);
        if (service == null) {
            log.error("服务不存在，服务ID: {}", serviceId);
            throw new BusinessException(ResultCode.SERVICE_NOT_FOUND, "服务不存在");
        }

        String serviceName = service.getName();
        log.info("服务硬删除前状态 - 服务ID: {}, 服务名称: {}, deleted: {}, 状态: {}", 
            serviceId, serviceName, service.getDeleted(), service.getStatus());

        // 执行硬删除，从数据库中完全移除记录
        log.info("执行数据库删除操作");
        int result = serviceMapper.deleteById(serviceId);
        log.info("数据库删除结果: {}", result);
        
        if (result <= 0) {
            log.error("硬删除服务失败，数据库操作失败，服务ID: {}, 影响行数: {}", serviceId, result);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "硬删除服务失败，数据库操作失败");
        }

        // 验证删除结果，确认记录不再存在
        log.info("验证数据库删除结果");
        Service deletedService = serviceMapper.selectById(serviceId);
        
        if (deletedService != null) {
            log.error("验证失败：删除后服务仍然存在，服务ID: {}", serviceId);
            
            // 尝试重新删除一次
            log.info("尝试重新删除服务");
            int retryResult = serviceMapper.deleteById(serviceId);
            
            if (retryResult <= 0) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "重试硬删除服务失败");
            }
            
            // 再次验证
            Service finalService = serviceMapper.selectById(serviceId);
            if (finalService != null) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, 
                    "数据库删除验证失败，记录仍然存在");
            }
        }

        log.info("服务硬删除成功，服务ID: {}, 服务名称: {}, 记录已从数据库中完全移除", serviceId, serviceName);
    }
}