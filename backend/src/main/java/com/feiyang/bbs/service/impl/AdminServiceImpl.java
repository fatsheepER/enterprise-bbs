package com.feiyang.bbs.service.impl;

import java.util.List;

import com.feiyang.bbs.common.BusinessException;
import com.feiyang.bbs.common.CurrentUser;
import com.feiyang.bbs.common.ErrorCode;
import com.feiyang.bbs.dto.BoardSaveDTO;
import com.feiyang.bbs.entity.Board;
import com.feiyang.bbs.entity.Post;
import com.feiyang.bbs.entity.Reply;
import com.feiyang.bbs.entity.User;
import com.feiyang.bbs.mapper.AdminMapper;
import com.feiyang.bbs.mapper.BoardMapper;
import com.feiyang.bbs.mapper.PostMapper;
import com.feiyang.bbs.mapper.ReplyMapper;
import com.feiyang.bbs.mapper.UserMapper;
import com.feiyang.bbs.service.AdminService;
import com.feiyang.bbs.vo.AdminDashboardStatsVO;
import com.feiyang.bbs.vo.BoardVO;
import com.feiyang.bbs.vo.PostDetailVO;
import com.feiyang.bbs.vo.PostListItemVO;
import com.feiyang.bbs.vo.ReplyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final AdminMapper adminMapper;
    private final BoardMapper boardMapper;
    private final PostMapper postMapper;
    private final ReplyMapper replyMapper;
    private final UserMapper userMapper;

    @Override
    public AdminDashboardStatsVO getDashboardStats(Long currentUserId) {
        ensureAdmin(currentUserId);
        return adminMapper.selectDashboardStats();
    }

    @Override
    public List<BoardVO> listBoards(Long currentUserId, Long id, String keyword, Integer status) {
        ensureAdmin(currentUserId);
        return boardMapper.selectAdminBoards(id, trimToNull(keyword), normalizeStatusOrNull(status));
    }

    @Override
    @Transactional
    public BoardVO createBoard(Long currentUserId, BoardSaveDTO dto) {
        ensureAdmin(currentUserId);
        String name = dto.getName().trim();
        if (boardMapper.countByName(name) > 0) {
            throw new BusinessException(ErrorCode.BOARD_NAME_EXISTS);
        }

        Board board = Board.builder()
                .name(name)
                .description(trimToEmpty(dto.getDescription()))
                .colorHex(defaultColor(dto.getColorHex()))
                .sortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder())
                .status(normalizeStatus(dto.getStatus()))
                .build();
        boardMapper.insert(board);

        return requireBoardVO(board.getId());
    }

    @Override
    @Transactional
    public BoardVO updateBoard(Long currentUserId, Long id, BoardSaveDTO dto) {
        ensureAdmin(currentUserId);
        requireBoard(id);
        String name = dto.getName().trim();
        if (boardMapper.countByNameExcludeId(name, id) > 0) {
            throw new BusinessException(ErrorCode.BOARD_NAME_EXISTS);
        }

        Board board = Board.builder()
                .id(id)
                .name(name)
                .description(trimToEmpty(dto.getDescription()))
                .colorHex(defaultColor(dto.getColorHex()))
                .sortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder())
                .status(normalizeStatus(dto.getStatus()))
                .build();
        boardMapper.update(board);

        return requireBoardVO(id);
    }

    @Override
    @Transactional
    public Boolean disableBoard(Long currentUserId, Long id) {
        ensureAdmin(currentUserId);
        requireBoard(id);
        boardMapper.updateStatus(id, 0);
        return true;
    }

    @Override
    public List<PostListItemVO> listPosts(Long currentUserId, Long id, Long boardId, String keyword, Integer status) {
        ensureAdmin(currentUserId);
        return postMapper.selectAdminPosts(id, boardId, trimToNull(keyword), normalizeStatusOrNull(status));
    }

    @Override
    @Transactional
    public PostDetailVO updatePostStatus(Long currentUserId, Long id, Integer status) {
        ensureAdmin(currentUserId);
        requirePost(id);
        postMapper.updateStatus(id, normalizeStatus(status));
        return requirePostDetail(id);
    }

    @Override
    @Transactional
    public Boolean hidePost(Long currentUserId, Long id) {
        ensureAdmin(currentUserId);
        requirePost(id);
        postMapper.updateStatus(id, 0);
        return true;
    }

    @Override
    public List<ReplyVO> listReplies(Long currentUserId, Long id, Long postId, String keyword, Integer status) {
        ensureAdmin(currentUserId);
        return replyMapper.selectAdminReplies(id, postId, trimToNull(keyword), normalizeStatusOrNull(status));
    }

    @Override
    @Transactional
    public ReplyVO updateReplyStatus(Long currentUserId, Long id, Integer status) {
        ensureAdmin(currentUserId);
        Reply reply = requireReply(id);
        replyMapper.updateStatus(id, normalizeStatus(status));
        postMapper.touchUpdatedAt(reply.getPostId());
        return requireReplyVO(id);
    }

    @Override
    @Transactional
    public Boolean hideReply(Long currentUserId, Long id) {
        ensureAdmin(currentUserId);
        Reply reply = requireReply(id);
        replyMapper.updateStatus(id, 0);
        postMapper.touchUpdatedAt(reply.getPostId());
        return true;
    }

    private void ensureAdmin(Long currentUserId) {
        User user = userMapper.findById(currentUserId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (!CurrentUser.ROLE_ADMIN.equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private Board requireBoard(Long id) {
        Board board = boardMapper.findById(id);
        if (board == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return board;
    }

    private BoardVO requireBoardVO(Long id) {
        BoardVO board = boardMapper.selectBoardVOById(id);
        if (board == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return board;
    }

    private Post requirePost(Long id) {
        Post post = postMapper.findById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return post;
    }

    private PostDetailVO requirePostDetail(Long id) {
        PostDetailVO post = postMapper.selectPostDetailById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return post;
    }

    private Reply requireReply(Long id) {
        Reply reply = replyMapper.findById(id);
        if (reply == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return reply;
    }

    private ReplyVO requireReplyVO(Long id) {
        ReplyVO reply = replyMapper.selectReplyVOById(id);
        if (reply == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return reply;
    }

    private Integer normalizeStatus(Integer status) {
        if (status == null) {
            return 1;
        }
        if (status == 0 || status == 1) {
            return status;
        }
        throw new BusinessException(ErrorCode.PARAM_ERROR, "状态只能为 0 或 1");
    }

    private Integer normalizeStatusOrNull(Integer status) {
        return status == null ? null : normalizeStatus(status);
    }

    private String defaultColor(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? "#007aff" : trimmed;
    }

    private String trimToEmpty(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? "" : trimmed;
    }

    private String trimToNull(String value) {
        String trimmed = value == null ? null : value.trim();
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }
}
