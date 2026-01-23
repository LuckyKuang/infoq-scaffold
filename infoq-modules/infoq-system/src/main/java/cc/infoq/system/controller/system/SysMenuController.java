package cc.infoq.system.controller.system;

import cc.infoq.common.constant.SystemConstants;
import cc.infoq.common.constant.TenantConstants;
import cc.infoq.common.domain.ApiResult;
import cc.infoq.common.log.annotation.Log;
import cc.infoq.common.log.enums.BusinessType;
import cc.infoq.common.redis.annotation.RepeatSubmit;
import cc.infoq.common.satoken.utils.LoginHelper;
import cc.infoq.common.utils.StringUtils;
import cc.infoq.common.web.core.BaseController;
import cc.infoq.system.domain.bo.SysMenuBo;
import cc.infoq.system.domain.entity.SysMenu;
import cc.infoq.system.domain.vo.RouterVo;
import cc.infoq.system.domain.vo.SysMenuVo;
import cc.infoq.system.service.SysMenuService;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.lang.tree.Tree;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单信息
 *
 * @author Lion Li
 */
@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/system/menu")
public class SysMenuController extends BaseController {

    private final SysMenuService sysMenuService;

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("/getRouters")
    public ApiResult<List<RouterVo>> getRouters() {
        List<SysMenu> menus = sysMenuService.selectMenuTreeByUserId(LoginHelper.getUserId());
        return ApiResult.ok(sysMenuService.buildMenus(menus));
    }

    /**
     * 获取菜单列表
     */
    @SaCheckRole(value = {
        TenantConstants.SUPER_ADMIN_ROLE_KEY,
        TenantConstants.TENANT_ADMIN_ROLE_KEY
    }, mode = SaMode.OR)
    @SaCheckPermission("system:menu:list")
    @GetMapping("/list")
    public ApiResult<List<SysMenuVo>> list(SysMenuBo menu) {
        List<SysMenuVo> menus = sysMenuService.selectMenuList(menu, LoginHelper.getUserId());
        return ApiResult.ok(menus);
    }

    /**
     * 根据菜单编号获取详细信息
     *
     * @param menuId 菜单ID
     */
    @SaCheckRole(value = {
        TenantConstants.SUPER_ADMIN_ROLE_KEY,
        TenantConstants.TENANT_ADMIN_ROLE_KEY
    }, mode = SaMode.OR)
    @SaCheckPermission("system:menu:query")
    @GetMapping(value = "/{menuId}")
    public ApiResult<SysMenuVo> getInfo(@PathVariable Long menuId) {
        return ApiResult.ok(sysMenuService.selectMenuById(menuId));
    }

    /**
     * 获取菜单下拉树列表
     */
    @SaCheckPermission("system:menu:query")
    @GetMapping("/treeselect")
    public ApiResult<List<Tree<Long>>> treeselect(SysMenuBo menu) {
        List<SysMenuVo> menus = sysMenuService.selectMenuList(menu, LoginHelper.getUserId());
        return ApiResult.ok(sysMenuService.buildMenuTreeSelect(menus));
    }

    /**
     * 加载对应角色菜单列表树
     *
     * @param roleId 角色ID
     */
    @SaCheckPermission("system:menu:query")
    @GetMapping(value = "/roleMenuTreeselect/{roleId}")
    public ApiResult<MenuTreeSelectVo> roleMenuTreeselect(@PathVariable("roleId") Long roleId) {
        List<SysMenuVo> menus = sysMenuService.selectMenuList(LoginHelper.getUserId());
        MenuTreeSelectVo selectVo = new MenuTreeSelectVo(
            sysMenuService.selectMenuListByRoleId(roleId),
            sysMenuService.buildMenuTreeSelect(menus));
        return ApiResult.ok(selectVo);
    }

    /**
     * 加载对应租户套餐菜单列表树
     *
     * @param packageId 租户套餐ID
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:menu:query")
    @GetMapping(value = "/tenantPackageMenuTreeselect/{packageId}")
    public ApiResult<MenuTreeSelectVo> tenantPackageMenuTreeselect(@PathVariable("packageId") Long packageId) {
        List<SysMenuVo> menus = sysMenuService.selectMenuList(LoginHelper.getUserId());
        List<Tree<Long>> list = sysMenuService.buildMenuTreeSelect(menus);
        // 删除租户管理菜单
        list.removeIf(menu -> menu.getId() == 6L);
        List<Long> ids = new ArrayList<>();
        if (packageId > 0L) {
            ids = sysMenuService.selectMenuListByPackageId(packageId);
        }
        MenuTreeSelectVo selectVo = new MenuTreeSelectVo(ids, list);
        return ApiResult.ok(selectVo);
    }

    /**
     * 新增菜单
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:menu:add")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public ApiResult<Void> add(@Validated @RequestBody SysMenuBo menu) {
        if (!sysMenuService.checkMenuNameUnique(menu)) {
            return ApiResult.fail("新增菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        } else if (SystemConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath())) {
            return ApiResult.fail("新增菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        } else if (!sysMenuService.checkRouteConfigUnique(menu)) {
            return ApiResult.fail("新增菜单'" + menu.getMenuName() + "'失败，路由名称或地址已存在");
        }
        return toAjax(sysMenuService.insertMenu(menu));
    }

    /**
     * 修改菜单
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:menu:edit")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public ApiResult<Void> edit(@Validated @RequestBody SysMenuBo menu) {
        if (!sysMenuService.checkMenuNameUnique(menu)) {
            return ApiResult.fail("修改菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        } else if (SystemConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath())) {
            return ApiResult.fail("修改菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        } else if (menu.getMenuId().equals(menu.getParentId())) {
            return ApiResult.fail("修改菜单'" + menu.getMenuName() + "'失败，上级菜单不能选择自己");
        } else if (!sysMenuService.checkRouteConfigUnique(menu)) {
            return ApiResult.fail("修改菜单'" + menu.getMenuName() + "'失败，路由名称或地址已存在");
        }
        return toAjax(sysMenuService.updateMenu(menu));
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:menu:remove")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public ApiResult<Void> remove(@PathVariable("menuId") Long menuId) {
        if (sysMenuService.hasChildByMenuId(menuId)) {
            return ApiResult.warn("存在子菜单,不允许删除");
        }
        if (sysMenuService.checkMenuExistRole(menuId)) {
            return ApiResult.warn("菜单已分配,不允许删除");
        }
        return toAjax(sysMenuService.deleteMenuById(menuId));
    }

    /**
     * 角色菜单列表树信息
     *
     * @param checkedKeys 选中菜单列表
     * @param menus       菜单下拉树结构列表
     */
    public record MenuTreeSelectVo(List<Long> checkedKeys, List<Tree<Long>> menus) {
    }

    /**
     * 批量级联删除菜单
     *
     * @param menuIds 菜单ID串
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:menu:remove")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/cascade/{menuIds}")
    public ApiResult<Void> remove(@PathVariable("menuIds") Long[] menuIds) {
        List<Long> menuIdList = List.of(menuIds);
        if (sysMenuService.hasChildByMenuId(menuIdList)) {
            return ApiResult.warn("存在子菜单,不允许删除");
        }
        sysMenuService.deleteMenuById(menuIdList);
        return ApiResult.ok();
    }

}
