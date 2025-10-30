package cc.infoq.system.service;

import cc.infoq.common.domain.model.LoginUser;
import cc.infoq.common.enums.LoginType;
import cc.infoq.system.domain.vo.SysUserVo;

import java.util.function.Supplier;

/**
 * 登录校验方法
 *
 * @author Lion Li
 */
public interface SysLoginService {

    /**
     * 退出登录
     */
    void logout();

    /**
     * 记录登录信息
     *
     * @param tenantId 租户ID
     * @param username 用户名
     * @param status   状态
     * @param message  消息内容
     */
    void recordLoginInfo(String tenantId, String username, String status, String message);

    /**
     * 构建登录用户
     */
    LoginUser buildLoginUser(SysUserVo user);

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    void recordLoginInfo(Long userId, String ip);

    /**
     * 登录校验
     */
    void checkLogin(LoginType loginType, String tenantId, String username, Supplier<Boolean> supplier);

    /**
     * 校验租户
     *
     * @param tenantId 租户ID
     */
    void checkTenant(String tenantId);

}
