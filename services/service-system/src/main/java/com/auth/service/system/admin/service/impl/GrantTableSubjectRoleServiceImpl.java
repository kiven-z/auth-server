package com.auth.service.system.admin.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.system.admin.convert.GrantTableSubjectRoleConverter;
import com.auth.service.system.admin.mapper.GrantTableMapper;
import com.auth.service.system.admin.model.enums.RoleTypeEnum;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;
import com.auth.service.system.admin.service.GrantTableSubjectRoleService;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import com.auth.service.system.admin.support.role.RoleGrantReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.GrantAuthorizationInvalidationTrigger;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

/**
 * grant_table 主体角色授权实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class GrantTableSubjectRoleServiceImpl implements GrantTableSubjectRoleService {

	private final DeptReferenceChecker deptReferenceChecker;

	private final PostReferenceChecker postReferenceChecker;

	private final GrantTableMapper grantTableMapper;

	private final RoleGrantReferenceChecker roleGrantReferenceChecker;

	private final GrantAuthorizationInvalidationTrigger grantInvalidationTrigger;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<GrantTableAssignedRoleRow> listAssignedRoles(GrantTableSubjectType subjectType, Long subjectId) {
		requireExistingActive(subjectType, subjectId);
		return GrantTableSubjectRoleConverter.INSTANCE.toAssignedRoleRowList(grantTableMapper
			.selectBoundRolesBySubject(subjectType.name(), subjectId, RoleTypeEnum.ORG_GRANT_TYPES));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replaceSubjectRoles(GrantTableSubjectType subjectType, Long subjectId, GrantTableAssignRoleForm form) {
		// 1. 检查主体是否存在且为活动状态
		requireExistingActive(subjectType, subjectId);
		List<String> roleCodes = CollUtil.emptyIfNull(form.getRoleCodes()).stream().distinct().toList();
		roleGrantReferenceChecker.requireGrantTableOrgRoles(subjectType, roleCodes);

		// 2. 删除当前主体的所有角色授权
		Long operatorId = SecurityUserUtils.getUserId();
		grantTableMapper.deleteBySubject(subjectType.name(), subjectId);
		if (CollUtil.isNotEmpty(roleCodes)) {
			grantTableMapper.batchInsertSubjectRoleGrants(subjectType.name(), subjectId, roleCodes, operatorId);
		}

		// 3. 提交授权边失效触发
		grantInvalidationTrigger.submitByGrantSubjects(List.of(new GrantSubjectKey(subjectType, subjectId)),
				"replace-" + subjectType.name() + "-roles:" + subjectId);
	}

	/**
	 * 检查主体是否存在且为活动状态
	 * @param subjectType 主体类型
	 * @param subjectId 主体 ID
	 */
	private void requireExistingActive(GrantTableSubjectType subjectType, Long subjectId) {
		switch (subjectType) {
			case DEPT -> deptReferenceChecker.getExistingActive(subjectId);
			case POST -> postReferenceChecker.getExistingActive(subjectId);
			default -> throw new IllegalArgumentException("Unsupported grant subject type: " + subjectType);
		}
	}

}
