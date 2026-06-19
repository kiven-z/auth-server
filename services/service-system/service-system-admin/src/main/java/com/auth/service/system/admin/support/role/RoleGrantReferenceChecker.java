package com.auth.service.system.admin.support.role;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
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
 * 角色关联写入前的 eligibility 校验（grant_table、sys_menu_role 等）
 *
 * @author bunny
 */
@RequiredArgsConstructor
@Component
public class RoleGrantReferenceChecker {

	private final SysRoleMapper sysRoleMapper;

	/**
	 * 写入前：角色均存在且已启用
	 * @param roleCodes 待写入角色编码（调用方应已去重）
	 * @param invalidCode 校验失败时抛出的结果码
	 */
	public void requireExistingEnabledRoles(List<String> roleCodes, SystemResultCode invalidCode) {
		if (CollUtil.isEmpty(roleCodes)) {
			return;
		}
		Long eligibleCount = sysRoleMapper.selectCount(Wrappers.<SysRoleEntity>lambdaQuery()
			.in(SysRoleEntity::getRoleCode, roleCodes)
			.eq(SysRoleEntity::getStatus, true));
		if (eligibleCount == null || eligibleCount != roleCodes.size()) {
			throw new SystemBusinessException(invalidCode);
		}
	}

	/**
	 * 组织类 grant_table 主体（部门、岗位等）：校验角色存在且已启用
	 * @param subjectType 授权主体类型（部门、岗位等）
	 * @param roleCodes 待写入角色编码
	 */
	public void requireGrantTableOrgRoles(GrantTableSubjectType subjectType, List<String> roleCodes) {
		if (subjectType != GrantTableSubjectType.DEPT && subjectType != GrantTableSubjectType.POST) {
			throw new IllegalArgumentException("Unsupported org grant subject type: " + subjectType);
		}
		requireExistingEnabledRoles(roleCodes, SystemCommonResultCode.GRANT_REFERENCE_INVALID);
	}

}
