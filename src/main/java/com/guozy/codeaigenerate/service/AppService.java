package com.guozy.codeaigenerate.service;

import com.guozy.codeaigenerate.model.dto.app.AppQueryRequest;
import com.guozy.codeaigenerate.model.dto.app.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.guozy.codeaigenerate.model.entity.App;

import java.util.List;

/**
 *  服务层。
 *
 * @author <a href="https://github.com/gzy-st">程序员郭志勇</a>
 */
public interface AppService extends IService<App> {

    /**
     * 获取脱敏的应用信息
     *
     * @param app 应用实体
     * @return 脱敏后的应用信息
     */
    AppVO getAppVO(App app);

    /**
     * 获取脱敏的应用列表
     *
     * @param appList 应用实体列表
     * @return 脱敏后的应用列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 获取查询条件
     *
     * @param appQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);
}
