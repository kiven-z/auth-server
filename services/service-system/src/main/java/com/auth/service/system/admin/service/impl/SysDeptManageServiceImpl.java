package com.auth.service.system.admin.service.impl;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;
import com.auth.service.system.admin.service.GrantTableSubjectRoleService;
import com.auth.service.system.admin.service.SysDeptManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

	private final GrantTableSubjectRoleService grantTableSubjectRoleService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<GrantTableAssignedRoleRow> listAssignedRoles(Long deptId) {
		// FIXME 让Controller直接连接GrantTableSubjectRoleService
		return grantTableSubjectRoleService.listAssignedRoles(GrantTableSubjectType.DEPT, deptId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replaceDeptRoles(Long deptId, GrantTableAssignRoleForm form) {
		grantTableSubjectRoleService.replaceSubjectRoles(GrantTableSubjectType.DEPT, deptId, form);
	}

}
