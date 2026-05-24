package com.feiyang.bbs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReplyCreateDTO {
    private Long parentReplyId;

    @NotBlank(message = "回复内容不能为空")
    @Size(max = 2000, message = "回复内容长度不能超过 2000")
    private String content;
}
