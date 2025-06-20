package com.nyumtolic.nyumtolic.post.comment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentForm {

    @NotEmpty(message = "댓글 내용은 필수항목입니다")
    @Size(min = 1, max = 1000, message = "댓글은 1자 이상 1000자 이하로 작성해야 합니다")
    private String content;

    private boolean isAnonymous = false;
}