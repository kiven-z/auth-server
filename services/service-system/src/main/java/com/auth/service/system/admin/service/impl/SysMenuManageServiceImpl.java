package com.auth.service.system.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.auth.service.system.admin.mapper.*;
import com.auth.service.system.admin.model.entity.*;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignPermissionForm;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignRoleForm;
import com.auth.service.system.admin.model.form.menu.SysMenuBatchStatusForm;
import com.auth.service.system.admin.model.vo.menu.SysMenuAssignedPermissionRow;
import com.auth.service.system.admin.model.vo.menu.SysMenuAssignedRoleRow;
import com.auth.service.system.admin.service.SysMenuManageService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.SystemResultCodeEnum;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

	private final SysRoleMapper sysRoleMapper;

	private final SysPermissionMapper sysPermissionMapper;

	public SysMenuManageServiceImpl(SysMenuRoleMapper sysMenuRoleMapper,
			SysMenuPermissionMapper sysMenuPermissionMapper, SysRoleMapper sysRoleMapper,
			SysPermissionMapper sysPermissionMapper) {
		this.sysMenuRoleMapper = sysMenuRoleMapper;
		this.sysMenuPermissionMapper = sysMenuPermissionMapper;
		this.sysRoleMapper = sysRoleMapper;
		this.sysPermissionMapper = sysPermissionMapper;
	}

	/**
	 * 批量更新状态（0=启用 1=禁用）
	 * @param form 请求体
	 */
	@Override
	public void batchUpdateStatus(SysMenuBatchStatusForm form) {
		boolean enabled = form.getStatus() != null && form.getStatus() == 0;
		for (String idStr : form.getIds()) {
			Long id = Long.valueOf(idStr);
			requireMenu(id);
			lambdaUpdate().eq(SysMenuEntity::getId, id).set(SysMenuEntity::getStatus, enabled).update();
		}
	}

	/**
	 * 当前菜单已分配角色（供前端与角色分页合并展示）
	 * @param menuId 菜单主键
	 * @return 已分配行，{@code assigned} 恒为 true
	 */
	@Override
	public List<SysMenuAssignedRoleRow> listAssignedRoles(Long menuId) {
		requireMenu(menuId);
		List<SysMenuRoleEntity> links = sysMenuRoleMapper
			.selectList(Wrappers.<SysMenuRoleEntity>lambdaQuery().eq(SysMenuRoleEntity::getMenuId, menuId));
		List<SysMenuAssignedRoleRow> rows = new ArrayList<>();
		for (SysMenuRoleEntity link : links) {
			SysRoleEntity role = sysRoleMapper.selectOne(Wrappers.<SysRoleEntity>lambdaQuery()
				.eq(SysRoleEntity::getRoleCode, link.getRoleCode())
				.last("LIMIT 1"));
			String name = role != null ? role.getRoleName() : link.getRoleCode();
			rows.add(new SysMenuAssignedRoleRow(link.getRoleCode(), name, true));
		}
		return rows;
	}

	/**
	 * 覆盖写入菜单-角色关联
	 * @param menuId 菜单主键
	 * @param form 角色编码列表
	 */
	@Override
	public void replaceMenuRoles(Long menuId, SysMenuAssignRoleForm form) {
		requireMenu(menuId);
		List<String> codes = form.getRoleCodes() == null ? List.of() : form.getRoleCodes();
		validateRoleCodes(codes);
		sysMenuRoleMapper.delete(Wrappers.<SysMenuRoleEntity>lambdaQuery().eq(SysMenuRoleEntity::getMenuId, menuId));
		for (String code : codes) {
			SysMenuRoleEntity row = new SysMenuRoleEntity();
			row.setMenuId(menuId);
			row.setRoleCode(code);
			sysMenuRoleMapper.insert(row);
		}
	}

	/**
	 * 全量权限列表并标记当前菜单是否已关联（供分配抽屉）
	 * @param menuId 菜单主键
	 * @return 权限行
	 */
	@Override
	public List<SysMenuAssignedPermissionRow> listPermissionsWithAssignFlags(Long menuId) {
		requireMenu(menuId);
		Set<String> assignedCodes = sysMenuPermissionMapper
			.selectList(Wrappers.<SysMenuPermissionEntity>lambdaQuery().eq(SysMenuPermissionEntity::getMenuId, menuId))
			.stream()
			.map(SysMenuPermissionEntity::getPermissionCode)
			.collect(Collectors.toSet());
		List<SysPermissionEntity> all = sysPermissionMapper.selectList(Wrappers.<SysPermissionEntity>lambdaQuery()
			.eq(SysPermissionEntity::getStatus, Boolean.TRUE)
			.orderByAsc(SysPermissionEntity::getPermissionCode));
		List<SysMenuAssignedPermissionRow> rows = new ArrayList<>();
		for (SysPermissionEntity p : all) {
			boolean assigned = assignedCodes.contains(p.getPermissionCode());
			rows.add(new SysMenuAssignedPermissionRow(p.getPermissionCode(), p.getPermissionName(), assigned));
		}
		return rows;
	}

	/**
	 * 覆盖写入菜单-权限关联
	 * @param menuId 菜单主键
	 * @param form 权限编码列表
	 */
	@Override
	public void replaceMenuPermissions(Long menuId, SysMenuAssignPermissionForm form) {
		requireMenu(menuId);
		List<String> codes = form.getPermissionCodes() == null ? List.of() : form.getPermissionCodes();
		validatePermissionCodes(codes);
		sysMenuPermissionMapper
			.delete(Wrappers.<SysMenuPermissionEntity>lambdaQuery().eq(SysMenuPermissionEntity::getMenuId, menuId));
		for (String code : codes) {
			SysMenuPermissionEntity row = new SysMenuPermissionEntity();
			row.setMenuId(menuId);
			row.setPermissionCode(code);
			sysMenuPermissionMapper.insert(row);
		}
	}

	private void validateRoleCodes(List<String> codes) {
		for (String code : codes) {
			if (StrUtil.isBlank(code)) {
				continue;
			}
			Long cnt = sysRoleMapper
				.selectCount(Wrappers.<SysRoleEntity>lambdaQuery().eq(SysRoleEntity::getRoleCode, code));
			if (cnt == null || cnt == 0L) {
				throw new SystemBusinessException(SystemResultCodeEnum.MENU_ROLE_CODE_INVALID, code);
			}
		}
	}

	private void validatePermissionCodes(List<String> codes) {
		for (String code : codes) {
			if (StrUtil.isBlank(code)) {
				continue;
			}
			Long cnt = sysPermissionMapper.selectCount(
					Wrappers.<SysPermissionEntity>lambdaQuery().eq(SysPermissionEntity::getPermissionCode, code));
			if (cnt == null || cnt == 0L) {
				throw new SystemBusinessException(SystemResultCodeEnum.MENU_PERMISSION_CODE_INVALID, code);
			}
		}
	}

	private void requireMenu(Long id) {
		SysMenuEntity entity = getById(id);
		if (entity == null) {
			throw new SystemBusinessException(SystemResultCodeEnum.MENU_NOT_FOUND, id);
		}
	}

}
