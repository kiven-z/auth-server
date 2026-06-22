package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.convert.sys.SysRolePermissionConverter;
import com.auth.service.system.admin.mapper.role.SysRolePermissionMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
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
		Long roleId = roleReferenceChecker.getExistingByRoleCode(roleCode).getId();
		List<SysRoleAssignedPermissionPO> permissionList = sysRolePermissionMapper
			.selectAssignedPermissionsByRoleId(roleId);

		return SysRolePermissionConverter.INSTANCE.toAssignedPermissionRowList(permissionList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void assignPermissions(String roleCode, List<String> permissionCodes) {
		SysRoleEntity role = roleReferenceChecker.getExistingByRoleCode(roleCode);
		List<String> codes = permissionCodes.stream().distinct().toList();

		Long operatorId = SecurityUserUtils.getUserId();
		sysRolePermissionMapper.deleteByRoleId(role.getId());
		if (CollUtil.isNotEmpty(codes)) {
			List<Long> permissionIds = permissionReferenceChecker.resolveActivePermissionIds(codes);
			sysRolePermissionMapper.batchInsertRolePermissions(role.getId(), permissionIds, operatorId, operatorId);
		}

		roleInvalidationTrigger.submitByRoleCodes(List.of(role.getRoleCode()), "assign-permissions");
		log.info("Role permissions replaced: roleCode={}, count={}", role.getRoleCode(), codes.size());
	}

}
