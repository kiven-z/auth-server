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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.auth.common.web.autoconfigure.FeignUtil.isOk;

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

        SysRoleDetailVO vo = BeanUtil.copyProperties(entity, SysRoleDetailVO.class);
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
        SysRoleEntity entity = BeanUtil.copyProperties(form, SysRoleEntity.class);
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
        Integer roleType = oldEntity.getRoleType();
        if (RoleTypeEnum.SYSTEM.getCode().equals(roleType)
                && !Objects.equals(roleType, form.getRoleType())) {
            throw new SystemBusinessException(SystemResultCodeEnum.ROLE_SYSTEM_CANNOT_CHANGE_TYPE);
        }

        // 执行更新（role_code 变更由数据库 ON UPDATE CASCADE 自动传递到关联表）
        SysRoleEntity updateEntity = BeanUtil.copyProperties(form, SysRoleEntity.class);
        updateById(updateEntity);

        // 收集需要刷新缓存的用户 ID
        List<Long> affectedUserIds = new ArrayList<>(refreshUsersByRoleCode(oldEntity.getRoleCode()));

        // 去重后，先更新 perm_version，再刷新缓存
        if (CollUtil.isNotEmpty(affectedUserIds)) {
            List<Long> distinctIds = affectedUserIds.stream().distinct().collect(Collectors.toList());
            baseMapper.batchIncrementPermVersion(distinctIds);
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
        List<SysRoleEntity> entities = baseMapper.selectBatchIds(ids);

        // 检查是否存在不存在的 ID
        Set<Long> existedIds = entities.stream().map(SysRoleEntity::getId).collect(Collectors.toSet());
        ids.stream()
                .filter(id -> !existedIds.contains(id))
                .findFirst()
                .ifPresent(missingId -> {
                    throw new SystemBusinessException(SystemResultCodeEnum.ROLE_NOT_FOUND, missingId);
                });

        // 检查系统角色和被引用状态
        List<String> affectedRoleCodes = entities.stream()
                .peek(entity -> {
                    if (RoleTypeEnum.SYSTEM.getCode().equals(entity.getRoleType())) {
                        throw new SystemBusinessException(SystemResultCodeEnum.ROLE_SYSTEM_CANNOT_DELETE);
                    }
                })
                .peek(entity -> {
                    Integer refCount = baseMapper.countGrantTableRefByRoleCode(entity.getRoleCode());
                    if (refCount != null && refCount > 0) {
                        throw new SystemBusinessException(SystemResultCodeEnum.ROLE_IN_USE, refCount);
                    }
                })
                .map(SysRoleEntity::getRoleCode)
                .toList();

        // 批量逻辑删除（ORM 自动处理 is_deleted 字段）
        removeBatchByIds(ids);

        // 刷新缓存
        affectedRoleCodes.forEach(this::refreshAndFlushCache);
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

        // 使用实体更新方式，避免 Mockito 中 Lambda 缓存缺失
        SysRoleEntity updateEntity = new SysRoleEntity();
        updateEntity.setId(id);
        updateEntity.setStatus(status);
        baseMapper.updateById(updateEntity);

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
        return baseMapper.selectRoleOptions(roleName, roleCode);
    }

    /**
     * 查询持有指定角色的所有用户 ID，先更新 perm_version，再刷新缓存
     *
     * @param roleCode 角色编码
     */
    private void refreshAndFlushCache(String roleCode) {
        List<Long> userIds = refreshUsersByRoleCode(roleCode);
        if (CollUtil.isEmpty(userIds)) {
            return;
        }

        // 先自增 perm_version，使 JWT 版本比对失效
        baseMapper.batchIncrementPermVersion(userIds);
        authenticationInnerFeignClient.refreshUserCache(userIds);
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
            if (isOk(result)) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch user ids for roleCode={}: {}", roleCode, e.getMessage());
        }
        return ListUtil.empty();
    }

}
