package com.nyumtolic.nyumtolic.security.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateForm {

    @Size(min = 1, max = 20)
    @NotEmpty(message = "별명은 필수항목입니다")
    private String nickname;

}
