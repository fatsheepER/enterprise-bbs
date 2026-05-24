package com.feiyang.bbs.dto;

import lombok.Data;

@Data
public class UserProfileUpdateDTO {
    private String nickname;
    private String avatar;
    private String email;
    private String bio;
}
