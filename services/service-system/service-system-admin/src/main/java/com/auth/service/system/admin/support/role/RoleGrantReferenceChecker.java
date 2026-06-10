package com.auth.service.system.admin.support.role;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.enums.RoleTypeEnum;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.exception.code.SystemResultCode;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
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
	 * grant_table 写入前：角色均存在、已启用且 roleType 在允许集合内
	 * @param roleCodes 待写入角色编码（调用方应已去重）
	 * @param allowedRoleTypes 允许的 roleType 集合
	 * @param invalidCode 校验失败时抛出的结果码
	 */
	public void requireGrantEligibleRoles(List<String> roleCodes, Collection<Integer> allowedRoleTypes,
			SystemResultCode invalidCode) {
		if (CollUtil.isEmpty(roleCodes)) {
			return;
		}
		Long eligibleCount = sysRoleMapper.selectCount(Wrappers.<SysRoleEntity>lambdaQuery()
			.in(SysRoleEntity::getRoleCode, roleCodes)
			.eq(SysRoleEntity::getStatus, true)
			.in(SysRoleEntity::getRoleType, allowedRoleTypes));
		if (eligibleCount == null || eligibleCount != roleCodes.size()) {
			throw new SystemBusinessException(invalidCode);
		}
	}

	/**
	 * 组织类 grant_table 主体：仅允许业务/自定义角色（roleType 3/4）
	 * @param subjectType 授权主体类型（部门、岗位等）
	 * @param roleCodes 待写入角色编码
	 */
	public void requireGrantTableOrgRoles(GrantTableSubjectType subjectType, List<String> roleCodes) {
		if (subjectType != GrantTableSubjectType.DEPT && subjectType != GrantTableSubjectType.POST) {
			throw new IllegalArgumentException("Unsupported org grant subject type: " + subjectType);
		}
		requireGrantEligibleRoles(roleCodes, RoleTypeEnum.ORG_GRANT_TYPES,
				SystemCommonResultCode.GRANT_REFERENCE_INVALID);
	}

	/**
	 * 用户直连 grant_table 主体：仅允许业务/自定义角色（roleType 3/4）
	 * @param roleCodes 待写入角色编码
	 */
	public void requireGrantTableUserRoles(List<String> roleCodes) {
		requireGrantEligibleRoles(roleCodes, RoleTypeEnum.USER_GRANT_TYPES,
				SystemCommonResultCode.GRANT_REFERENCE_INVALID);
	}

}
