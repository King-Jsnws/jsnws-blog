package com.jsnws.server.controller;

import com.jsnws.server.common.ApiException;
import com.jsnws.server.entity.SysUser;
import com.jsnws.server.mapper.SysUserMapper;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 控制器公共支撑：从请求中取当前登录用户
 */
public abstract class BaseController {

    protected static final String USER_ID_ATTR = "userId";

    /** 当前登录用户 id（未登录返回 null） */
    protected Long currentUserId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Object attr = request.getAttribute(USER_ID_ATTR);
        return attr == null ? null : (Long) attr;
    }

    /** 当前登录用户，未登录抛出异常 */
    protected SysUser currentUser(SysUserMapper userMapper) {
        Long userId = currentUserId();
        if (userId == null) {
            throw new ApiException("登录已失效");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new ApiException("登录已失效");
        }
        return user;
    }
}
