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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> listPermissionCodes(String roleCode) {
		String normalized = requireExistingRoleCode(roleCode);
		return sysRolePermissionMapper.selectPermissionCodesByRoleCode(normalized);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void assignPermissions(String roleCode, List<String> permissionCodes) {
		String normalized = requireExistingRoleCode(roleCode);
		List<String> codes = permissionCodes.stream().distinct().toList();

		Long operatorId = SecurityUserUtils.getUserId();
		sysRolePermissionMapper.deleteByRoleCode(normalized);
		if (CollUtil.isNotEmpty(codes)) {
			Long validCount = sysPermissionMapper.selectCount(Wrappers.<SysPermissionEntity>lambdaQuery()
				.in(SysPermissionEntity::getPermissionCode, codes)
				.eq(SysPermissionEntity::getStatus, true));
			if (validCount == null || validCount != codes.size()) {
				throw new SystemBusinessException(SystemAdminResultCode.ROLE_PERMISSION_INVALID);
			}
			sysRolePermissionMapper.batchInsertRolePermissions(normalized, codes, operatorId, operatorId);
		}

		roleInvalidationTrigger.submitByRoleCodes(List.of(normalized), "assign-permissions:" + normalized);
		log.info("Role permissions replaced: roleCode={}, count={}", normalized, codes.size());
	}

	/**
	 * 确保角色代码存在
	 * @param roleCode 角色代码
	 * @return 角色代码
	 * @throws SystemBusinessException 如果角色代码不存在
	 */
	private String requireExistingRoleCode(String roleCode) {
		if (CharSequenceUtil.isBlank(roleCode)) {
			throw new SystemBusinessException(SystemAdminResultCode.ROLE_NOT_FOUND, roleCode);
		}

		SysRoleEntity role = sysRoleMapper
			.selectOne(Wrappers.<SysRoleEntity>lambdaQuery().eq(SysRoleEntity::getRoleCode, roleCode).last("LIMIT 1"));
		if (role == null) {
			throw new SystemBusinessException(SystemAdminResultCode.ROLE_NOT_FOUND, roleCode);
		}
		return roleCode;
	}

}
