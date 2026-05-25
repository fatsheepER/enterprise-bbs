package com.feiyang.bbs.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserServiceImpl implements UserService {
    private static final long MAX_AVATAR_FILE_SIZE = 2L * 1024 * 1024;
    private static final String AVATAR_URL_PREFIX = "/api/uploads/avatars/";

    private final UserMapper userMapper;
    private final Path avatarDirectory;

    public UserServiceImpl(UserMapper userMapper, @Value("${bbs.upload-root}") String uploadRoot) {
        this.userMapper = userMapper;
        this.avatarDirectory = Paths.get(uploadRoot).toAbsolutePath().normalize().resolve("avatars");
    }

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
                .email(trimToNull(dto.getEmail()))
                .bio(trimToNull(dto.getBio()))
                .build();
        userMapper.updateProfile(user);

        return requireUserVO(userId);
    }

    @Override
    @Transactional
    public UserVO uploadAvatar(Long userId, MultipartFile file) {
        ensureUserExists(userId);
        String extension = validateAvatarFile(file);
        String fileName = UUID.randomUUID() + extension;
        Path destination = avatarDirectory.resolve(fileName);

        try {
            Files.createDirectories(avatarDirectory);
            file.transferTo(destination);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "头像上传失败");
        }

        userMapper.updateAvatar(userId, AVATAR_URL_PREFIX + fileName);
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

    private String validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请选择头像文件");
        }
        if (file.getSize() > MAX_AVATAR_FILE_SIZE) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "头像文件不能超过 2MB");
        }

        return switch (file.getContentType() == null ? "" : file.getContentType()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new BusinessException(ErrorCode.PARAM_ERROR, "仅支持 JPG、PNG 或 WebP 图片");
        };
    }
}
