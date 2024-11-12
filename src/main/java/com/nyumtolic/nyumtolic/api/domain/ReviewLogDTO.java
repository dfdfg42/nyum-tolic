package com.nyumtolic.nyumtolic.api.domain;

import com.nyumtolic.nyumtolic.domain.ReviewLog;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewLogDTO {
    private Long id;
    private LocalDateTime createdAt;
    private Double rating;
    private String content;
    private Long restaurantId;
    private AuthorDTO author;

    public ReviewLogDTO(ReviewLog reviewLog) {
        this.id = reviewLog.getId();
        this.createdAt = reviewLog.getCreatedAt();
        this.rating = reviewLog.getRating();
        this.content = reviewLog.getContent();
        this.restaurantId = reviewLog.getRestaurantId();
        this.author = new AuthorDTO(reviewLog.getAuthor());
    }
}