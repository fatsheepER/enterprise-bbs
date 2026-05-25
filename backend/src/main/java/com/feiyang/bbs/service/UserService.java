package com.feiyang.bbs.service;

import java.util.List;

import com.feiyang.bbs.dto.PasswordUpdateDTO;
import com.feiyang.bbs.dto.UserLoginDTO;
import com.feiyang.bbs.dto.UserProfileUpdateDTO;
import com.feiyang.bbs.dto.UserRegisterDTO;
import com.feiyang.bbs.vo.LoginVO;
import com.feiyang.bbs.vo.UserReplyListItemVO;
import com.feiyang.bbs.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserVO register(UserRegisterDTO dto);

    LoginVO login(UserLoginDTO dto);

    UserVO getProfile(Long userId);

    UserVO updateProfile(Long userId, UserProfileUpdateDTO dto);

    UserVO uploadAvatar(Long userId, MultipartFile file);

    Boolean updatePassword(Long userId, PasswordUpdateDTO dto);

    List<UserReplyListItemVO> getReplies(Long userId);
}
