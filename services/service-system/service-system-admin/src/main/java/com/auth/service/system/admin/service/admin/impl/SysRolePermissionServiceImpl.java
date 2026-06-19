package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.convert.sys.SysRolePermissionConverter;
import com.auth.service.system.admin.mapper.role.SysRolePermissionMapper;
import com.auth.service.system.admin.model.po.role.SysRoleAssignedPermissionPO;
import com.auth.service.system.admin.model.vo.role.SysRoleAssignedPermissionRow;
import com.auth.service.system.admin.service.admin.SysRolePermissionService;
import com.auth.service.system.admin.support.permission.PermissionReferenceChecker;
import com.auth.service.system.admin.support.role.RoleReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.RoleAuthorizationInvalidationTrigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色权限分配服务实现。
 *
 * @author Bunny
 */

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysRolePermissionServiceImpl implements SysRolePermissionService {

	private final RoleReferenceChecker roleReferenceChecker;

	private final SysRolePermissionMapper sysRolePermissionMapper;

	private final PermissionReferenceChecker permissionReferenceChecker;

	private final RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysRoleAssignedPermissionRow> listAssignedPermissions(String roleCode) {
		String normalized = roleReferenceChecker.getExistingByRoleCode(roleCode).getRoleCode();
		List<SysRoleAssignedPermissionPO> permissionList = sysRolePermissionMapper
			.selectAssignedPermissionsByRoleCode(normalized);

		return SysRolePermissionConverter.INSTANCE.toAssignedPermissionRowList(permissionList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void assignPermissions(String roleCode, List<String> permissionCodes) {
		String normalized = roleReferenceChecker.getExistingByRoleCode(roleCode).getRoleCode();
		List<String> codes = permissionCodes.stream().distinct().toList();

		Long operatorId = SecurityUserUtils.getUserId();
		sysRolePermissionMapper.deleteByRoleCode(normalized);
		if (CollUtil.isNotEmpty(codes)) {
			permissionReferenceChecker.requireActivePermissionCodes(codes);
			sysRolePermissionMapper.batchInsertRolePermissions(normalized, codes, operatorId, operatorId);
		}

		roleInvalidationTrigger.submitByRoleCodes(List.of(normalized), "assign-permissions");
		log.info("Role permissions replaced: roleCode={}, count={}", normalized, codes.size());
	}

}
