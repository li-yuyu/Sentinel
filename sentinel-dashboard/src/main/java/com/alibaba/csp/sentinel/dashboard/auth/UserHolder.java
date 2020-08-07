package com.alibaba.csp.sentinel.dashboard.auth;

import com.alibaba.csp.sentinel.dashboard.auth.AuthService.AuthUser;

/**
 * @author Lyle
 * @since 2020年8月7日 下午5:56:27 
 */
public class UserHolder {

    private static final ThreadLocal<AuthUser> userHolder = new ThreadLocal<AuthUser>();

    public static void set(AuthUser sysUser) {
        userHolder.set(sysUser);
    }

    public static AuthUser getCurrentUser() {
        return userHolder.get();
    }

    public static void remove() {
        userHolder.remove();
    }
}

