package com.auth.service.system.admin.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.system.admin.convert.SysDeptManageConverter;
import com.auth.service.system.admin.mapper.GrantTableMapper;
import com.auth.service.system.admin.mapper.SysDeptDetailMapper;
import com.auth.service.system.admin.model.form.dept.SysDeptAssignRoleForm;
import com.auth.service.system.admin.model.po.dept.SysDeptBoundRolePO;
import com.auth.service.system.admin.model.vo.dept.SysDeptAssignedRoleRow;
import com.auth.service.system.admin.service.SysDeptManageService;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.admin.support.role.RoleGrantReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.GrantAuthorizationInvalidationTrigger;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 部门扩展管理实现
 *
 * @author bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysDeptManageServiceImpl implements SysDeptManageService {

	private final DeptReferenceChecker deptReferenceChecker;

	private final SysDeptDetailMapper sysDeptDetailMapper;

	private final GrantTableMapper grantTableMapper;

	private final RoleGrantReferenceChecker roleGrantReferenceChecker;

	private final GrantAuthorizationInvalidationTrigger grantInvalidationTrigger;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysDeptAssignedRoleRow> listAssignedRoles(Long deptId) {
		// 检查当前部门是否存在且启用
		deptReferenceChecker.getExistingActive(deptId);

		// 查询当前部门已分配角色
		List<SysDeptBoundRolePO> boundRoles = sysDeptDetailMapper.selectBoundRolesByDeptId(deptId);
		return SysDeptManageConverter.INSTANCE.toAssignedRoleRowList(boundRoles);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replaceDeptRoles(Long deptId, SysDeptAssignRoleForm form) {
		// 检查当前部门是否存在且启用
		deptReferenceChecker.getExistingActive(deptId);
		List<String> roleCodes = CollUtil.emptyIfNull(form.getRoleCodes()).stream().distinct().toList();
		roleGrantReferenceChecker.requireDeptGrantEligibleRoles(roleCodes);

		// 删除当前部门已分配角色
		Long operatorId = SecurityUserUtils.getUserId();
		grantTableMapper.deleteBySubject(GrantTableSubjectType.DEPT.name(), deptId);
		if (CollUtil.isNotEmpty(roleCodes)) {
			grantTableMapper.batchInsertSubjectRoleGrants(GrantTableSubjectType.DEPT.name(), deptId, roleCodes,
					operatorId);
		}

		// 提交授权失效事件
		grantInvalidationTrigger.submitByGrantSubjects(List.of(new GrantSubjectKey(GrantTableSubjectType.DEPT, deptId)),
				"replace-dept-roles:" + deptId);
	}

}
