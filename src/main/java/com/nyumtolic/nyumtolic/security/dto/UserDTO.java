package com.nyumtolic.nyumtolic.security.dto;


import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String loginId;
    private String nickname;
    // 필요한 필드만 포함
}
