package com.feiyang.bbs.mapper;

import java.util.List;

import com.feiyang.bbs.entity.User;
import com.feiyang.bbs.vo.UserReplyListItemVO;
import com.feiyang.bbs.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    int insert(User user);

    User findById(@Param("id") Long id);

    User findByUsername(@Param("username") String username);

    UserVO selectUserVOById(@Param("id") Long id);

    int countByUsername(@Param("username") String username);

    int updateProfile(User user);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    List<UserReplyListItemVO> selectUserReplies(@Param("userId") Long userId);
}
