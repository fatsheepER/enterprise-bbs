package com.feiyang.bbs.vo;

import lombok.Data;

@Data
public class ReplyReferenceVO {
    private String type;
    private Long id;
    private String authorName;
    private String contentPreview;
    private String href;
}
