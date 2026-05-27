package com.auth.service.system.admin.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.SysRoleConverter;
import com.auth.service.system.admin.convert.SysRoleDetailVoConverter;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.GrantTableMapper;
import com.auth.service.system.admin.mapper.SysRoleCascadeMapper;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.mapper.SysRolePermissionMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleCreateForm;
import com.auth.service.system.admin.model.form.role.SysRoleUpdateForm;
import com.auth.service.system.admin.model.query.SysRoleQuery;
import com.auth.service.system.admin.model.vo.role.SysRoleDetailVO;
import com.auth.service.system.admin.model.vo.role.SysRoleOptionVO;
import com.auth.service.system.admin.model.vo.role.SysRolePageResponse;
import com.auth.service.system.admin.service.SysRoleService;
import com.auth.service.system.admin.support.sqlbuild.SysRolePageOrderSqlBuilder;
import com.auth.service.system.authorization.dispatch.RoleAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.common.support.FieldChangeSupport;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 系统角色服务实现
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final GrantTableMapper grantTableMapper;

	private final SysRolePermissionMapper sysRolePermissionMapper;

	private final SysRoleCascadeMapper sysRoleCascadeMapper;

	private final RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	public SysRoleServiceImpl(AuditUserDisplayService auditUserDisplayService, GrantTableMapper grantTableMapper,
			SysRolePermissionMapper sysRolePermissionMapper, SysRoleCascadeMapper sysRoleCascadeMapper,
			RoleAuthorizationInvalidationTrigger roleInvalidationTrigger) {
		this.auditUserDisplayService = auditUserDisplayService;
		this.grantTableMapper = grantTableMapper;
		this.sysRolePermissionMapper = sysRolePermissionMapper;
		this.sysRoleCascadeMapper = sysRoleCascadeMapper;
		this.roleInvalidationTrigger = roleInvalidationTrigger;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysRolePageResponse> getPage(Page<SysRoleEntity> pageParams, SysRoleQuery query) {
		String orderBySql = SysRolePageOrderSqlBuilder.buildOrderBySql(query.getSort());
		IPage<SysRoleEntity> entityPage = baseMapper.selectListByPage(pageParams, query, orderBySql);
		IPage<SysRolePageResponse> page = entityPage.convert(SysRoleConverter.INSTANCE::toPageResponse);

		auditUserDisplayService.enrichAuditUsernames(page);
		return PageResponse.of(page);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysRoleDetailVO getDetail(Long id) {
		SysRoleEntity entity = baseMapper.selectById(id);
		if (entity == null) {
			throw new SystemBusinessException(SystemAdminResultCode.ROLE_NOT_FOUND, id);
		}

		return SysRoleDetailVoConverter.toDetailVo(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(SysRoleCreateForm form) {
		String roleCode = CharSequenceUtil.trim(form.getRoleCode());

		// 检查角色编码是否重复
		long exists = count(Wrappers.<SysRoleEntity>lambdaQuery().eq(SysRoleEntity::getRoleCode, roleCode));
		if (exists > 0) {
			throw new SystemBusinessException(SystemAdminResultCode.ROLE_CODE_DUPLICATE, roleCode);
		}

		// 创建角色
		SysRoleEntity entity = SysRoleConverter.INSTANCE.toEntity(form);
		entity.setRoleCode(roleCode);

		save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(SysRoleUpdateForm form) {
		// 检查角色是否存在
		Long id = form.getId();
		SysRoleEntity existing = baseMapper.selectById(id);
		if (existing == null) {
			throw new SystemBusinessException(SystemAdminResultCode.ROLE_NOT_FOUND, id);
		}

		// 检查角色编码是否重复
		String oldRoleCode = existing.getRoleCode();
		String newRoleCode = CharSequenceUtil.trim(form.getRoleCode());
		Boolean oldStatus = existing.getStatus();
		Boolean newStatus = form.getStatus();
		// 检查角色编码是否重复
		long duplicate = count(Wrappers.<SysRoleEntity>lambdaQuery()
			.eq(SysRoleEntity::getRoleCode, newRoleCode)
			.ne(SysRoleEntity::getId, existing.getId()));
		if (duplicate > 0) {
			throw new SystemBusinessException(SystemAdminResultCode.ROLE_CODE_DUPLICATE, newRoleCode);
		}

		// 检查角色编码是否变更
		boolean roleCodeChanged = FieldChangeSupport.codeChanged(oldRoleCode, newRoleCode);
		boolean statusChanged = FieldChangeSupport.valueChanged(oldStatus, newStatus);

		// 更新角色
		SysRoleConverter.INSTANCE.applyUpdateForm(form, existing);
		existing.setRoleCode(newRoleCode);
		updateById(existing);

		// 检查角色编码是否变更
		if (roleCodeChanged) {
			cascadeRoleCodeRename(oldRoleCode, newRoleCode);
		}

		// 检查角色状态是否变更
		if (FieldChangeSupport.anyChanged(roleCodeChanged, statusChanged)) {
			roleInvalidationTrigger.submitByRoleCodes(FieldChangeSupport.renameCodes(oldRoleCode, newRoleCode),
					"update:" + newRoleCode);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteById(Long id) {
		SysRoleEntity existing = baseMapper.selectById(id);
		if (existing == null) {
			throw new SystemBusinessException(SystemAdminResultCode.ROLE_NOT_FOUND, id);
		}

		// 检查角色是否被 grant_table 引用
		String roleCode = existing.getRoleCode();
		long grantRefs = grantTableMapper.countByRoleCode(roleCode);
		if (grantRefs > 0) {
			throw new SystemBusinessException(SystemAdminResultCode.ROLE_IN_USE, roleCode);
		}

		// 删除角色权限
		sysRolePermissionMapper.deleteByRoleCode(roleCode);
		sysRoleCascadeMapper.deleteMenuRolesByRoleCode(roleCode);
		removeById(id);

		// 触发授权失效
		roleInvalidationTrigger.submitByRoleCodes(List.of(roleCode), "delete:" + roleCode);
		log.info("Role deleted: id={}, roleCode={}", id, roleCode);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysRoleOptionVO> listOptions(String roleName, String roleCode) {
		return SysRoleConverter.INSTANCE.toOptionVoList(baseMapper.selectRoleOptions(roleName, roleCode));
	}

	/**
	 * {@code role_code} 变更时级联更新关联表
	 * @param oldRoleCode 原编码
	 * @param newRoleCode 新编码
	 */
	private void cascadeRoleCodeRename(String oldRoleCode, String newRoleCode) {
		sysRoleCascadeMapper.updateGrantTableRoleCode(oldRoleCode, newRoleCode);
		sysRoleCascadeMapper.updateRolePermissionRoleCode(oldRoleCode, newRoleCode);
		sysRoleCascadeMapper.updateMenuRoleRoleCode(oldRoleCode, newRoleCode);
		log.info("Role code cascaded: {} -> {}", oldRoleCode, newRoleCode);
	}

}
