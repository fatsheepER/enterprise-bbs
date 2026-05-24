package com.feiyang.bbs.controller;

import com.feiyang.bbs.common.CurrentUser;
import com.feiyang.bbs.common.Result;
import com.feiyang.bbs.service.ReplyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/replies")
@RequiredArgsConstructor
public class ReplyController {
    private final ReplyService replyService;

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id, HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireLogin(request);
        return Result.success(replyService.deleteOwnReply(currentUser.getId(), id));
    }
}
