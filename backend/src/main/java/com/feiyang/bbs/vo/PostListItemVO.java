package com.feiyang.bbs.vo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class PostListItemVO {
    private Long id;
    private Long boardId;
    private String boardName;
    private String boardColorHex;
    private Integer boardStatus;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private String authorRole;
    private String title;
    private String contentPreview;
    private Integer status;
    private Integer viewCount;
    private Integer replyCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
