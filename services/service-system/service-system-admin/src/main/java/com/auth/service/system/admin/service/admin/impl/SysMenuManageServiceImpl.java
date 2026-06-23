package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.admin.convert.reference.ReferenceConverter;
import com.auth.service.system.admin.mapper.menu.SysMenuMapper;
import com.auth.service.system.admin.mapper.menu.SysMenuPermissionMapper;
import com.auth.service.system.admin.mapper.menu.SysMenuRoleMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignPermissionForm;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignRoleForm;
import com.auth.service.system.admin.model.po.reference.PermissionReferencePO;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.vo.reference.ext.MenuAssignedPermissionReferenceVO;
import com.auth.service.system.admin.model.vo.reference.ext.MenuAssignedRoleReferenceVO;
import com.auth.service.system.admin.service.admin.SysMenuManageService;
import com.auth.service.system.admin.support.permission.PermissionReferenceChecker;
import com.auth.service.system.admin.support.role.RoleGrantReferenceChecker;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 菜单管理实现
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysMenuManageServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity>
		implements SysMenuManageService {

	private final SysMenuRoleMapper sysMenuRoleMapper;

	private final SysMenuPermissionMapper sysMenuPermissionMapper;

	private final RoleGrantReferenceChecker roleGrantReferenceChecker;

	private final PermissionReferenceChecker permissionReferenceChecker;

	public SysMenuManageServiceImpl(SysMenuRoleMapper sysMenuRoleMapper,
			SysMenuPermissionMapper sysMenuPermissionMapper, RoleGrantReferenceChecker roleGrantReferenceChecker,
			PermissionReferenceChecker permissionReferenceChecker) {
		this.sysMenuRoleMapper = sysMenuRoleMapper;
		this.sysMenuPermissionMapper = sysMenuPermissionMapper;
		this.roleGrantReferenceChecker = roleGrantReferenceChecker;
		this.permissionReferenceChecker = permissionReferenceChecker;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MenuAssignedRoleReferenceVO> listAssignedRoles(Long menuId) {
		List<RoleReferencePO> dtoList = sysMenuRoleMapper.selectAssignedRolesByMenuId(menuId);

		return ReferenceConverter.INSTANCE.toMenuAssignedRoleList(dtoList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replaceMenuRoles(Long menuId, SysMenuAssignRoleForm form) {
		List<Long> roleIds = CollUtil.emptyIfNull(form.getRoleIds()).stream().distinct().toList();
		roleGrantReferenceChecker.requireExistingEnabledRoleIds(roleIds,
				SystemCommonResultCode.GRANT_REFERENCE_INVALID);

		sysMenuRoleMapper.deleteByMenuId(menuId);

		if (CollUtil.isNotEmpty(roleIds)) {
			sysMenuRoleMapper.batchInsertMenuRoles(menuId, roleIds);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MenuAssignedPermissionReferenceVO> listPermissionsWithAssignFlags(Long menuId) {
		List<PermissionReferencePO> dtoList = sysMenuPermissionMapper.selectAssignedPermissionByMenuId(menuId);

		return ReferenceConverter.INSTANCE.toMenuAssignedPermissionList(dtoList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replaceMenuPermissions(Long menuId, SysMenuAssignPermissionForm form) {
		List<Long> permissionIds = CollUtil.emptyIfNull(form.getPermissionIds()).stream().distinct().toList();
		permissionReferenceChecker.requireActivePermissionIds(permissionIds);

		sysMenuPermissionMapper.deleteByMenuId(menuId);
		if (CollUtil.isNotEmpty(permissionIds)) {
			sysMenuPermissionMapper.batchInsertMenuPermissions(menuId, permissionIds);
		}
	}

}
