package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.system.admin.convert.grant.GrantTableSubjectRoleConverter;
import com.auth.service.system.admin.mapper.role.GrantTableMapper;
import com.auth.service.system.admin.model.entity.GrantTableEntity;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundRolePO;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;
import com.auth.service.system.admin.service.admin.GrantTableService;
import com.auth.service.system.admin.support.granttable.GrantTableSubjectExistenceVerifier;
import com.auth.service.system.admin.support.role.RoleGrantReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.GrantAuthorizationInvalidationTrigger;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * grant_table 读写与主体角色授权编排实现。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class GrantTableServiceImpl extends ServiceImpl<GrantTableMapper, GrantTableEntity>
		implements GrantTableService {

	private final GrantTableSubjectExistenceVerifier subjectExistenceVerifier;

	private final RoleGrantReferenceChecker roleGrantReferenceChecker;

	private final GrantAuthorizationInvalidationTrigger grantInvalidationTrigger;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<GrantTableAssignedRoleRow> listAssignedRoles(GrantTableSubjectType subjectType, Long subjectId) {
		subjectExistenceVerifier.requireExistingActive(subjectType, subjectId);

		return listBoundRoles(subjectType, subjectId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<GrantTableAssignedRoleRow> listBoundRoles(GrantTableSubjectType subjectType, Long subjectId) {
		List<GrantTableBoundRolePO> poList = baseMapper.selectBoundRolesBySubject(subjectType.name(), subjectId);

		return GrantTableSubjectRoleConverter.INSTANCE.toAssignedRoleRowList(poList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replaceOrgSubjectRoles(GrantTableSubjectType subjectType, Long subjectId,
			GrantTableAssignRoleForm form) {
		// 校验主体是否存在
		subjectExistenceVerifier.requireExistingActive(subjectType, subjectId);

		// 校验角色编码
		List<String> roleCodes = CollUtil.emptyIfNull(form.getRoleCodes()).stream().distinct().toList();
		roleGrantReferenceChecker.requireGrantTableOrgRoles(subjectType, roleCodes);

		// 替换主体角色授权
		replaceSubjectRoleGrants(subjectType.name(), subjectId, roleCodes);
		grantInvalidationTrigger.submitByGrantSubjects(List.of(new GrantSubjectKey(subjectType, subjectId)),
				"replace-roles");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replaceSubjectRoleGrants(String subjectType, Long subjectId, List<String> roleCodes) {
		baseMapper.deleteBySubject(subjectType, subjectId);
		if (CollUtil.isEmpty(roleCodes)) {
			return;
		}

		List<GrantTableEntity> entities = roleCodes.stream().map(roleCode -> {
			GrantTableEntity entity = new GrantTableEntity();
			entity.setSubjectType(subjectType);
			entity.setSubjectId(subjectId);
			entity.setRoleCode(roleCode);
			return entity;
		}).toList();
		super.saveBatch(entities);
	}

}
