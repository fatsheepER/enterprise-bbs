package com.feiyang.bbs.common;

import jakarta.servlet.http.HttpServletRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUser {
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLE_HEADER = "X-User-Role";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    private Long id;
    private String role;

    public static CurrentUser optional(HttpServletRequest request) {
        String userId = request.getHeader(USER_ID_HEADER);
        String role = request.getHeader(USER_ROLE_HEADER);
        if (isBlank(userId) || isBlank(role)) {
            return null;
        }
        try {
            return new CurrentUser(Long.valueOf(userId), role.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static CurrentUser requireLogin(HttpServletRequest request) {
        CurrentUser currentUser = optional(request);
        if (currentUser == null || currentUser.getId() == null || !currentUser.hasKnownRole()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return currentUser;
    }

    public static CurrentUser requireAdmin(HttpServletRequest request) {
        CurrentUser currentUser = requireLogin(request);
        if (!currentUser.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return currentUser;
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }

    public boolean isUser() {
        return ROLE_USER.equals(role);
    }

    public boolean hasKnownRole() {
        return isUser() || isAdmin();
    }

    public void requireSelf(Long targetUserId) {
        if (targetUserId == null || !targetUserId.equals(id)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
