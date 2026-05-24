package com.feiyang.bbs.service;

import java.util.List;

import com.feiyang.bbs.dto.PostCreateDTO;
import com.feiyang.bbs.vo.PostDetailVO;
import com.feiyang.bbs.vo.PostListItemVO;

public interface PostService {
    List<PostListItemVO> listVisiblePosts(Long boardId, Long userId, String keyword, String sort);

    PostDetailVO getVisiblePost(Long id);

    PostDetailVO createPost(Long currentUserId, PostCreateDTO dto);

    Boolean deleteOwnPost(Long currentUserId, Long postId);
}
