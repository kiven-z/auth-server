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
	 * 权限 ID 均存在且已启用（调用方应已去重）。
	 * @param permissionIds 待写入权限 ID
	 */
	public void requireActivePermissionIds(List<Long> permissionIds) {
		if (CollUtil.isEmpty(permissionIds)) {
			return;
		}

		Long eligibleCount = sysPermissionMapper.selectCount(Wrappers.<SysPermissionEntity>lambdaQuery()
			.in(SysPermissionEntity::getId, permissionIds)
			.eq(SysPermissionEntity::getStatus, true));
		if (eligibleCount == null || eligibleCount != permissionIds.size()) {
			throw new SystemBusinessException(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		}
	}

}
