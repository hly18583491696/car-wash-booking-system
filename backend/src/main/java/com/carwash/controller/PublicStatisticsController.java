package com.carwash.controller;

import com.carwash.common.result.Result;
import com.carwash.dto.StatsSummary;
import com.carwash.mapper.UserMapper;
import com.carwash.mapper.BookingMapper;
import com.carwash.mapper.FeedbackMapper;
import com.carwash.mapper.PaymentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/stats")
public class PublicStatisticsController {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private BookingMapper bookingMapper;
    @Autowired
    private FeedbackMapper feedbackMapper;
    @Autowired
    private PaymentMapper paymentMapper;

    @GetMapping("/summary")
    public Result<StatsSummary> summary() {
        int users = userMapper.countTotalUsers();
        int completed = bookingMapper.countCompletedBookings();
        Double avg = feedbackMapper.avgRating();
        int satisfaction = (int) Math.round(((avg != null ? avg : 0.0) / 5.0) * 100.0);
        Integer avgMinutes = paymentMapper.avgPaymentResponseMinutes();
        StatsSummary s = new StatsSummary();
        s.setUsersCount(users);
        s.setCompletedBookings(completed);
        s.setSatisfactionPercent(satisfaction);
        s.setAvgPaymentResponseMinutes(avgMinutes != null ? avgMinutes : 0);
        return Result.success(s);
    }
}
