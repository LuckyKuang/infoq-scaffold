package cc.infoq.system.service.impl;

import cc.infoq.common.constant.SystemConstants;
import cc.infoq.common.exception.ServiceException;
import cc.infoq.common.mybatis.core.page.PageQuery;
import cc.infoq.common.mybatis.core.page.TableDataInfo;
import cc.infoq.common.utils.MapstructUtils;
import cc.infoq.common.utils.StringUtils;
import cc.infoq.system.domain.bo.SysTenantPackageBo;
import cc.infoq.system.domain.entity.SysTenant;
import cc.infoq.system.domain.entity.SysTenantPackage;
import cc.infoq.system.domain.vo.SysTenantPackageVo;
import cc.infoq.system.mapper.SysTenantMapper;
import cc.infoq.system.mapper.SysTenantPackageMapper;
import cc.infoq.system.service.SysTenantPackageService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 租户套餐Service业务层处理
 *
 * @author Michelle.Chung
 */
@AllArgsConstructor
@Service
public class SysTenantPackageServiceImpl implements SysTenantPackageService {

    private final SysTenantPackageMapper sysTenantPackageMapper;
    private final SysTenantMapper sysTenantMapper;

    /**
     * 查询租户套餐
     */
    @Override
    public SysTenantPackageVo queryById(Long packageId){
        return sysTenantPackageMapper.selectVoById(packageId);
    }

    /**
     * 查询租户套餐列表
     */
    @Override
    public TableDataInfo<SysTenantPackageVo> queryPageList(SysTenantPackageBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysTenantPackage> lqw = buildQueryWrapper(bo);
        Page<SysTenantPackageVo> result = sysTenantPackageMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<SysTenantPackageVo> selectList() {
        return sysTenantPackageMapper.selectVoList(new LambdaQueryWrapper<SysTenantPackage>()
                .eq(SysTenantPackage::getStatus, SystemConstants.NORMAL));
    }

    /**
     * 查询租户套餐列表
     */
    @Override
    public List<SysTenantPackageVo> queryList(SysTenantPackageBo bo) {
        LambdaQueryWrapper<SysTenantPackage> lqw = buildQueryWrapper(bo);
        return sysTenantPackageMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SysTenantPackage> buildQueryWrapper(SysTenantPackageBo bo) {
        LambdaQueryWrapper<SysTenantPackage> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getPackageName()), SysTenantPackage::getPackageName, bo.getPackageName());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SysTenantPackage::getStatus, bo.getStatus());
        lqw.orderByAsc(SysTenantPackage::getPackageId);
        return lqw;
    }

    /**
     * 新增租户套餐
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(SysTenantPackageBo bo) {
        SysTenantPackage add = MapstructUtils.convert(bo, SysTenantPackage.class);
        // 保存菜单id
        List<Long> menuIds = Arrays.asList(bo.getMenuIds());
        add.setMenuIds(CollUtil.isNotEmpty(menuIds) ? StringUtils.joinComma(menuIds) : "");
        boolean flag = sysTenantPackageMapper.insert(add) > 0;
        if (flag) {
            bo.setPackageId(add.getPackageId());
        }
        return flag;
    }

    /**
     * 修改租户套餐
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(SysTenantPackageBo bo) {
        SysTenantPackage update = MapstructUtils.convert(bo, SysTenantPackage.class);
        // 保存菜单id
        List<Long> menuIds = Arrays.asList(bo.getMenuIds());
        update.setMenuIds(CollUtil.isNotEmpty(menuIds) ? StringUtils.joinComma(menuIds) : "");
        return sysTenantPackageMapper.updateById(update) > 0;
    }

    /**
     * 校验套餐名称是否唯一
     */
    @Override
    public boolean checkPackageNameUnique(SysTenantPackageBo bo) {
        boolean exist = sysTenantPackageMapper.exists(new LambdaQueryWrapper<SysTenantPackage>()
            .eq(SysTenantPackage::getPackageName, bo.getPackageName())
            .ne(ObjectUtil.isNotNull(bo.getPackageId()), SysTenantPackage::getPackageId, bo.getPackageId()));
        return !exist;
    }

    /**
     * 修改套餐状态
     *
     * @param bo 套餐信息
     * @return 结果
     */
    @Override
    public int updatePackageStatus(SysTenantPackageBo bo) {
        SysTenantPackage tenantPackage = MapstructUtils.convert(bo, SysTenantPackage.class);
        return sysTenantPackageMapper.updateById(tenantPackage);
    }

    /**
     * 批量删除租户套餐
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            boolean exists = sysTenantMapper.exists(new LambdaQueryWrapper<SysTenant>().in(SysTenant::getPackageId, ids));
            if (exists) {
                throw new ServiceException("租户套餐已被使用");
            }
        }
        return sysTenantPackageMapper.deleteByIds(ids) > 0;
    }
}
