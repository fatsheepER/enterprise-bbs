package com.feiyang.bbs.service.impl;

import java.util.List;

import com.feiyang.bbs.common.BusinessException;
import com.feiyang.bbs.common.CurrentUser;
import com.feiyang.bbs.common.ErrorCode;
import com.feiyang.bbs.dto.PasswordUpdateDTO;
import com.feiyang.bbs.dto.UserLoginDTO;
import com.feiyang.bbs.dto.UserProfileUpdateDTO;
import com.feiyang.bbs.dto.UserRegisterDTO;
import com.feiyang.bbs.entity.User;
import com.feiyang.bbs.mapper.UserMapper;
import com.feiyang.bbs.service.UserService;
import com.feiyang.bbs.vo.LoginVO;
import com.feiyang.bbs.vo.UserReplyListItemVO;
import com.feiyang.bbs.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserVO register(UserRegisterDTO dto) {
        String username = trim(dto.getUsername());
        if (userMapper.countByUsername(username) > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        User user = User.builder()
                .username(username)
                .password(dto.getPassword())
                .nickname(defaultIfBlank(dto.getNickname(), username))
                .avatar("")
                .email(trimToNull(dto.getEmail()))
                .bio(defaultIfBlank(dto.getBio(), ""))
                .role(CurrentUser.ROLE_USER)
                .build();
        userMapper.insert(user);

        return requireUserVO(user.getId());
    }

    @Override
    public LoginVO login(UserLoginDTO dto) {
        User user = userMapper.findByUsername(trim(dto.getUsername()));
        if (user == null || !user.getPassword().equals(dto.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        return new LoginVO(requireUserVO(user.getId()));
    }

    @Override
    public UserVO getProfile(Long userId) {
        return requireUserVO(userId);
    }

    @Override
    @Transactional
    public UserVO updateProfile(Long userId, UserProfileUpdateDTO dto) {
        ensureUserExists(userId);

        User user = User.builder()
                .id(userId)
                .nickname(trimToNull(dto.getNickname()))
                .avatar(trimToNull(dto.getAvatar()))
                .email(trimToNull(dto.getEmail()))
                .bio(trimToNull(dto.getBio()))
                .build();
        userMapper.updateProfile(user);

        return requireUserVO(userId);
    }

    @Override
    @Transactional
    public Boolean updatePassword(Long userId, PasswordUpdateDTO dto) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (!user.getPassword().equals(dto.getOldPassword())) {
            throw new BusinessException(ErrorCode.OLD_PASSWORD_ERROR);
        }

        userMapper.updatePassword(userId, dto.getNewPassword());
        return true;
    }

    @Override
    public List<UserReplyListItemVO> getReplies(Long userId) {
        ensureUserExists(userId);
        return userMapper.selectUserReplies(userId);
    }

    private UserVO requireUserVO(Long userId) {
        UserVO userVO = userMapper.selectUserVOById(userId);
        if (userVO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return userVO;
    }

    private void ensureUserExists(Long userId) {
        if (userMapper.findById(userId) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String trimToNull(String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }
}
