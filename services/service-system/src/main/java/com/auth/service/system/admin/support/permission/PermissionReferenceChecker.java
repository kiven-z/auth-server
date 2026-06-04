package com.auth.service.system.admin.support.permission;

import java.util.List;

import org.springframework.stereotype.Component;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysPermissionMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

/**
 * 权限关联写入前的存在性与启用状态校验。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class PermissionReferenceChecker {

	private final SysPermissionMapper sysPermissionMapper;

	/**
	 * 权限码均存在且已启用（调用方应已去重）。
	 * @param permissionCodes 待写入权限码
	 */
	public void requireActivePermissionCodes(List<String> permissionCodes) {
		if (CollUtil.isEmpty(permissionCodes)) {
			return;
		}
		Long activeCount = sysPermissionMapper.selectCount(Wrappers.<SysPermissionEntity>lambdaQuery()
			.in(SysPermissionEntity::getPermissionCode, permissionCodes)
			.eq(SysPermissionEntity::getStatus, true));
		if (activeCount != permissionCodes.size()) {
			throw new SystemBusinessException(SystemAdminResultCode.ROLE_PERMISSION_INVALID);
		}
	}

}
