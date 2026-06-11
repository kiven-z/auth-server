package com.auth.service.system.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.model.enums.RoleTypeEnum;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;
import com.auth.service.system.admin.service.GrantTableService;
import com.auth.service.system.admin.service.SysUserRoleService;
import com.auth.service.system.admin.support.role.RoleGrantReferenceChecker;
import com.auth.service.system.admin.support.user.UserOperationChecker;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户直连角色授权实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserRoleServiceImpl implements SysUserRoleService {

	private final UserReferenceChecker userReferenceChecker;

	private final UserOperationChecker userOperationChecker;

	private final RoleGrantReferenceChecker roleGrantReferenceChecker;

	private final GrantTableService grantTableService;

	private final UserAuthorizationInvalidationTrigger userAuthorizationInvalidationTrigger;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<GrantTableAssignedRoleRow> listAssignedRoles(Long userId) {
		userReferenceChecker.getExistingActive(userId);
		return grantTableService.listBoundRoles(GrantTableSubjectType.USER, userId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replaceUserRoles(Long userId, GrantTableAssignRoleForm form) {
		userReferenceChecker.getExistingActive(userId);
		userOperationChecker.requireOperable(List.of(userId));

		List<String> roleCodes = CollUtil.emptyIfNull(form.getRoleCodes()).stream().distinct().toList();
		roleGrantReferenceChecker.requireGrantEligibleRoles(roleCodes, RoleTypeEnum.SUBJECT_GRANT_TYPES,
				SystemCommonResultCode.GRANT_REFERENCE_INVALID);

		grantTableService.replaceSubjectRoleGrants(GrantTableSubjectType.USER.name(), userId, roleCodes);

		userAuthorizationInvalidationTrigger.submitByUserIds(List.of(userId), "replace-roles");
	}

}
