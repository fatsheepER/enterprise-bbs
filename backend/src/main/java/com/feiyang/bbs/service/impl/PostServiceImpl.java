package com.feiyang.bbs.service.impl;

import java.util.List;

import com.feiyang.bbs.common.BusinessException;
import com.feiyang.bbs.common.ErrorCode;
import com.feiyang.bbs.dto.PostCreateDTO;
import com.feiyang.bbs.entity.Board;
import com.feiyang.bbs.entity.Post;
import com.feiyang.bbs.mapper.BoardMapper;
import com.feiyang.bbs.mapper.PostMapper;
import com.feiyang.bbs.mapper.UserMapper;
import com.feiyang.bbs.service.PostService;
import com.feiyang.bbs.vo.PostDetailVO;
import com.feiyang.bbs.vo.PostListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private static final int STATUS_ENABLED = 1;

    private final PostMapper postMapper;
    private final BoardMapper boardMapper;
    private final UserMapper userMapper;

    @Override
    public List<PostListItemVO> listVisiblePosts(Long boardId, Long userId, String keyword, String sort) {
        return postMapper.selectVisiblePosts(boardId, userId, trimToNull(keyword), normalizeSort(sort));
    }

    @Override
    @Transactional
    public PostDetailVO getVisiblePost(Long id) {
        PostDetailVO post = postMapper.selectVisiblePostDetailById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        postMapper.incrementViewCount(id);
        return requireVisiblePostDetail(id);
    }

    @Override
    @Transactional
    public PostDetailVO createPost(Long currentUserId, PostCreateDTO dto) {
        ensureUserExists(currentUserId);
        Board board = boardMapper.findById(dto.getBoardId());
        if (board == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (!Integer.valueOf(STATUS_ENABLED).equals(board.getStatus())) {
            throw new BusinessException(ErrorCode.RESOURCE_UNAVAILABLE);
        }

        Post post = Post.builder()
                .boardId(dto.getBoardId())
                .userId(currentUserId)
                .title(dto.getTitle().trim())
                .content(dto.getContent().trim())
                .status(STATUS_ENABLED)
                .viewCount(0)
                .build();
        postMapper.insert(post);

        return requireVisiblePostDetail(post.getId());
    }

    @Override
    @Transactional
    public Boolean deleteOwnPost(Long currentUserId, Long postId) {
        ensureUserExists(currentUserId);
        Post post = requirePost(postId);
        if (!currentUserId.equals(post.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        postMapper.updateStatus(postId, 0);
        return true;
    }

    private Post requirePost(Long id) {
        Post post = postMapper.findById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return post;
    }

    private PostDetailVO requireVisiblePostDetail(Long id) {
        PostDetailVO post = postMapper.selectVisiblePostDetailById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return post;
    }

    private void ensureUserExists(Long userId) {
        if (userMapper.findById(userId) == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private String normalizeSort(String sort) {
        if ("newest".equals(sort) || "views".equals(sort)) {
            return sort;
        }
        return "latest";
    }

    private String trimToNull(String value) {
        String trimmed = value == null ? null : value.trim();
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }
}
