package com.carwash.mapper;

import com.carwash.entity.SystemConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 系统配置数据访问层
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    /**
     * 根据配置键查询配置
     */
    @Select("SELECT * FROM system_config WHERE config_key = #{configKey}")
    SystemConfig selectByConfigKey(@Param("configKey") String configKey);

    /**
     * 查询指定类型的所有配置
     */
    @Select("SELECT * FROM system_config WHERE config_type = #{configType} ORDER BY config_key ASC")
    List<SystemConfig> selectByConfigType(@Param("configType") String configType);

    /**
     * 更新配置值
     */
    @Update("UPDATE system_config SET config_value = #{configValue} WHERE config_key = #{configKey}")
    int updateConfigValue(@Param("configKey") String configKey, @Param("configValue") String configValue);

    /**
     * 查询所有配置
     */
    @Select("SELECT * FROM system_config ORDER BY config_key ASC")
    List<SystemConfig> selectAllConfigs();
}