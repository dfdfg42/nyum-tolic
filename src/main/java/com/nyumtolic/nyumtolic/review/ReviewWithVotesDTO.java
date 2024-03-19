package com.nyumtolic.nyumtolic.review;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewWithVotesDTO {

    private Review review;
    private Long votesCount;

    public ReviewWithVotesDTO(Review review, Long votesCount) {
        this.review = review;
        this.votesCount = votesCount;
    }

}
