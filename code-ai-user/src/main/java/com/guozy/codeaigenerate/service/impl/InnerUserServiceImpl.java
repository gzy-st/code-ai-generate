package com.guozy.codeaigenerate.service.impl;

import com.guozy.codeaigenerate.innerservice.InnerUserService;
import com.guozy.codeaigenerate.model.entity.User;
import com.guozy.codeaigenerate.model.dto.user.vo.UserVO;
import com.guozy.codeaigenerate.service.UserService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 内部用户服务 Dubbo 实现
 */
@DubboService
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private UserService userService;

    @Override
    public List<User> listByIds(Collection<? extends Serializable> ids) {
        return userService.listByIds(ids);
    }

    @Override
    public User getById(Serializable id) {
        return userService.getById(id);
    }

    @Override
    public UserVO getUserVO(User user) {
        return userService.getUserVO(user);
    }
}