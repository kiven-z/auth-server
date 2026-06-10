package com.auth.service.system.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.UserDeptAssignConvert;
import com.auth.service.system.admin.convert.UserDeptConverter;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.UserDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.entity.UserDeptEntity;
import com.auth.service.system.admin.model.enums.UserOrgRelationStatusEnum;
import com.auth.service.system.admin.model.form.userdept.UserDeptAssignForm;
import com.auth.service.system.admin.model.po.userdept.UserDeptPageRowPO;
import com.auth.service.system.admin.model.query.user.UserDeptPageQuery;
import com.auth.service.system.admin.model.vo.userdept.UserDeptPageVO;
import com.auth.service.system.admin.service.SysUserDeptService;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.admin.support.user.UserOperationChecker;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.admin.support.userdept.UserDeptPrimaryCoordinator;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<UserDeptPageVO> getPage(Long userId, UserDeptPageQuery query) {
		userReferenceChecker.getExistingActive(userId);

		Page<UserDeptEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<UserDeptPageRowPO> page = userDeptMapper.selectListByPage(pageParams, userId, query);
		IPage<UserDeptPageVO> voPage = page.convert(UserDeptConverter.INSTANCE::toPageVo);

		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(UserDeptAssignForm form) {
		Long userId = form.getUserId();
		Long deptId = form.getDeptId();

		userReferenceChecker.getExistingActive(userId);
		userOperationChecker.requireOperable(List.of(userId));

		SysDeptEntity dept = deptReferenceChecker.getExistingActive(deptId);
		if (BooleanUtil.isFalse(dept.getStatus())) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE, deptId);
		}
		if (userDeptMapper.countByUserIdAndDeptId(userId, deptId) > 0) {
			throw new SystemBusinessException(SystemAdminResultCode.USER_DEPT_DUPLICATE, deptId);
		}

		boolean isPrimary = resolveCreatePrimary(userId, form);
		UserDeptEntity entity = UserDeptAssignConvert.toCreateEntity(form, isPrimary);

		userDeptMapper.insert(entity);
		if (Boolean.TRUE.equals(entity.getIsPrimary())) {
			userDeptPrimaryCoordinator.promotePrimary(userId, entity.getId());
		}
		userDeptPrimaryCoordinator.ensurePrimaryInvariant(userId);
		userAuthorizationInvalidationTrigger.submitByUserIds(List.of(userId), "create-dept");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(UserDeptAssignForm form) {
		Long userId = form.getUserId();
		Long deptId = form.getDeptId();

		userReferenceChecker.getExistingActive(userId);
		userOperationChecker.requireOperable(List.of(userId));

		UserDeptEntity existing = requireExistingRelationByDept(userId, deptId);
		Long relationId = existing.getId();
		if (Boolean.TRUE.equals(form.getIsPrimary())) {
			requirePrimaryEligibility(form.getStatus(), true);
		}

		UserDeptEntity entity = UserDeptAssignConvert.toUpdateEntity(relationId, form);

		userDeptMapper.updateById(entity);

		if (Boolean.TRUE.equals(form.getIsPrimary()) && UserOrgRelationStatusEnum.canBePrimary(form.getStatus())) {
			userDeptPrimaryCoordinator.promotePrimary(userId, relationId);
		}

		userDeptPrimaryCoordinator.ensurePrimaryInvariant(userId);
		userAuthorizationInvalidationTrigger.submitByUserIds(List.of(userId), "update-dept");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPrimary(Long userId, Long deptId) {
		userReferenceChecker.getExistingActive(userId);
		userOperationChecker.requireOperable(List.of(userId));

		UserDeptEntity existing = requireExistingRelationByDept(userId, deptId);
		if (!UserOrgRelationStatusEnum.canBePrimary(existing.getStatus())) {
			throw new SystemBusinessException(SystemAdminResultCode.USER_DEPT_PRIMARY_NOT_ELIGIBLE, existing.getId());
		}

		userDeptPrimaryCoordinator.promotePrimary(userId, existing.getId());
		userAuthorizationInvalidationTrigger.submitByUserIds(List.of(userId), "set-primary-dept");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void transfer(Long userId, Long deptId) {
		userReferenceChecker.getExistingActive(userId);
		userOperationChecker.requireOperable(List.of(userId));

		UserDeptEntity existing = requireExistingRelationByDept(userId, deptId);
		Long relationId = existing.getId();
		UserDeptEntity entity = new UserDeptEntity();
		entity.setId(relationId);
		entity.setStatus(UserOrgRelationStatusEnum.TRANSFERRED.getCode());
		entity.setIsPrimary(false);
		userDeptMapper.updateById(entity);

		userDeptPrimaryCoordinator.ensurePrimaryInvariant(userId);
		userAuthorizationInvalidationTrigger.submitByUserIds(List.of(userId), "transfer-dept");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteByDeptIds(Long userId, List<Long> deptIds) {
		userReferenceChecker.getExistingActive(userId);
		userOperationChecker.requireOperable(List.of(userId));

		List<Long> ids = CollUtil.emptyIfNull(deptIds).stream().distinct().toList();
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		for (Long deptId : ids) {
			UserDeptEntity existing = requireExistingRelationByDept(userId, deptId);
			userDeptMapper.deleteById(existing.getId());
		}

		userDeptPrimaryCoordinator.ensurePrimaryInvariant(userId);
		userAuthorizationInvalidationTrigger.submitByUserIds(List.of(userId), "delete-dept");
	}

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

	private UserDeptEntity requireExistingRelationByDept(Long userId, Long deptId) {
		UserDeptEntity existing = userDeptMapper.selectByUserIdAndDeptId(userId, deptId);
		if (existing == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST, deptId);
		}
		return existing;
	}

	private void requirePrimaryEligibility(Integer status, Boolean isPrimary) {
		if (Boolean.TRUE.equals(isPrimary) && !UserOrgRelationStatusEnum.canBePrimary(status)) {
			throw new SystemBusinessException(SystemAdminResultCode.USER_DEPT_PRIMARY_NOT_ELIGIBLE);
		}
	}

}
