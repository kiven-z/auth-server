package com.auth.service.system.admin.service.impl;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;
import com.auth.service.system.admin.service.GrantTableSubjectRoleService;
import com.auth.service.system.admin.service.SysPostManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 岗位扩展管理实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysPostManageServiceImpl implements SysPostManageService {

	private final GrantTableSubjectRoleService grantTableSubjectRoleService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<GrantTableAssignedRoleRow> listAssignedRoles(Long postId) {
		// FIXME 让Controller直接连接GrantTableSubjectRoleService
		return grantTableSubjectRoleService.listAssignedRoles(GrantTableSubjectType.POST, postId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replacePostRoles(Long postId, GrantTableAssignRoleForm form) {
		grantTableSubjectRoleService.replaceSubjectRoles(GrantTableSubjectType.POST, postId, form);
	}

}
