package com.feiyang.bbs.vo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class BoardVO {
    private Long id;
    private String name;
    private String description;
    private String colorHex;
    private Integer sortOrder;
    private Integer status;
    private Integer postCount;
    private Integer replyCount;
    private LatestPostVO latestPost;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
