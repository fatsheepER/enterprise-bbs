package com.feiyang.bbs.service.impl;

import java.util.List;

import com.feiyang.bbs.common.BusinessException;
import com.feiyang.bbs.common.ErrorCode;
import com.feiyang.bbs.dto.ReplyCreateDTO;
import com.feiyang.bbs.entity.Board;
import com.feiyang.bbs.entity.Post;
import com.feiyang.bbs.entity.Reply;
import com.feiyang.bbs.mapper.BoardMapper;
import com.feiyang.bbs.mapper.PostMapper;
import com.feiyang.bbs.mapper.ReplyMapper;
import com.feiyang.bbs.mapper.UserMapper;
import com.feiyang.bbs.service.ReplyService;
import com.feiyang.bbs.vo.ReplyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReplyServiceImpl implements ReplyService {
    private static final int STATUS_ENABLED = 1;

    private final ReplyMapper replyMapper;
    private final PostMapper postMapper;
    private final BoardMapper boardMapper;
    private final UserMapper userMapper;

    @Override
    public List<ReplyVO> listVisibleReplies(Long postId) {
        ensurePostVisible(postId);
        return replyMapper.selectVisibleRepliesByPostId(postId);
    }

    @Override
    @Transactional
    public ReplyVO createReply(Long currentUserId, Long postId, ReplyCreateDTO dto) {
        ensureUserExists(currentUserId);
        Post post = ensurePostVisible(postId);

        Long parentReplyId = dto.getParentReplyId();
        if (parentReplyId != null) {
            Reply parentReply = requireReply(parentReplyId);
            if (!postId.equals(parentReply.getPostId())
                    || !Integer.valueOf(STATUS_ENABLED).equals(parentReply.getStatus())) {
                throw new BusinessException(ErrorCode.RESOURCE_UNAVAILABLE);
            }
        }

        Reply reply = Reply.builder()
                .postId(post.getId())
                .userId(currentUserId)
                .parentReplyId(parentReplyId)
                .content(dto.getContent().trim())
                .status(STATUS_ENABLED)
                .build();
        replyMapper.insert(reply);
        postMapper.touchUpdatedAt(postId);

        return requireReplyVO(reply.getId());
    }

    @Override
    @Transactional
    public Boolean deleteOwnReply(Long currentUserId, Long replyId) {
        ensureUserExists(currentUserId);
        Reply reply = requireReply(replyId);
        if (!currentUserId.equals(reply.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        replyMapper.updateStatus(replyId, 0);
        postMapper.touchUpdatedAt(reply.getPostId());
        return true;
    }

    private Post ensurePostVisible(Long postId) {
        Post post = postMapper.findById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        Board board = boardMapper.findById(post.getBoardId());
        if (board == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (!Integer.valueOf(STATUS_ENABLED).equals(post.getStatus())
                || !Integer.valueOf(STATUS_ENABLED).equals(board.getStatus())) {
            throw new BusinessException(ErrorCode.RESOURCE_UNAVAILABLE);
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

    private void ensureUserExists(Long userId) {
        if (userMapper.findById(userId) == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
