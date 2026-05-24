package com.feiyang.bbs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostCreateDTO {
    @NotNull(message = "版块 ID 不能为空")
    private Long boardId;

    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 100, message = "帖子标题长度不能超过 100")
    private String title;

    @NotBlank(message = "帖子正文不能为空")
    @Size(max = 5000, message = "帖子正文长度不能超过 5000")
    private String content;
}
