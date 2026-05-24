package com.feiyang.bbs.mapper;

import java.util.List;

import com.feiyang.bbs.entity.Post;
import com.feiyang.bbs.vo.PostDetailVO;
import com.feiyang.bbs.vo.PostListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostMapper {
    int insert(Post post);

    Post findById(@Param("id") Long id);

    PostDetailVO selectPostDetailById(@Param("id") Long id);

    PostDetailVO selectVisiblePostDetailById(@Param("id") Long id);

    List<PostListItemVO> selectVisiblePosts(@Param("boardId") Long boardId,
                                            @Param("userId") Long userId,
                                            @Param("keyword") String keyword,
                                            @Param("sort") String sort);

    List<PostListItemVO> selectAdminPosts(@Param("id") Long id,
                                          @Param("boardId") Long boardId,
                                          @Param("keyword") String keyword,
                                          @Param("status") Integer status);

    int incrementViewCount(@Param("id") Long id);

    int touchUpdatedAt(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
