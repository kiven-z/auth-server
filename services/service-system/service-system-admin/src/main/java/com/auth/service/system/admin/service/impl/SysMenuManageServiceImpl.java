package com.auth.service.system.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.admin.convert.SysMenuManageConverter;
import com.auth.service.system.admin.mapper.SysMenuMapper;
import com.auth.service.system.admin.mapper.SysMenuPermissionMapper;
import com.auth.service.system.admin.mapper.SysMenuRoleMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.enums.RoleTypeEnum;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignPermissionForm;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignRoleForm;
import com.auth.service.system.admin.model.po.menu.SysMenuAssignedPermissionPO;
import com.auth.service.system.admin.model.po.menu.SysMenuAssignedRolePO;
import com.auth.service.system.admin.model.vo.menu.SysMenuAssignedPermissionRow;
import com.auth.service.system.admin.model.vo.menu.SysMenuAssignedRoleRow;
import com.auth.service.system.admin.service.SysMenuManageService;
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

	public SysMenuManageServiceImpl(SysMenuRoleMapper sysMenuRoleMapper,
			SysMenuPermissionMapper sysMenuPermissionMapper, RoleGrantReferenceChecker roleGrantReferenceChecker) {
		this.sysMenuRoleMapper = sysMenuRoleMapper;
		this.sysMenuPermissionMapper = sysMenuPermissionMapper;
		this.roleGrantReferenceChecker = roleGrantReferenceChecker;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysMenuAssignedRoleRow> listAssignedRoles(Long menuId) {
		List<SysMenuAssignedRolePO> dtoList = sysMenuRoleMapper.selectAssignedRolesByMenuId(menuId);

		return dtoList.stream().map(SysMenuManageConverter.INSTANCE::toAssignedRoleRow).toList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replaceMenuRoles(Long menuId, SysMenuAssignRoleForm form) {
		List<String> roleCodes = CollUtil.emptyIfNull(form.getRoleCodes()).stream().distinct().toList();
		roleGrantReferenceChecker.requireGrantEligibleRoles(roleCodes, RoleTypeEnum.MENU_ASSIGN_TYPES,
				SystemCommonResultCode.GRANT_REFERENCE_INVALID);

		// 删除当前菜单已分配角色
		sysMenuRoleMapper.deleteByMenuId(menuId);

		// 插入新的菜单-角色关联
		if (CollUtil.isNotEmpty(roleCodes)) {
			sysMenuRoleMapper.batchInsertMenuRoles(menuId, roleCodes);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysMenuAssignedPermissionRow> listPermissionsWithAssignFlags(Long menuId) {
		List<SysMenuAssignedPermissionPO> dtoList = sysMenuPermissionMapper.selectAssignedPermissionByMenuId(menuId);

		return dtoList.stream().map(SysMenuManageConverter.INSTANCE::toAssignedPermissionRow).toList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replaceMenuPermissions(Long menuId, SysMenuAssignPermissionForm form) {
		List<String> permissionCodes = CollUtil.emptyIfNull(form.getPermissionCodes());

		sysMenuPermissionMapper.deleteByMenuId(menuId);
		if (CollUtil.isNotEmpty(permissionCodes)) {
			sysMenuPermissionMapper.batchInsertMenuPermissions(menuId, permissionCodes);
		}
	}

}
