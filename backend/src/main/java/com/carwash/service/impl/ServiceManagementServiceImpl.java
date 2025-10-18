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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@org.springframework.stereotype.Service
public class ServiceManagementServiceImpl implements ServiceManagementService {

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
}