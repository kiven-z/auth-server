package com.auth.service.system.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.UserDeptConverter;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.UserDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.entity.UserDeptEntity;
import com.auth.service.system.admin.model.enums.UserOrgRelationStatusEnum;
import com.auth.service.system.admin.model.form.userdept.UserDeptAssignForm;
import com.auth.service.system.admin.model.form.userdept.UserDeptUpdateForm;
import com.auth.service.system.admin.model.po.userdept.UserDeptPageRowPO;
import com.auth.service.system.admin.model.query.user.UserDeptPageQuery;
import com.auth.service.system.admin.model.vo.userdept.UserDeptPageVO;
import com.auth.service.system.admin.service.SysUserDeptService;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.admin.support.user.UserOperationChecker;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.admin.support.userdept.UserDeptPrimaryCoordinator;
import com.auth.service.system.authorization.dispatch.trigger.DeptAuthorizationInvalidationTrigger;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户部门关联服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserDeptServiceImpl implements SysUserDeptService {

    private final UserReferenceChecker userReferenceChecker;

    private final UserOperationChecker userOperationChecker;

    private final DeptReferenceChecker deptReferenceChecker;

    private final UserDeptMapper userDeptMapper;

    private final UserDeptPrimaryCoordinator userDeptPrimaryCoordinator;

    private final UserAuthorizationInvalidationTrigger userAuthorizationInvalidationTrigger;

    private final DeptAuthorizationInvalidationTrigger deptAuthorizationInvalidationTrigger;

    private final AuditUserDisplayService auditUserDisplayService;

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponse<UserDeptPageVO> getPage(Long userId, UserDeptPageQuery query) {
        // 检查当前用户是否存在
        userReferenceChecker.getExistingActive(userId);

        Page<UserDeptEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
        IPage<UserDeptPageRowPO> page = userDeptMapper.selectListByPage(pageParams, userId, query);
        IPage<UserDeptPageVO> voPage = page.convert(UserDeptConverter.INSTANCE::toPageVo);

        // 设置审计用户名
        auditUserDisplayService.enrichAuditUsernames(voPage);
        return PageResponse.of(voPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(Long userId, UserDeptAssignForm form) {
        // 检查当前用户是否存在
        userReferenceChecker.getExistingActive(userId);

        // 操作用户不能是自己
        userOperationChecker.requireOperable(List.of(userId));

        Long deptId = form.getDeptId();
        SysDeptEntity dept = deptReferenceChecker.getExistingActive(deptId);
        if (BooleanUtil.isFalse(dept.getStatus())) {
            throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE, deptId);
        }
        // 统计用户与部门是否已有关联
        if (userDeptMapper.countByUserIdAndDeptId(userId, deptId) > 0) {
            throw new SystemBusinessException(SystemAdminResultCode.USER_DEPT_DUPLICATE, deptId);
        }

        UserDeptEntity entity = new UserDeptEntity();
        entity.setUserId(userId);
        entity.setDeptId(form.getDeptId());
        entity.setStatus(form.getStatus());
        entity.setPosition(form.getPosition());
        entity.setRemark(form.getRemark());
        entity.setGrantTime(LocalDateTime.now());
        entity.setIsPrimary(resolveCreatePrimary(userId, form));

        // 检查当前部门状态是否可以被设置为主部门
        if (!UserOrgRelationStatusEnum.canBePrimary(entity.getStatus())) {
            entity.setIsPrimary(false);
        }

        userDeptMapper.insert(entity);
        applyPrimaryAfterCreate(userId, entity);
        userDeptPrimaryCoordinator.ensurePrimaryInvariant(userId);
        submitInvalidation(userId, List.of(deptId), "create-dept");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // FIXME 待确认
    public void update(Long userId, Long relationId, UserDeptUpdateForm form) {
        userReferenceChecker.getExistingActive(userId);
        userOperationChecker.requireOperable(List.of(userId));

        UserDeptEntity existing = requireExistingRelation(userId, relationId);
        if (Boolean.TRUE.equals(form.getIsPrimary())) {
            requirePrimaryEligibility(form.getStatus(), true);
        }

        UserDeptEntity entity = new UserDeptEntity();
        entity.setId(relationId);
        entity.setStatus(form.getStatus());
        entity.setPosition(form.getPosition());
        entity.setRemark(form.getRemark());
        entity.setVersion(form.getVersion());

        if (!UserOrgRelationStatusEnum.canBePrimary(form.getStatus())) {
            entity.setIsPrimary(false);
        } else if (Boolean.FALSE.equals(form.getIsPrimary())) {
            entity.setIsPrimary(false);
        }

        int updated = userDeptMapper.updateById(entity);
        if (updated == 0) {
            throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST, relationId);
        }

        if (Boolean.TRUE.equals(form.getIsPrimary()) && UserOrgRelationStatusEnum.canBePrimary(form.getStatus())) {
            userDeptPrimaryCoordinator.promotePrimary(userId, relationId);
        }

        userDeptPrimaryCoordinator.ensurePrimaryInvariant(userId);
        submitInvalidation(userId, List.of(existing.getDeptId()), "update-dept");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // FIXME 待确认
    public void setPrimary(Long userId, Long relationId) {
        userReferenceChecker.getExistingActive(userId);
        userOperationChecker.requireOperable(List.of(userId));

        UserDeptEntity existing = requireExistingRelation(userId, relationId);
        if (!UserOrgRelationStatusEnum.canBePrimary(existing.getStatus())) {
            throw new SystemBusinessException(SystemAdminResultCode.USER_DEPT_PRIMARY_NOT_ELIGIBLE, relationId);
        }

        userDeptPrimaryCoordinator.promotePrimary(userId, relationId);
        submitInvalidation(userId, List.of(existing.getDeptId()), "set-primary-dept");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // FIXME 待确认
    public void transfer(Long userId, Long relationId) {
        userReferenceChecker.getExistingActive(userId);
        userOperationChecker.requireOperable(List.of(userId));

        UserDeptEntity existing = requireExistingRelation(userId, relationId);
        UserDeptEntity entity = new UserDeptEntity();
        entity.setId(relationId);
        entity.setStatus(UserOrgRelationStatusEnum.TRANSFERRED.getCode());
        entity.setIsPrimary(false);
        userDeptMapper.updateById(entity);

        userDeptPrimaryCoordinator.ensurePrimaryInvariant(userId);
        submitInvalidation(userId, List.of(existing.getDeptId()), "transfer-dept");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteByIds(Long userId, List<Long> relationIds) {
        // FIXME 重写下面方法内容，删除直接删除，用户可以没有部门
        userReferenceChecker.getExistingActive(userId);
        userOperationChecker.requireOperable(List.of(userId));

        List<Long> ids = CollUtil.emptyIfNull(relationIds).stream().distinct().toList();
        if (CollUtil.isEmpty(ids)) {
            return;
        }

        Set<Long> deptIds = new HashSet<>();
        for (Long relationId : ids) {
            UserDeptEntity existing = requireExistingRelation(userId, relationId);
            deptIds.add(existing.getDeptId());
            userDeptMapper.deleteById(relationId);
        }

        // FIXME 删除完成后直接 trigger 当前用户id
        userDeptPrimaryCoordinator.ensurePrimaryInvariant(userId);
        submitInvalidation(userId, new ArrayList<>(deptIds), "delete-dept");
    }

    // FIXME 待确认
    private boolean resolveCreatePrimary(Long userId, UserDeptAssignForm form) {
        if (form.getIsPrimary() != null) {
            requirePrimaryEligibility(form.getStatus(), form.getIsPrimary());
            return form.getIsPrimary();
        }
        if (!UserOrgRelationStatusEnum.canBePrimary(form.getStatus())) {
            return false;
        }
        return userDeptMapper.selectActiveByUserId(userId).isEmpty();
    }

    // FIXME 待确认
    private void applyPrimaryAfterCreate(Long userId, UserDeptEntity entity) {
        if (!Boolean.TRUE.equals(entity.getIsPrimary())) {
            return;
        }
        userDeptPrimaryCoordinator.promotePrimary(userId, entity.getId());
    }

    // FIXME 待确认
    private UserDeptEntity requireExistingRelation(Long userId, Long relationId) {
        UserDeptEntity existing = userDeptMapper.selectByIdAndUserId(relationId, userId);
        if (existing == null) {
            throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST, relationId);
        }
        return existing;
    }

    // FIXME 待确认
    private void requirePrimaryEligibility(Integer status, Boolean isPrimary) {
        if (Boolean.TRUE.equals(isPrimary) && !UserOrgRelationStatusEnum.canBePrimary(status)) {
            throw new SystemBusinessException(SystemAdminResultCode.USER_DEPT_PRIMARY_NOT_ELIGIBLE);
        }
    }

    private void submitInvalidation(Long userId, List<Long> deptIds, String operation) {
        // FIXME 这写的是什么？？？用户分配哪来更新部门的？？？整个用户管理最简单的地方就是不用考虑受全面，只要考虑当前用户，只更新当前用户就行了
        userAuthorizationInvalidationTrigger.submitByUserIds(List.of(userId), operation);
        deptAuthorizationInvalidationTrigger.submitByDeptIds(deptIds, operation);
    }

}
