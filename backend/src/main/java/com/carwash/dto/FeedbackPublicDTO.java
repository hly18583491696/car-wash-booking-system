package com.carwash.dto;

import java.time.LocalDateTime;

public class FeedbackPublicDTO {
    private Long id;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
    private String userMaskedName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getUserMaskedName() { return userMaskedName; }
    public void setUserMaskedName(String userMaskedName) { this.userMaskedName = userMaskedName; }
}
