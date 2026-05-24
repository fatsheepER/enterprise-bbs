package com.feiyang.bbs.controller;

import java.util.List;

import com.feiyang.bbs.common.CurrentUser;
import com.feiyang.bbs.common.Result;
import com.feiyang.bbs.dto.PostCreateDTO;
import com.feiyang.bbs.dto.ReplyCreateDTO;
import com.feiyang.bbs.service.PostService;
import com.feiyang.bbs.service.ReplyService;
import com.feiyang.bbs.vo.PostDetailVO;
import com.feiyang.bbs.vo.PostListItemVO;
import com.feiyang.bbs.vo.ReplyVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final ReplyService replyService;

    @GetMapping
    public Result<List<PostListItemVO>> list(@RequestParam(required = false) Long boardId,
                                             @RequestParam(required = false) Long userId,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String sort) {
        return Result.success(postService.listVisiblePosts(boardId, userId, keyword, sort));
    }

    @GetMapping("/{id}")
    public Result<PostDetailVO> detail(@PathVariable Long id) {
        return Result.success(postService.getVisiblePost(id));
    }

    @PostMapping
    public Result<PostDetailVO> create(@Valid @RequestBody PostCreateDTO dto,
                                       HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireLogin(request);
        return Result.success(postService.createPost(currentUser.getId(), dto));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id, HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireLogin(request);
        return Result.success(postService.deleteOwnPost(currentUser.getId(), id));
    }

    @GetMapping("/{postId}/replies")
    public Result<List<ReplyVO>> replies(@PathVariable Long postId) {
        return Result.success(replyService.listVisibleReplies(postId));
    }

    @PostMapping("/{postId}/replies")
    public Result<ReplyVO> createReply(@PathVariable Long postId,
                                       @Valid @RequestBody ReplyCreateDTO dto,
                                       HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireLogin(request);
        return Result.success(replyService.createReply(currentUser.getId(), postId, dto));
    }
}
