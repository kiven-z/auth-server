package com.auth.service.system.admin.support.permission;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.admin.mapper.permission.SysPermissionMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
		resolveActivePermissionIds(permissionCodes);
	}

	/**
	 * 校验权限码均存在且已启用，并返回对应主键 ID（与入参去重后顺序一致）。
	 * @param permissionCodes 待写入权限码（调用方应已去重）
	 * @return 权限 ID 列表
	 */
	public List<Long> resolveActivePermissionIds(List<String> permissionCodes) {
		if (CollUtil.isEmpty(permissionCodes)) {
			return List.of();
		}

		List<SysPermissionEntity> activePermissions = sysPermissionMapper
			.selectList(Wrappers.<SysPermissionEntity>lambdaQuery()
				.in(SysPermissionEntity::getPermissionCode, permissionCodes)
				.eq(SysPermissionEntity::getStatus, true));

		if (activePermissions.size() != permissionCodes.size()) {
			throw new SystemBusinessException(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		}

		Map<String, Long> codeToId = activePermissions.stream()
			.collect(Collectors.toMap(SysPermissionEntity::getPermissionCode, SysPermissionEntity::getId,
					(existing, duplicate) -> existing));

		return permissionCodes.stream().map(codeToId::get).toList();
	}

}
