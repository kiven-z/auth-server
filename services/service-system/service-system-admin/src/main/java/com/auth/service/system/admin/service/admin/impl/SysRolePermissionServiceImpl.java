package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.convert.admin.ReferenceConverter;
import com.auth.service.system.admin.mapper.role.SysRolePermissionMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.po.reference.PermissionReferencePO;
import com.auth.service.system.admin.model.vo.reference.PermissionReferenceVO;
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
	public List<PermissionReferenceVO> listAssignedPermissions(Long roleId) {
		roleReferenceChecker.getExistingActive(roleId);
		List<PermissionReferencePO> permissionList = sysRolePermissionMapper.selectAssignedPermissionsByRoleId(roleId);

		return ReferenceConverter.INSTANCE.toPermissionReferenceList(permissionList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void assignPermissions(Long roleId, List<Long> permissionIds) {
		SysRoleEntity role = roleReferenceChecker.getExistingActive(roleId);

		List<Long> ids = permissionIds.stream().distinct().toList();
		Long operatorId = SecurityUserUtils.getUserId();

		sysRolePermissionMapper.deleteByRoleId(role.getId());
		if (CollUtil.isNotEmpty(ids)) {
			permissionReferenceChecker.requireActivePermissionIds(ids);
			sysRolePermissionMapper.batchInsertRolePermissions(role.getId(), ids, operatorId, operatorId);
		}

		roleInvalidationTrigger.submitByRoleCodes(List.of(role.getRoleCode()), "assign-permissions");
		log.info("Role permissions replaced: roleId={}, count={}", role.getId(), ids.size());
	}

}
