package com.nyumtolic.nyumtolic.review;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewForm {

    @NotEmpty(message = "내용은 필수항목입니다")
    @Size(min = 1, max = 500, message = "내용은 1자 이상 500자 이하로 작성해야 합니다")
    private String content;
}
