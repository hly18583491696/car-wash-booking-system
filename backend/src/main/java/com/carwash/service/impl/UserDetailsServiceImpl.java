package com.carwash.service.impl;

import com.carwash.entity.User;
import com.carwash.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 用户详情服务实现类
 * 实现Spring Security的UserDetailsService接口
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username).or().eq("phone", username);
        User user = userMapper.selectOne(queryWrapper);
        
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }
        
        // 构建用户权限
        Collection<GrantedAuthority> authorities = getAuthorities(user);
        
        // 返回Spring Security的User对象
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.getStatus() == 1, // enabled
            true, // accountNonExpired
            true, // credentialsNonExpired
            true, // accountNonLocked
            authorities
        );
    }

    /**
     * 获取用户权限
     */
    private Collection<GrantedAuthority> getAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // 根据用户角色设置权限
        switch (user.getRole()) {
            case "admin":
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                break;
            case "user":
            default:
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                break;
        }
        
        return authorities;
    }
}