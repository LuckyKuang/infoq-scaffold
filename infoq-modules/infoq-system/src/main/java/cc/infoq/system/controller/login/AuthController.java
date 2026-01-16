package cc.infoq.system.controller.login;

import cc.infoq.common.constant.SystemConstants;
import cc.infoq.common.domain.ApiResult;
import cc.infoq.common.domain.model.LoginBody;
import cc.infoq.common.domain.model.RegisterBody;
import cc.infoq.common.encrypt.annotation.ApiEncrypt;
import cc.infoq.common.json.utils.JsonUtils;
import cc.infoq.common.redis.annotation.RateLimiter;
import cc.infoq.common.redis.enums.LimitType;
import cc.infoq.common.satoken.utils.LoginHelper;
import cc.infoq.common.sse.dto.SseMessageDto;
import cc.infoq.common.sse.utils.SseMessageUtils;
import cc.infoq.common.tenant.helper.TenantHelper;
import cc.infoq.common.utils.*;
import cc.infoq.system.domain.bo.SysTenantBo;
import cc.infoq.system.domain.vo.*;
import cc.infoq.system.service.*;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.exception.NotLoginException;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 认证
 *
 * @author Lion Li
 */
@Slf4j
@SaIgnore
@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final SysLoginService sysLoginService;
    private final SysRegisterService sysRegisterService;
    private final SysConfigService sysConfigService;
    private final SysTenantService sysTenantService;
    private final SysClientService sysClientService;
    private final ScheduledExecutorService scheduledExecutorService;


    /**
     * 登录方法
     *
     * @param body 登录信息
     * @return 结果
     */
    @ApiEncrypt
    @PostMapping("/login")
    public ApiResult<LoginVo> login(@RequestBody String body) {
        LoginBody loginBody = JsonUtils.parseObject(body, LoginBody.class);
        ValidatorUtils.validate(loginBody);
        // 授权类型和客户端id
        String clientId = loginBody.getClientId();
        String grantType = loginBody.getGrantType();
        SysClientVo client = sysClientService.queryByClientId(clientId);
        // 查询不到 client 或 client 内不包含 grantType
        if (ObjectUtil.isNull(client) || !StringUtils.contains(client.getGrantType(), grantType)) {
            log.info("客户端id: {} 认证类型：{} 异常!.", clientId, grantType);
            return ApiResult.fail(MessageUtils.message("auth.grant.type.error"));
        } else if (!SystemConstants.NORMAL.equals(client.getStatus())) {
            return ApiResult.fail(MessageUtils.message("auth.grant.type.blocked"));
        }
        // 校验租户
        sysLoginService.checkTenant(loginBody.getTenantId());
        // 登录
        LoginVo loginVo = AuthStrategy.login(body, client, grantType);

        Long userId = LoginHelper.getUserId();
        scheduledExecutorService.schedule(() -> {
            SseMessageDto dto = new SseMessageDto();
            dto.setMessage(DateUtils.getTodayHour(new Date()) + "好，欢迎登录 infoq-scaffold 后台管理系统");
            dto.setUserIds(List.of(userId));
            SseMessageUtils.publishMessage(dto);
        }, 5, TimeUnit.SECONDS);
        return ApiResult.ok(loginVo);
    }


    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ApiResult<Void> logout() {
        sysLoginService.logout();
        return ApiResult.ok("退出成功");
    }

    /**
     * 用户注册
     */
    @ApiEncrypt
    @PostMapping("/register")
    public ApiResult<Void> register(@Validated @RequestBody RegisterBody user) {
        if (!sysConfigService.selectRegisterEnabled(user.getTenantId())) {
            return ApiResult.fail("当前系统没有开启注册功能！");
        }
        sysRegisterService.register(user);
        return ApiResult.ok();
    }

    /**
     * 登录页面租户下拉框
     *
     * @return 租户列表
     */
    @RateLimiter(time = 60, count = 20, limitType = LimitType.IP)
    @GetMapping("/tenant/list")
    public ApiResult<LoginTenantVo> tenantList(HttpServletRequest request) throws Exception {
        // 返回对象
        LoginTenantVo result = new LoginTenantVo();
        boolean enable = TenantHelper.isEnable();
        result.setTenantEnabled(enable);
        // 如果未开启租户这直接返回
        if (!enable) {
            return ApiResult.ok(result);
        }

        List<SysTenantVo> tenantList = sysTenantService.queryList(new SysTenantBo());
        List<TenantListVo> voList = MapstructUtils.convert(tenantList, TenantListVo.class);
        try {
            // 如果只超管返回所有租户
            if (LoginHelper.isSuperAdmin()) {
                result.setVoList(voList);
                return ApiResult.ok(result);
            }
        } catch (NotLoginException ignored) {
        }

        // 获取域名
        String host;
        String referer = request.getHeader("referer");
        if (StringUtils.isNotBlank(referer)) {
            // 这里从referer中取值是为了本地使用hosts添加虚拟域名，方便本地环境调试
            host = referer.split("//")[1].split("/")[0];
        } else {
            host = new URL(request.getRequestURL().toString()).getHost();
        }
        // 根据域名进行筛选
        List<TenantListVo> list = StreamUtils.filter(voList, vo ->
            StringUtils.equalsIgnoreCase(vo.getDomain(), host));
        result.setVoList(CollUtil.isNotEmpty(list) ? list : voList);
        return ApiResult.ok(result);
    }

}
