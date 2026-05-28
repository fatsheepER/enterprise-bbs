package com.feiyang.bbs.mapper;

import java.util.List;

import com.feiyang.bbs.entity.Reply;
import com.feiyang.bbs.vo.ReplyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReplyMapper {
    int insert(Reply reply);

    Reply findById(@Param("id") Long id);

    ReplyVO selectReplyVOById(@Param("id") Long id);

    List<ReplyVO> selectVisibleRepliesByPostId(@Param("postId") Long postId);

    List<ReplyVO> selectAdminReplies(@Param("id") Long id,
                                     @Param("postId") Long postId,
                                     @Param("keyword") String keyword,
                                     @Param("status") Integer status);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(@Param("id") Long id);

    int deleteByPostId(@Param("postId") Long postId);

    int deleteByBoardId(@Param("boardId") Long boardId);
}
