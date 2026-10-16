package com.guozy.codeaigenerate.innerservice;

import com.guozy.codeaigenerate.exception.BusinessException;
import com.guozy.codeaigenerate.exception.ErrorCode;
import com.guozy.codeaigenerate.model.entity.User;
import com.guozy.codeaigenerate.model.dto.user.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static com.guozy.codeaigenerate.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 内部使用的用户服务（Dubbo 接口）
 */
public interface InnerUserService {

    /**
     * 根据 id 列表批量查询用户
     */
    List<User> listByIds(Collection<? extends Serializable> ids);

    /**
     * 根据 id 查询用户
     */
    User getById(Serializable id);

    /**
     * 获取用户脱敏信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取当前登录用户（静态方法，直接读共享 Redis Session，不走 RPC）
     */
    static User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }
}