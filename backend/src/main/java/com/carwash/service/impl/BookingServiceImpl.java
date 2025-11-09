package com.carwash.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.carwash.common.BusinessException;
import com.carwash.common.result.ResultCode;
import com.carwash.dto.BookingRequest;
import com.carwash.dto.BookingResponse;
import com.carwash.entity.Booking;
import com.carwash.entity.Feedback;
import com.carwash.entity.Payment;
import com.carwash.entity.Service;
import com.carwash.entity.TimeSlot;
import com.carwash.mapper.BookingMapper;
import com.carwash.mapper.FeedbackMapper;
import com.carwash.mapper.PaymentMapper;
import com.carwash.mapper.ServiceMapper;
import com.carwash.mapper.TimeSlotMapper;
import com.carwash.mapper.UserMapper;
import com.carwash.service.BookingService;
import com.carwash.service.WebSocketService;
import com.carwash.service.NotificationService;
import com.carwash.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.carwash.utils.TimeUtils;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 预约订单服务实现类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@org.springframework.stereotype.Service
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private ServiceMapper serviceMapper;

    @Autowired
    private TimeSlotMapper timeSlotMapper;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBooking(BookingRequest request) {
        log.info("创建预约订单开始，请求参数: {}", request);

        try {
            // 验证必要参数
            if (request.getUserId() == null) {
                log.error("用户ID为空");
                throw new BusinessException(ResultCode.PARAM_ERROR, "用户ID不能为空");
            }
            if (request.getServiceId() == null) {
                log.error("服务ID为空");
                throw new BusinessException(ResultCode.PARAM_ERROR, "服务ID不能为空");
            }
            
            // 验证服务是否存在
            Service service = serviceMapper.selectById(request.getServiceId());
            if (service == null) {
                log.error("服务不存在，服务ID: {}", request.getServiceId());
                throw new BusinessException(ResultCode.SERVICE_NOT_FOUND, "服务不存在");
            }
            log.info("找到服务: {}", service.getName());

            // 验证时间段是否可用（如果提供了时间段ID）
            TimeSlot timeSlot = null;
            if (request.getTimeSlotId() != null) {
                timeSlot = timeSlotMapper.selectById(request.getTimeSlotId());
                if (timeSlot == null) {
                    log.warn("时间段不存在，时间段ID: {}，将使用默认设置", request.getTimeSlotId());
                } else {
                    // 检查时间段状态
                    if (timeSlot.getStatus() == 0) {
                        throw new BusinessException(ResultCode.APPOINTMENT_CONFLICT, "该时间段不可用");
                    }

                    // 检查时间段是否已被预约
                    int bookedCount = bookingMapper.countByTimeSlot(request.getTimeSlotId());
                    if (bookedCount >= timeSlot.getMaxBookings()) {
                        throw new BusinessException(ResultCode.APPOINTMENT_CONFLICT, "该时间段已被预约满");
                    }
                }
            } else {
                log.info("未指定时间段ID，将使用默认时间设置");
            }

            // 创建订单
            Booking booking = new Booking();
            BeanUtils.copyProperties(request, booking);
            booking.setOrderNo(generateOrderNo());
            booking.setTotalPrice(service.getPrice());
            booking.setStatus("pending");
            booking.setPaymentStatus("unpaid");
            
            // 设置时间段ID和预约时间
            if (timeSlot != null) {
                booking.setTimeSlotId(request.getTimeSlotId());
                booking.setBookingTime(timeSlot.getStartTime() + "-" + timeSlot.getEndTime());
            } else {
                // 如果没有时间段，设置默认值
                booking.setTimeSlotId(1L); // 设置默认时间段ID
                booking.setBookingTime("09:00-10:00");
            }
            
            // 设置预约日期 - 如果没有提供，使用当前日期
            if (booking.getBookingDate() == null) {
                booking.setBookingDate(LocalDate.now());
            }
            
            // 设置时间字段
            booking.setCreatedAt(TimeUtils.now());
            booking.setUpdatedAt(TimeUtils.now());
            booking.setDeleted(0);

            log.info("准备插入订单数据: {}", booking);
            
            int result = bookingMapper.insert(booking);
            log.info("数据库插入结果: {}, 生成的订单ID: {}", result, booking.getId());
            
            if (result <= 0) {
                log.error("数据库插入失败，result: {}", result);
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "创建预约订单失败，数据库插入失败");
            }

            if (booking.getId() == null) {
                log.error("订单ID为空，插入可能失败");
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "创建预约订单失败，未获取到订单ID");
            }

            log.info("预约订单创建成功，订单ID: {}, 订单号: {}", booking.getId(), booking.getOrderNo());
            return booking.getId();
            
        } catch (BusinessException e) {
            log.error("业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("创建预约订单异常", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "创建预约订单失败: " + e.getMessage());
        }
    }

    @Override
    public List<BookingResponse> getUserBookings(Long userId) {
        log.info("获取用户订单列表，用户ID: {}", userId);

        QueryWrapper<Booking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("deleted", 0)
                   .orderByDesc("created_at");

        List<Booking> bookings = bookingMapper.selectList(queryWrapper);

        return bookings.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public BookingResponse getBookingById(Long bookingId) {
        log.info("获取订单详情，订单ID: {}", bookingId);

        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null || booking.getDeleted() == 1) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }

        return convertToResponse(booking);
    }

    @Override
    public BookingResponse getBookingByOrderNo(String orderNo) {
        log.info("根据订单号获取订单详情，订单号: {}", orderNo);

        QueryWrapper<Booking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo)
                   .eq("deleted", 0);

        Booking booking = bookingMapper.selectOne(queryWrapper);
        if (booking == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }

        return convertToResponse(booking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long bookingId, String reason) {
        log.info("取消订单，订单ID: {}, 原因: {}", bookingId, reason);

        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null || booking.getDeleted() == 1) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }

        // 检查订单状态是否可以取消
        if (!"pending".equals(booking.getStatus()) && !"confirmed".equals(booking.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_CANNOT_CANCEL, "当前订单状态无法取消");
        }

        // 更新订单状态
        booking.setStatus("cancelled");
        booking.setCancelReason(reason);
        booking.setCancelledAt(TimeUtils.now());
        booking.setUpdatedAt(TimeUtils.now());

        int result = bookingMapper.updateById(booking);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "取消订单失败");
        }

        log.info("订单取消成功，订单ID: {}", bookingId);

        // 推送订单取消状态到WebSocket客户端
        try {
            webSocketService.pushOrderStatusUpdate(booking.getUserId(), bookingId, booking.getOrderNo(), "pending", "cancelled", "用户取消订单");
            log.info("订单取消WebSocket推送成功，用户ID: {}, 订单ID: {}", booking.getUserId(), bookingId);
        } catch (Exception e) {
            log.error("订单取消WebSocket推送失败，用户ID: {}, 订单ID: {}, 错误: {}", 
                booking.getUserId(), bookingId, e.getMessage(), e);
            // WebSocket推送失败不影响主业务流程
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBookingStatus(Long bookingId, String status) {
        log.info("更新订单状态，订单ID: {}, 状态: {}", bookingId, status);

        // 验证订单存在
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null || booking.getDeleted() == 1) {
            log.error("订单不存在或已删除，订单ID: {}", bookingId);
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }

        String oldStatus = booking.getStatus();
        log.info("订单当前状态: {}, 目标状态: {}", oldStatus, status);

        // 验证状态转换是否合法
        if (!isValidStatusTransition(oldStatus, status)) {
            log.warn("无效的状态转换: {} -> {}", oldStatus, status);
            throw new BusinessException(ResultCode.INVALID_STATUS_TRANSITION, 
                String.format("无效的状态转换: %s -> %s", oldStatus, status));
        }

        // 更新订单状态
        booking.setStatus(status);
        booking.setUpdatedAt(TimeUtils.now());

        // 根据状态设置相应的时间戳
        switch (status) {
            case "confirmed":
                // 设置确认时间
                log.info("设置订单确认时间");
                break;
            case "in_progress":
                // 设置开始服务时间
                log.info("设置服务开始时间");
                break;
            case "completed":
                booking.setCompletedAt(TimeUtils.now());
                log.info("设置订单完成时间");
                break;
            case "cancelled":
                booking.setCancelledAt(TimeUtils.now());
                log.info("设置订单取消时间");
                break;
        }

        // 执行数据库更新
        log.info("执行数据库更新操作");
        int result = bookingMapper.updateById(booking);
        log.info("数据库更新结果: {}", result);
        
        if (result <= 0) {
            log.error("数据库更新失败，影响行数: {}", result);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "更新订单状态失败，数据库操作失败");
        }

        // 强制刷新并验证更新结果
        log.info("验证数据库更新结果");
        Booking updatedBooking = bookingMapper.selectById(bookingId);
        
        if (updatedBooking == null) {
            log.error("验证失败：更新后无法查询到订单，订单ID: {}", bookingId);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "数据库状态验证失败：订单不存在");
        }
        
        if (!status.equals(updatedBooking.getStatus())) {
            log.error("数据库状态同步失败，订单ID: {}, 期望状态: {}, 实际状态: {}", 
                bookingId, status, updatedBooking.getStatus());
            
            // 尝试重新更新一次
            log.info("尝试重新更新订单状态");
            updatedBooking.setStatus(status);
            updatedBooking.setUpdatedAt(TimeUtils.now());
            int retryResult = bookingMapper.updateById(updatedBooking);
            
            if (retryResult <= 0) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "重试更新订单状态失败");
            }
            
            // 再次验证
            Booking finalBooking = bookingMapper.selectById(bookingId);
            if (!status.equals(finalBooking.getStatus())) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, 
                    String.format("数据库状态同步失败，期望: %s, 实际: %s", status, finalBooking.getStatus()));
            }
        }

        log.info("订单状态更新成功，订单ID: {}, 状态变更: {} -> {}, 更新时间: {}", 
            bookingId, oldStatus, status, updatedBooking.getUpdatedAt());

        // 推送订单状态更新到WebSocket客户端
        try {
            webSocketService.pushOrderStatusUpdate(updatedBooking.getUserId(), bookingId, updatedBooking.getOrderNo(), oldStatus, status, "订单状态更新");
            log.info("WebSocket推送成功，用户ID: {}, 订单ID: {}, 状态: {}", 
                updatedBooking.getUserId(), bookingId, status);
        } catch (Exception e) {
            log.error("WebSocket推送失败，用户ID: {}, 订单ID: {}, 错误: {}", 
                updatedBooking.getUserId(), bookingId, e.getMessage(), e);
            // WebSocket推送失败不影响主业务流程
        }

        // 发送短信通知
        try {
            // 获取用户信息
            com.carwash.entity.User user = userMapper.selectById(updatedBooking.getUserId());
            if (user != null && user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
                String customerName = user.getRealName() != null ? user.getRealName() : user.getUsername();
                
                // 发送订单状态变更短信通知
                boolean smsResult = smsService.sendOrderStatusNotification(
                    user.getPhone(), 
                    updatedBooking.getOrderNo(), 
                    oldStatus, 
                    status, 
                    customerName
                );
                
                if (smsResult) {
                    log.info("短信通知发送成功，用户ID: {}, 手机号: {}, 订单号: {}, 状态变更: {} -> {}", 
                        updatedBooking.getUserId(), user.getPhone(), updatedBooking.getOrderNo(), oldStatus, status);
                } else {
                    log.warn("短信通知发送失败，用户ID: {}, 手机号: {}, 订单号: {}", 
                        updatedBooking.getUserId(), user.getPhone(), updatedBooking.getOrderNo());
                }
            } else {
                log.warn("用户手机号为空，跳过短信通知，用户ID: {}", updatedBooking.getUserId());
            }
        } catch (Exception e) {
            log.error("短信通知发送异常，用户ID: {}, 订单ID: {}, 错误: {}", 
                updatedBooking.getUserId(), bookingId, e.getMessage(), e);
            // 短信发送失败不影响主业务流程
        }
    }

    /**
     * 验证状态转换是否合法
     */
    private boolean isValidStatusTransition(String fromStatus, String toStatus) {
        // 定义合法的状态转换规则
        switch (fromStatus) {
            case "pending":
                return "confirmed".equals(toStatus) || "cancelled".equals(toStatus);
            case "confirmed":
                return "in_progress".equals(toStatus) || "cancelled".equals(toStatus);
            case "in_progress":
                return "completed".equals(toStatus) || "cancelled".equals(toStatus);
            case "completed":
                return false; // 已完成的订单不能再转换状态
            case "cancelled":
                return false; // 已取消的订单不能再转换状态
            default:
                return true; // 未知状态允许转换（兼容性考虑）
        }
    }

    @Override
    public List<BookingResponse> getAllBookings() {
        log.info("获取所有订单列表");

        QueryWrapper<Booking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                   .orderByDesc("created_at");

        List<Booking> bookings = bookingMapper.selectList(queryWrapper);

        return bookings.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBooking(Long bookingId) {
        log.info("删除订单（硬删除），订单ID: {}", bookingId);

        // 验证订单ID
        if (bookingId == null) {
            log.error("订单ID为空");
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单ID不能为空");
        }

        // 检查订单是否存在
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            log.error("订单不存在，订单ID: {}", bookingId);
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }

        String orderNo = booking.getOrderNo();
        log.info("订单删除前状态 - 订单ID: {}, 订单号: {}, deleted: {}, 状态: {}", 
            bookingId, orderNo, booking.getDeleted(), booking.getStatus());

        // 级联删除关联的支付记录
        try {
            QueryWrapper<Payment> paymentWrapper = new QueryWrapper<>();
            paymentWrapper.eq("booking_id", bookingId);
            List<Payment> payments = paymentMapper.selectList(paymentWrapper);
            if (payments != null && !payments.isEmpty()) {
                log.info("删除订单关联的支付记录，数量: {}", payments.size());
                for (Payment payment : payments) {
                    paymentMapper.deleteById(payment.getId());
                    log.info("已删除支付记录，支付ID: {}", payment.getId());
                }
            }
        } catch (Exception e) {
            log.error("删除关联支付记录失败", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "删除关联支付记录失败");
        }

        // 级联删除关联的反馈记录
        try {
            List<Feedback> feedbacks = feedbackMapper.selectByBookingId(bookingId);
            if (feedbacks != null && !feedbacks.isEmpty()) {
                log.info("删除订单关联的反馈记录，数量: {}", feedbacks.size());
                for (Feedback feedback : feedbacks) {
                    feedbackMapper.deleteById(feedback.getId());
                    log.info("已删除反馈记录，反馈ID: {}", feedback.getId());
                }
            }
        } catch (Exception e) {
            log.error("删除关联反馈记录失败", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "删除关联反馈记录失败");
        }

        // 执行硬删除，从数据库中完全移除记录
        log.info("执行数据库删除操作");
        int result = bookingMapper.deleteById(bookingId);
        log.info("数据库删除结果: {}", result);
        
        if (result <= 0) {
            log.error("硬删除订单失败，数据库操作失败，订单ID: {}, 影响行数: {}", bookingId, result);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "删除订单失败，数据库操作失败");
        }

        // 验证删除结果，确认记录不再存在
        log.info("验证数据库删除结果");
        Booking deletedBooking = bookingMapper.selectById(bookingId);
        
        if (deletedBooking != null) {
            log.error("验证失败：删除后订单仍然存在，订单ID: {}", bookingId);
            
            // 尝试重新删除一次
            log.info("尝试重新删除订单");
            int retryResult = bookingMapper.deleteById(bookingId);
            
            if (retryResult <= 0) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "重试硬删除订单失败");
            }
            
            // 再次验证
            Booking finalBooking = bookingMapper.selectById(bookingId);
            if (finalBooking != null) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, 
                    "数据库删除验证失败，记录仍然存在");
            }
        }

        log.info("订单删除成功，订单ID: {}, 订单号: {}, 记录已从数据库中完全移除", bookingId, orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void permanentlyDeleteBooking(Long bookingId) {
        log.info("永久删除订单（硬删除），订单ID: {}", bookingId);

        // 验证订单ID
        if (bookingId == null) {
            log.error("订单ID为空");
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单ID不能为空");
        }

        // 检查订单是否存在（包括已软删除的订单）
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            log.error("订单不存在，订单ID: {}", bookingId);
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }

        String orderNo = booking.getOrderNo();
        log.info("订单硬删除前状态 - 订单ID: {}, 订单号: {}, deleted: {}, 状态: {}", 
            bookingId, orderNo, booking.getDeleted(), booking.getStatus());

        // 执行硬删除，从数据库中完全移除记录
        log.info("执行数据库删除操作");
        int result = bookingMapper.deleteById(bookingId);
        log.info("数据库删除结果: {}", result);
        
        if (result <= 0) {
            log.error("硬删除订单失败，数据库操作失败，订单ID: {}, 影响行数: {}", bookingId, result);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "硬删除订单失败，数据库操作失败");
        }

        // 验证删除结果，确认记录不再存在
        log.info("验证数据库删除结果");
        Booking deletedBooking = bookingMapper.selectById(bookingId);
        
        if (deletedBooking != null) {
            log.error("验证失败：删除后订单仍然存在，订单ID: {}", bookingId);
            
            // 尝试重新删除一次
            log.info("尝试重新删除订单");
            int retryResult = bookingMapper.deleteById(bookingId);
            
            if (retryResult <= 0) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "重试硬删除订单失败");
            }
            
            // 再次验证
            Booking finalBooking = bookingMapper.selectById(bookingId);
            if (finalBooking != null) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, 
                    "数据库删除验证失败，记录仍然存在");
            }
        }

        log.info("订单硬删除成功，订单ID: {}, 订单号: {}, 记录已从数据库中完全移除", bookingId, orderNo);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "CW" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 转换为响应对象
     */
    private BookingResponse convertToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        BeanUtils.copyProperties(booking, response);
        
        // 填充服务名称
        if (booking.getServiceId() != null) {
            Service service = serviceMapper.selectById(booking.getServiceId());
            if (service != null) {
                response.setServiceName(service.getName());
            }
        }
        
        return response;
    }
}