package com.carwash.controller;

import com.carwash.common.result.Result;
import com.carwash.dto.FeedbackPublicDTO;
import com.carwash.entity.Feedback;
import com.carwash.entity.User;
import com.carwash.mapper.FeedbackMapper;
import com.carwash.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/public/feedback")
public class PublicFeedbackController {
    @Autowired
    private FeedbackMapper feedbackMapper;
    @Autowired
    private UserMapper userMapper;

    @GetMapping("/latest")
    public Result<List<FeedbackPublicDTO>> latest(@RequestParam(value = "limit", required = false, defaultValue = "6") Integer limit) {
        List<Feedback> list = feedbackMapper.selectLatest(limit);
        List<FeedbackPublicDTO> out = new ArrayList<>();
        for (Feedback f : list) {
            User u = f.getUserId() != null ? userMapper.selectById(f.getUserId()) : null;
            String name = u != null && u.getUsername() != null ? mask(u.getUsername()) : "匿名用户";
            FeedbackPublicDTO dto = new FeedbackPublicDTO();
            dto.setId(f.getId());
            dto.setRating(f.getRating());
            dto.setContent(f.getContent());
            dto.setCreatedAt(f.getCreatedAt());
            dto.setUserMaskedName(name);
            out.add(dto);
        }
        return Result.success(out);
    }

    private String mask(String s) {
        if (s == null || s.length() <= 2) return "***";
        int len = s.length();
        return s.substring(0, 1) + "***" + s.substring(len - 1);
    }
}
