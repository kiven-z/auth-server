package com.auth.service.system.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysPermissionMapper;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.mapper.SysRolePermissionMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.service.SysRolePermissionService;
import com.auth.service.system.authorization.dispatch.trigger.RoleAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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

	private final SysRoleMapper sysRoleMapper;

	private final SysRolePermissionMapper sysRolePermissionMapper;

	private final SysPermissionMapper sysPermissionMapper;

	private final RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	@Override
	public List<String> listPermissionCodes(String roleCode) {
		String normalized = requireExistingRoleCode(roleCode);
		return sysRolePermissionMapper.selectPermissionCodesByRoleCode(normalized);
	}

	@Override
	public void assignPermissions(String roleCode, List<String> permissionCodes) {
		String normalized = requireExistingRoleCode(roleCode);
		List<String> codes = permissionCodes.stream().distinct().toList();
		validatePermissionCodesExist(codes);

		Long operatorId = SecurityUserUtils.getUserId();
		if (operatorId == null) {
			operatorId = 0L;
		}

		sysRolePermissionMapper.deleteByRoleCode(normalized);
		if (CollUtil.isNotEmpty(codes)) {
			sysRolePermissionMapper.batchInsertRolePermissions(normalized, codes, operatorId, operatorId);
		}

		roleInvalidationTrigger.submitByRoleCodes(List.of(normalized), "assign-permissions:" + normalized);
		log.info("Role permissions replaced: roleCode={}, count={}", normalized, codes.size());
	}

	private String requireExistingRoleCode(String roleCode) {
		String normalized = CharSequenceUtil.trim(roleCode);
		if (CharSequenceUtil.isBlank(normalized)) {
			throw new SystemBusinessException(SystemAdminResultCode.ROLE_NOT_FOUND, roleCode);
		}

		SysRoleEntity role = sysRoleMapper.selectOne(
				Wrappers.<SysRoleEntity>lambdaQuery().eq(SysRoleEntity::getRoleCode, normalized).last("LIMIT 1"));
		if (role == null) {
			throw new SystemBusinessException(SystemAdminResultCode.ROLE_NOT_FOUND, normalized);
		}
		return normalized;
	}

	private void validatePermissionCodesExist(List<String> permissionCodes) {
		if (CollUtil.isEmpty(permissionCodes)) {
			return;
		}

		Long validCount = sysPermissionMapper.selectCount(Wrappers.<SysPermissionEntity>lambdaQuery()
			.in(SysPermissionEntity::getPermissionCode, permissionCodes)
			.eq(SysPermissionEntity::getStatus, true));
		if (validCount == null || validCount != permissionCodes.size()) {
			throw new SystemBusinessException(SystemAdminResultCode.ROLE_PERMISSION_INVALID);
		}
	}

}
