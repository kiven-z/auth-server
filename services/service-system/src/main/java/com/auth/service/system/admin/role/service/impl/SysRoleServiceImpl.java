package com.auth.service.system.admin.role.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.feign.AuthenticationInnerFeignClient;
import com.auth.service.system.admin.role.mapper.SysRoleMapper;
import com.auth.service.system.admin.role.model.entity.SysRoleEntity;
import com.auth.service.system.admin.role.model.enums.RoleTypeEnum;
import com.auth.service.system.admin.role.model.form.SysRoleCreateForm;
import com.auth.service.system.admin.role.model.form.SysRoleUpdateForm;
import com.auth.service.system.admin.role.model.query.SysRoleQuery;
import com.auth.service.system.admin.role.model.vo.SysRoleDetailVO;
import com.auth.service.system.admin.role.model.vo.SysRoleOptionVO;
import com.auth.service.system.admin.role.model.vo.SysRolePageResponse;
import com.auth.service.system.admin.role.service.SysRoleService;
import com.auth.service.system.admin.role.support.SysRolePageOrderSqlBuilder;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.SystemResultCodeEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 系统角色服务实现
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleService {

    private final AuthenticationInnerFeignClient authenticationInnerFeignClient;

    public SysRoleServiceImpl(AuthenticationInnerFeignClient authenticationInnerFeignClient) {
        this.authenticationInnerFeignClient = authenticationInnerFeignClient;
    }

    /**
     * 分页查询角色
     *
     * @param pageParams 分页
     * @param query      条件
     * @return 分页结果
     */
    @Override
    public PageResponse<SysRolePageResponse> getPage(Page<SysRoleEntity> pageParams, SysRoleQuery query) {
        String orderBySql = SysRolePageOrderSqlBuilder.buildOrderBySql(query.getSort());
        IPage<SysRolePageResponse> page = baseMapper.selectListByPage(pageParams, query, orderBySql);
        return PageResponse.of(page);
    }

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    @Override
    public SysRoleDetailVO getDetail(Long id) {
        SysRoleEntity entity = baseMapper.selectById(id);
        if (entity == null) {
            throw new SystemBusinessException(SystemResultCodeEnum.ROLE_NOT_FOUND, id);
        }
        SysRoleDetailVO vo = new SysRoleDetailVO();
        BeanUtil.copyProperties(entity, vo);
        vo.setRoleTypeDesc(RoleTypeEnum.getDesc(entity.getRoleType()));
        return vo;
    }

    /**
     * 新增角色
     *
     * @param form 新增表单
     */
    @Override
    public void create(SysRoleCreateForm form) {
        SysRoleEntity entity = new SysRoleEntity();
        BeanUtil.copyProperties(form, entity);
        save(entity);
    }

    /**
     * 更新角色
     *
     * @param form 修改表单
     */
    @Override
    public void update(SysRoleUpdateForm form) {
        SysRoleEntity oldEntity = baseMapper.selectById(form.getId());
        if (oldEntity == null) {
            throw new SystemBusinessException(SystemResultCodeEnum.ROLE_NOT_FOUND, form.getId());
        }

        // 系统角色不可修改 role_type
        if (RoleTypeEnum.SYSTEM.getCode().equals(oldEntity.getRoleType())
                && !Objects.equals(oldEntity.getRoleType(), form.getRoleType())) {
            throw new SystemBusinessException(SystemResultCodeEnum.ROLE_SYSTEM_CANNOT_CHANGE_TYPE);
        }

        String oldRoleCode = oldEntity.getRoleCode();
        String newRoleCode = form.getRoleCode();
        boolean roleCodeChanged = !Objects.equals(oldRoleCode, newRoleCode);

        // 获取受影响用户 ID（改 code 的话新旧都涉及）
        List<Long> affectedUserIds = new ArrayList<>();
        if (roleCodeChanged) {
            affectedUserIds.addAll(refreshUsersByRoleCode(oldRoleCode));
        }

        // 执行更新
        SysRoleEntity updateEntity = new SysRoleEntity();
        BeanUtil.copyProperties(form, updateEntity);
        updateById(updateEntity);

        // 如果 role_code 变了，级联更新关联表
        if (roleCodeChanged) {
            baseMapper.cascadeUpdateRoleCode(oldRoleCode, newRoleCode);
            // 新 code 的用户也需要刷新
            affectedUserIds.addAll(refreshUsersByRoleCode(newRoleCode));
        }

        // 刷新缓存（不改 code 时也要刷新，因为角色名称等变化影响 AuthProfile）
        if (!roleCodeChanged) {
            affectedUserIds.addAll(refreshUsersByRoleCode(oldRoleCode));
        }

        // 去重后，先更新 perm_version，再刷新缓存
        if (CollUtil.isNotEmpty(affectedUserIds)) {
            List<Long> distinctIds = affectedUserIds.stream().distinct().collect(Collectors.toList());
            updatePermVersion(distinctIds);
            authenticationInnerFeignClient.refreshUserCache(distinctIds);
        }
    }

    /**
     * 批量删除角色
     *
     * @param ids 角色ID列表
     */
    @Override
    public void deleteBatch(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }

        List<SysRoleEntity> entities = baseMapper.selectBatchIds(ids);
        if (entities.size() != ids.size()) {
            // 有角色不存在
            for (Long id : ids) {
                boolean exists = entities.stream().anyMatch(e -> e.getId().equals(id));
                if (!exists) {
                    throw new SystemBusinessException(SystemResultCodeEnum.ROLE_NOT_FOUND, id);
                }
            }
        }

        List<String> affectedRoleCodes = new ArrayList<>();
        for (SysRoleEntity entity : entities) {
            // 系统角色不可删除
            if (RoleTypeEnum.SYSTEM.getCode().equals(entity.getRoleType())) {
                throw new SystemBusinessException(SystemResultCodeEnum.ROLE_SYSTEM_CANNOT_DELETE);
            }

            Integer refCount = baseMapper.countGrantTableRefByRoleCode(entity.getRoleCode());
            if (refCount != null && refCount > 0) {
                throw new SystemBusinessException(SystemResultCodeEnum.ROLE_IN_USE, refCount);
            }

            affectedRoleCodes.add(entity.getRoleCode());
        }

        // 批量逻辑删除
        baseMapper.logicDeleteBatchByIds(ids);

        // 刷新缓存
        for (String roleCode : affectedRoleCodes) {
            refreshAndFlushCache(roleCode);
        }
    }

    /**
     * 切换角色状态
     *
     * @param id     角色ID
     * @param status 目标状态
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        SysRoleEntity entity = baseMapper.selectById(id);
        if (entity == null) {
            throw new SystemBusinessException(SystemResultCodeEnum.ROLE_NOT_FOUND, id);
        }

        // 系统角色不可禁用
        if (status == 1 && RoleTypeEnum.SYSTEM.getCode().equals(entity.getRoleType())) {
            throw new SystemBusinessException(SystemResultCodeEnum.ROLE_SYSTEM_CANNOT_DISABLE);
        }

        baseMapper.updateStatusById(id, status);

        // 刷新缓存
        refreshAndFlushCache(entity.getRoleCode());
    }

    /**
     * 查询角色下拉选项
     *
     * @param roleName 角色名称（可选模糊）
     * @param roleCode 角色编码（可选模糊）
     * @return 下拉选项列表
     */
    @Override
    public List<SysRoleOptionVO> listOptions(String roleName, String roleCode) {
        QueryWrapper<SysRoleEntity> wrapper = new QueryWrapper<>();
        wrapper.select("id", "role_code", "role_name")
                .eq("is_deleted", 0);

        if (roleName != null && !roleName.isEmpty()) {
            wrapper.like("role_name", roleName);
        }
        if (roleCode != null && !roleCode.isEmpty()) {
            wrapper.like("role_code", roleCode);
        }

        wrapper.orderByAsc("order_num");
        wrapper.last("LIMIT 100");

        List<SysRoleEntity> entities = baseMapper.selectList(wrapper);
        return entities.stream().map(e -> new SysRoleOptionVO(e.getId(), e.getRoleCode(), e.getRoleName()))
                .collect(Collectors.toList());
    }

    /**
     * 查询持有指定角色的所有用户 ID，先更新 perm_version，再刷新缓存
     *
     * @param roleCode 角色编码
     */
    private void refreshAndFlushCache(String roleCode) {
        List<Long> userIds = refreshUsersByRoleCode(roleCode);
        if (CollUtil.isNotEmpty(userIds)) {
            // 先自增 perm_version，使 JWT 版本比对失效
            updatePermVersion(userIds);
            authenticationInnerFeignClient.refreshUserCache(userIds);
        }
    }

    /**
     * 批量更新用户权限版本号（perm_version + 1）
     *
     * @param userIds 用户 ID 列表
     */
    private void updatePermVersion(List<Long> userIds) {
        baseMapper.batchIncrementPermVersion(userIds);
    }

    /**
     * 内部辅助：查询持有该角色的所有用户 ID
     *
     * @param roleCode 角色编码
     * @return 用户 ID 列表
     */
    private List<Long> refreshUsersByRoleCode(String roleCode) {
        try {
            Result<List<Long>> result = authenticationInnerFeignClient.listUserIdsByRoleCode(roleCode);
            if (result != null && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch user ids for roleCode={}: {}", roleCode, e.getMessage());
        }
        return ListUtil.empty();
    }

}
