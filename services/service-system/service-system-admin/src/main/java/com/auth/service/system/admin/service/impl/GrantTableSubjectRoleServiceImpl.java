package com.auth.service.system.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.system.admin.convert.GrantTableSubjectRoleConverter;
import com.auth.service.system.admin.mapper.GrantTableMapper;
import com.auth.service.system.admin.model.enums.RoleTypeEnum;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;
import com.auth.service.system.admin.service.GrantTableSubjectRoleService;
import com.auth.service.system.admin.support.granttable.GrantTableSubjectExistenceVerifier;
import com.auth.service.system.admin.support.role.RoleGrantReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.GrantAuthorizationInvalidationTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * grant_table 主体角色授权实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class GrantTableSubjectRoleServiceImpl implements GrantTableSubjectRoleService {

	private final GrantTableSubjectExistenceVerifier subjectExistenceVerifier;

	private final GrantTableMapper grantTableMapper;

	private final RoleGrantReferenceChecker roleGrantReferenceChecker;

	private final GrantAuthorizationInvalidationTrigger grantInvalidationTrigger;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<GrantTableAssignedRoleRow> listAssignedRoles(GrantTableSubjectType subjectType, Long subjectId) {
		subjectExistenceVerifier.requireExistingActive(subjectType, subjectId);
		return GrantTableSubjectRoleConverter.INSTANCE.toAssignedRoleRowList(grantTableMapper
			.selectBoundRolesBySubject(subjectType.name(), subjectId, RoleTypeEnum.ORG_GRANT_TYPES));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replaceSubjectRoles(GrantTableSubjectType subjectType, Long subjectId, GrantTableAssignRoleForm form) {
		subjectExistenceVerifier.requireExistingActive(subjectType, subjectId);
		List<String> roleCodes = CollUtil.emptyIfNull(form.getRoleCodes()).stream().distinct().toList();
		roleGrantReferenceChecker.requireGrantTableOrgRoles(subjectType, roleCodes);

		Long operatorId = SecurityUserUtils.getUserId();
		grantTableMapper.deleteBySubject(subjectType.name(), subjectId);
		if (CollUtil.isNotEmpty(roleCodes)) {
			grantTableMapper.batchInsertSubjectRoleGrants(subjectType.name(), subjectId, roleCodes, operatorId);
		}

		grantInvalidationTrigger.submitByGrantSubjects(List.of(new GrantSubjectKey(subjectType, subjectId)),
				"replace-roles");
	}

}
