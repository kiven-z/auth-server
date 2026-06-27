package com.auth.service.system.admin.support.role;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.admin.mapper.role.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.exception.code.SystemResultCode;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 角色关联写入前的存在性与 eligibility 校验。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class RoleReferenceChecker {

	private final SysRoleMapper sysRoleMapper;

	/**
	 * 按角色 ID 加载角色，不存在则抛业务异常。
	 * @param roleId 角色 ID
	 * @return 角色实体
	 */
	public SysRoleEntity getExistingActive(Long roleId) {
		SysRoleEntity role = sysRoleMapper.selectById(roleId);
		if (role == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST, roleId);
		}
		return role;
	}

	/**
	 * 写入前：角色 ID 均存在且已启用。
	 * @param roleIds 待写入角色 ID（调用方应已去重）
	 * @param invalidCode 校验失败时抛出的结果码
	 */
	public void requireExistingEnabledRoleIds(List<Long> roleIds, SystemResultCode invalidCode) {
		if (CollUtil.isEmpty(roleIds)) {
			return;
		}
		Long eligibleCount = sysRoleMapper.selectCount(Wrappers.<SysRoleEntity>lambdaQuery()
			.in(SysRoleEntity::getId, roleIds)
			.eq(SysRoleEntity::getStatus, true));
		if (eligibleCount == null || eligibleCount != roleIds.size()) {
			throw new SystemBusinessException(invalidCode);
		}
	}

}
