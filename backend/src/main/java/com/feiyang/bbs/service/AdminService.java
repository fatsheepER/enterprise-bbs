package com.feiyang.bbs.service;

import java.util.List;

import com.feiyang.bbs.dto.BoardSaveDTO;
import com.feiyang.bbs.vo.AdminDashboardStatsVO;
import com.feiyang.bbs.vo.BoardVO;
import com.feiyang.bbs.vo.PostDetailVO;
import com.feiyang.bbs.vo.PostListItemVO;
import com.feiyang.bbs.vo.ReplyVO;

public interface AdminService {
    AdminDashboardStatsVO getDashboardStats(Long currentUserId);

    List<BoardVO> listBoards(Long currentUserId, Long id, String keyword, Integer status);

    BoardVO createBoard(Long currentUserId, BoardSaveDTO dto);

    BoardVO updateBoard(Long currentUserId, Long id, BoardSaveDTO dto);

    Boolean disableBoard(Long currentUserId, Long id);

    List<PostListItemVO> listPosts(Long currentUserId, Long id, Long boardId, String keyword, Integer status);

    PostDetailVO updatePostStatus(Long currentUserId, Long id, Integer status);

    Boolean hidePost(Long currentUserId, Long id);

    List<ReplyVO> listReplies(Long currentUserId, Long id, Long postId, String keyword, Integer status);

    ReplyVO updateReplyStatus(Long currentUserId, Long id, Integer status);

    Boolean hideReply(Long currentUserId, Long id);
}
