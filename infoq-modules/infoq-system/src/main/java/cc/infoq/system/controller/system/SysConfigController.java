package cc.infoq.system.controller.system;

import cc.infoq.common.domain.ApiResult;
import cc.infoq.common.excel.utils.ExcelUtil;
import cc.infoq.common.log.annotation.Log;
import cc.infoq.common.log.enums.BusinessType;
import cc.infoq.common.mybatis.core.page.PageQuery;
import cc.infoq.common.mybatis.core.page.TableDataInfo;
import cc.infoq.common.redis.annotation.RepeatSubmit;
import cc.infoq.common.web.core.BaseController;
import cc.infoq.system.domain.bo.SysConfigBo;
import cc.infoq.system.domain.vo.SysConfigVo;
import cc.infoq.system.service.SysConfigService;
import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 参数配置 信息操作处理
 *
 * @author Lion Li
 */
@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/system/config")
public class SysConfigController extends BaseController {

    private final SysConfigService sysConfigService;

    /**
     * 获取参数配置列表
     */
    @SaCheckPermission("system:config:list")
    @GetMapping("/list")
    public TableDataInfo<SysConfigVo> list(SysConfigBo config, PageQuery pageQuery) {
        return sysConfigService.selectPageConfigList(config, pageQuery);
    }

    /**
     * 导出参数配置列表
     */
    @Log(title = "参数管理", businessType = BusinessType.EXPORT)
    @SaCheckPermission("system:config:export")
    @PostMapping("/export")
    public void export(SysConfigBo config, HttpServletResponse response) {
        List<SysConfigVo> list = sysConfigService.selectConfigList(config);
        ExcelUtil.exportExcel(list, "参数数据", SysConfigVo.class, response);
    }

    /**
     * 根据参数编号获取详细信息
     *
     * @param configId 参数ID
     */
    @SaCheckPermission("system:config:query")
    @GetMapping(value = "/{configId}")
    public ApiResult<SysConfigVo> getInfo(@PathVariable Long configId) {
        return ApiResult.ok(sysConfigService.selectConfigById(configId));
    }

    /**
     * 根据参数键名查询参数值
     *
     * @param configKey 参数Key
     */
    @GetMapping(value = "/configKey/{configKey}")
    public ApiResult<String> getConfigKey(@PathVariable String configKey) {
        return ApiResult.ok("操作成功", sysConfigService.selectConfigByKey(configKey));
    }

    /**
     * 新增参数配置
     */
    @SaCheckPermission("system:config:add")
    @Log(title = "参数管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public ApiResult<Void> add(@Validated @RequestBody SysConfigBo config) {
        if (!sysConfigService.checkConfigKeyUnique(config)) {
            return ApiResult.fail("新增参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        sysConfigService.insertConfig(config);
        return ApiResult.ok();
    }

    /**
     * 修改参数配置
     */
    @SaCheckPermission("system:config:edit")
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public ApiResult<Void> edit(@Validated @RequestBody SysConfigBo config) {
        if (!sysConfigService.checkConfigKeyUnique(config)) {
            return ApiResult.fail("修改参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        sysConfigService.updateConfig(config);
        return ApiResult.ok();
    }

    /**
     * 根据参数键名修改参数配置
     */
    @SaCheckPermission("system:config:edit")
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateByKey")
    public ApiResult<Void> updateByKey(@RequestBody SysConfigBo config) {
        sysConfigService.updateConfig(config);
        return ApiResult.ok();
    }

    /**
     * 删除参数配置
     *
     * @param configIds 参数ID串
     */
    @SaCheckPermission("system:config:remove")
    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public ApiResult<Void> remove(@PathVariable Long[] configIds) {
        sysConfigService.deleteConfigByIds(Arrays.asList(configIds));
        return ApiResult.ok();
    }

    /**
     * 刷新参数缓存
     */
    @SaCheckPermission("system:config:remove")
    @Log(title = "参数管理", businessType = BusinessType.CLEAN)
    @DeleteMapping("/refreshCache")
    public ApiResult<Void> refreshCache() {
        sysConfigService.resetConfigCache();
        return ApiResult.ok();
    }
}
