package com.carwash.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carwash.entity.PaymentAudit;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付审计日志 Mapper
 */
@Mapper
public interface PaymentAuditMapper extends BaseMapper<PaymentAudit> {
    // 直接使用 BaseMapper 的 insert/select 等方法
}