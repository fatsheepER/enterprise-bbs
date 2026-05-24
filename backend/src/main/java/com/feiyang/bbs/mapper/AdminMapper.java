package com.feiyang.bbs.mapper;

import com.feiyang.bbs.vo.AdminDashboardStatsVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper {
    AdminDashboardStatsVO selectDashboardStats();
}
