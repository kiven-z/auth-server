package com.auth.service.system.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.admin.convert.SysMenuManageConverter;
import com.auth.service.system.admin.mapper.SysMenuMapper;
import com.auth.service.system.admin.mapper.SysMenuPermissionMapper;
import com.auth.service.system.admin.mapper.SysMenuRoleMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignPermissionForm;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignRoleForm;
import com.auth.service.system.admin.model.po.menu.SysMenuAssignedPermissionPO;
import com.auth.service.system.admin.model.po.menu.SysMenuAssignedRolePO;
import com.auth.service.system.admin.model.vo.menu.SysMenuAssignedPermissionRow;
import com.auth.service.system.admin.model.vo.menu.SysMenuAssignedRoleRow;
import com.auth.service.system.admin.service.SysMenuManageService;
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

	public SysMenuManageServiceImpl(SysMenuRoleMapper sysMenuRoleMapper,
			SysMenuPermissionMapper sysMenuPermissionMapper) {
		this.sysMenuRoleMapper = sysMenuRoleMapper;
		this.sysMenuPermissionMapper = sysMenuPermissionMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysMenuAssignedRoleRow> listAssignedRoles(Long menuId) {
		List<SysMenuAssignedRolePO> dtoList = sysMenuRoleMapper.selectAssignedRolesByMenuId(menuId);

		return SysMenuManageConverter.INSTANCE.toAssignedRoleRowList(dtoList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replaceMenuRoles(Long menuId, SysMenuAssignRoleForm form) {
		List<String> roleCodes = form.getRoleCodes();
		sysMenuRoleMapper.deleteByMenuId(menuId);

		if (CollUtil.isEmpty(roleCodes)) {
			return;
		}

		sysMenuRoleMapper.batchInsertMenuRoles(menuId, roleCodes);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysMenuAssignedPermissionRow> listPermissionsWithAssignFlags(Long menuId) {
		List<SysMenuAssignedPermissionPO> dtoList = sysMenuPermissionMapper.selectAssignedPermissionByMenuId(menuId);

		return SysMenuManageConverter.INSTANCE.toAssignedPermissionRowList(dtoList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replaceMenuPermissions(Long menuId, SysMenuAssignPermissionForm form) {
		List<String> permissionCodes = form.getPermissionCodes();
		sysMenuPermissionMapper.deleteByMenuId(menuId);

		if (CollUtil.isEmpty(permissionCodes)) {
			return;
		}
		sysMenuPermissionMapper.batchInsertMenuPermissions(menuId, permissionCodes);
	}

}
