package com.carwash.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carwash.common.result.Result;
import com.carwash.dto.ServiceRequest;
import com.carwash.dto.ServiceResponse;
import com.carwash.service.ServiceManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 服务管理控制器
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/services")
@Tag(name = "服务管理", description = "洗车服务项目管理相关接口")
public class ServiceController {

    @Autowired
    private ServiceManagementService serviceManagementService;

    /**
     * 获取可用服务列表（公开接口）
     */
    @GetMapping("/list")
    @Operation(summary = "获取服务列表", description = "获取所有可用的洗车服务项目")
    public Result<IPage<ServiceResponse>> getAvailableServices(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        
        Page<ServiceResponse> page = new Page<>(current, size);
        IPage<ServiceResponse> result = serviceManagementService.getAvailableServices(page);
        return Result.success(result);
    }

    /**
     * 根据ID获取服务详情（公开接口）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取服务详情", description = "根据服务ID获取详细信息")
    public Result<ServiceResponse> getServiceById(@PathVariable Long id) {
        ServiceResponse service = serviceManagementService.getServiceById(id);
        return Result.success(service);
    }

    /**
     * 根据分类获取服务（公开接口）
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "按分类获取服务", description = "根据服务分类获取服务列表")
    public Result<List<ServiceResponse>> getServicesByCategory(@PathVariable String category) {
        List<ServiceResponse> services = serviceManagementService.getServicesByCategory(category);
        return Result.success(services);
    }

    /**
     * 搜索服务（公开接口）
     */
    @GetMapping("/search")
    @Operation(summary = "搜索服务", description = "根据关键词搜索服务")
    public Result<List<ServiceResponse>> searchServices(@RequestParam String keyword) {
        List<ServiceResponse> services = serviceManagementService.searchServices(keyword);
        return Result.success(services);
    }

    /**
     * 获取所有分类（公开接口）
     */
    @GetMapping("/categories")
    @Operation(summary = "获取服务分类", description = "获取所有服务分类列表")
    public Result<List<String>> getAllCategories() {
        List<String> categories = serviceManagementService.getAllCategories();
        return Result.success(categories);
    }

    // ========== 管理员接口 ==========

    /**
     * 创建服务项目（管理员）
     */
    @PostMapping
    @Operation(summary = "创建服务", description = "创建新的洗车服务项目")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> createService(@Valid @RequestBody ServiceRequest request) {
        log.info("创建服务项目请求，服务名称: {}", request.getName());
        Long serviceId = serviceManagementService.createService(request);
        return Result.success(serviceId);
    }

    /**
     * 更新服务项目（管理员）
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新服务", description = "更新洗车服务项目信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateService(@PathVariable Long id, @Valid @RequestBody ServiceRequest request) {
        log.info("更新服务项目请求，服务ID: {}", id);
        serviceManagementService.updateService(id, request);
        return Result.success("服务更新成功");
    }

    /**
     * 删除服务项目（管理员）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除服务", description = "删除洗车服务项目")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteService(@PathVariable Long id) {
        log.info("删除服务项目请求，服务ID: {}", id);
        serviceManagementService.deleteService(id);
        return Result.success("服务删除成功");
    }

    /**
     * 获取所有服务列表（管理员）
     */
    @GetMapping("/admin/all")
    @Operation(summary = "获取所有服务", description = "管理员获取所有服务项目（包括下架的）")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<ServiceResponse>> getAllServices(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        
        Page<ServiceResponse> page = new Page<>(current, size);
        IPage<ServiceResponse> result = serviceManagementService.getAllServices(page);
        return Result.success(result);
    }

    /**
     * 更新服务状态（管理员）
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新服务状态", description = "更新服务的上架/下架状态")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateServiceStatus(@PathVariable Long id, @RequestParam Integer status) {
        log.info("更新服务状态请求，服务ID: {}, 状态: {}", id, status);
        serviceManagementService.updateServiceStatus(id, status);
        return Result.success("服务状态更新成功");
    }
}