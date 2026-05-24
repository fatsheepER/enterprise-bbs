package com.feiyang.bbs.controller;

import java.util.List;

import com.feiyang.bbs.common.CurrentUser;
import com.feiyang.bbs.common.Result;
import com.feiyang.bbs.dto.BoardSaveDTO;
import com.feiyang.bbs.dto.StatusUpdateDTO;
import com.feiyang.bbs.service.AdminService;
import com.feiyang.bbs.vo.AdminDashboardStatsVO;
import com.feiyang.bbs.vo.BoardVO;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/dashboard/stats")
    public Result<AdminDashboardStatsVO> stats(HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireAdmin(request);
        return Result.success(adminService.getDashboardStats(currentUser.getId()));
    }

    @GetMapping("/boards")
    public Result<List<BoardVO>> boards(@RequestParam(required = false) Long id,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) Integer status,
                                        HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireAdmin(request);
        return Result.success(adminService.listBoards(currentUser.getId(), id, keyword, status));
    }

    @PostMapping("/boards")
    public Result<BoardVO> createBoard(@Valid @RequestBody BoardSaveDTO dto,
                                       HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireAdmin(request);
        return Result.success(adminService.createBoard(currentUser.getId(), dto));
    }

    @PutMapping("/boards/{id}")
    public Result<BoardVO> updateBoard(@PathVariable Long id,
                                       @Valid @RequestBody BoardSaveDTO dto,
                                       HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireAdmin(request);
        return Result.success(adminService.updateBoard(currentUser.getId(), id, dto));
    }

    @DeleteMapping("/boards/{id}")
    public Result<Boolean> deleteBoard(@PathVariable Long id, HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireAdmin(request);
        return Result.success(adminService.disableBoard(currentUser.getId(), id));
    }

    @GetMapping("/posts")
    public Result<List<PostListItemVO>> posts(@RequestParam(required = false) Long id,
                                              @RequestParam(required = false) Long boardId,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Integer status,
                                              HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireAdmin(request);
        return Result.success(adminService.listPosts(currentUser.getId(), id, boardId, keyword, status));
    }

    @PutMapping("/posts/{id}/status")
    public Result<PostDetailVO> updatePostStatus(@PathVariable Long id,
                                                 @Valid @RequestBody StatusUpdateDTO dto,
                                                 HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireAdmin(request);
        return Result.success(adminService.updatePostStatus(currentUser.getId(), id, dto.getStatus()));
    }

    @DeleteMapping("/posts/{id}")
    public Result<Boolean> deletePost(@PathVariable Long id, HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireAdmin(request);
        return Result.success(adminService.hidePost(currentUser.getId(), id));
    }

    @GetMapping("/replies")
    public Result<List<ReplyVO>> replies(@RequestParam(required = false) Long id,
                                         @RequestParam(required = false) Long postId,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Integer status,
                                         HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireAdmin(request);
        return Result.success(adminService.listReplies(currentUser.getId(), id, postId, keyword, status));
    }

    @GetMapping("/posts/{postId}/replies")
    public Result<List<ReplyVO>> postReplies(@PathVariable Long postId,
                                             @RequestParam(required = false) Integer status,
                                             HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireAdmin(request);
        return Result.success(adminService.listReplies(currentUser.getId(), null, postId, null, status));
    }

    @PutMapping("/replies/{id}/status")
    public Result<ReplyVO> updateReplyStatus(@PathVariable Long id,
                                             @Valid @RequestBody StatusUpdateDTO dto,
                                             HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireAdmin(request);
        return Result.success(adminService.updateReplyStatus(currentUser.getId(), id, dto.getStatus()));
    }

    @DeleteMapping("/replies/{id}")
    public Result<Boolean> deleteReply(@PathVariable Long id, HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireAdmin(request);
        return Result.success(adminService.hideReply(currentUser.getId(), id));
    }
}
