package com.feiyang.bbs.vo;

import lombok.Data;

@Data
public class AdminDashboardStatsVO {
    private Integer userCount;
    private Integer boardCount;
    private Integer disabledBoardCount;
    private Integer postCount;
    private Integer hiddenPostCount;
    private Integer replyCount;
    private Integer hiddenReplyCount;
}
