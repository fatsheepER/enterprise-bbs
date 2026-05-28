package com.feiyang.bbs.controller;

import java.util.List;

import com.feiyang.bbs.common.CurrentUser;
import com.feiyang.bbs.common.Result;
import com.feiyang.bbs.dto.PasswordUpdateDTO;
import com.feiyang.bbs.dto.UserLoginDTO;
import com.feiyang.bbs.dto.UserProfileUpdateDTO;
import com.feiyang.bbs.dto.UserRegisterDTO;
import com.feiyang.bbs.service.UserService;
import com.feiyang.bbs.vo.LoginVO;
import com.feiyang.bbs.vo.UserReplyListItemVO;
import com.feiyang.bbs.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody UserRegisterDTO dto) {
        return Result.success(userService.register(dto));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @GetMapping("/profile")
    public Result<UserVO> profile(HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireLogin(request);
        return Result.success(userService.getProfile(currentUser.getId()));
    }

    @GetMapping("/profile/{id}")
    public Result<UserVO> profileById(@PathVariable Long id) {
        return Result.success(userService.getProfile(id));
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody UserProfileUpdateDTO dto,
                                        HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireLogin(request);
        return Result.success(userService.updateProfile(currentUser.getId(), dto));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<UserVO> uploadAvatar(@RequestPart("file") MultipartFile file,
                                       HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireLogin(request);
        return Result.success(userService.uploadAvatar(currentUser.getId(), file));
    }

    @PutMapping("/password")
    public Result<Boolean> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto,
                                          HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireLogin(request);
        return Result.success(userService.updatePassword(currentUser.getId(), dto));
    }

    @GetMapping("/replies")
    public Result<List<UserReplyListItemVO>> replies(HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireLogin(request);
        return Result.success(userService.getReplies(currentUser.getId()));
    }

    @GetMapping("/{id}/replies")
    public Result<List<UserReplyListItemVO>> repliesByUserId(@PathVariable Long id) {
        return Result.success(userService.getReplies(id));
    }
}
