package com.carwash.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 后端管理系统控制器
 * 提供独立的后端管理界面
 * 注意：路径改为 /backend-admin 以避免与前端 Vue 路由冲突
 */
@Controller
@RequestMapping("/backend-admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    /**
     * 管理后台首页
     */
    @GetMapping("")
    public String adminHome(Model model) {
        model.addAttribute("title", "汽车洗车服务管理后台");
        return "admin/index";
    }

    /**
     * 管理后台仪表盘
     */
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("title", "管理后台 - 仪表盘");
        return "admin/dashboard";
    }

    /**
     * 用户管理页面
     */
    @GetMapping("/users")
    public String adminUsers(Model model) {
        model.addAttribute("title", "管理后台 - 用户管理");
        return "admin/users";
    }

    /**
     * 预约管理页面
     */
    @GetMapping("/bookings")
    public String adminBookings(Model model) {
        model.addAttribute("title", "管理后台 - 预约管理");
        return "admin/bookings";
    }

    /**
     * 服务管理页面
     */
    @GetMapping("/services")
    public String adminServices(Model model) {
        model.addAttribute("title", "管理后台 - 服务管理");
        return "admin/services";
    }

    /**
     * 系统设置页面
     */
    @GetMapping("/settings")
    public String adminSettings(Model model) {
        model.addAttribute("title", "管理后台 - 系统设置");
        return "admin/settings";
    }
}