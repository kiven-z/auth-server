package com.auth.service.system.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.SysPermissionConverter;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.admin.mapper.SysMenuPermissionMapper;
import com.auth.service.system.admin.mapper.SysPermissionMapper;
import com.auth.service.system.admin.mapper.SysRolePermissionMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.enums.PermissionTypeEnum;
import com.auth.service.system.admin.model.form.permission.SysPermissionCreateForm;
import com.auth.service.system.admin.model.form.permission.SysPermissionUpdateForm;
import com.auth.service.system.admin.model.po.permission.SysPermissionBoundRolePO;
import com.auth.service.system.admin.model.query.SysPermissionQuery;
import com.auth.service.system.admin.model.vo.permission.SysPermissionDetailVO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionPageResponse;
import com.auth.service.system.admin.service.SysPermissionService;
import com.auth.service.system.admin.support.sqlbuild.SysPermissionPageOrderSqlBuilder;
import com.auth.service.system.authorization.dispatch.trigger.PermissionAuthorizationInvalidationTrigger;
import com.auth.service.system.authorization.dispatch.trigger.RoleAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.common.support.BusinessKeyAssert;
import com.auth.service.system.common.support.FieldChangeSupport;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 系统权限服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermissionEntity>
		implements SysPermissionService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final PermissionAuthorizationInvalidationTrigger permissionInvalidationTrigger;

	private final RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	private final SysRolePermissionMapper sysRolePermissionMapper;

	private final SysMenuPermissionMapper sysMenuPermissionMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysPermissionPageResponse> getPage(Page<SysPermissionEntity> pageParams,
			SysPermissionQuery query) {
		String orderBySql = SysPermissionPageOrderSqlBuilder.buildOrderBySql(query.getSort());
		IPage<SysPermissionEntity> entityPage = baseMapper.selectListByPage(pageParams, query, orderBySql);
		IPage<SysPermissionPageResponse> page = entityPage.convert(SysPermissionConverter.INSTANCE::toPageResponse);

		auditUserDisplayService.enrichAuditUsernames(page);
		return PageResponse.of(page);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysPermissionDetailVO getDetail(Long id) {
		SysPermissionEntity entity = baseMapper.selectById(id);
		if (entity == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST, id);
		}

		SysPermissionDetailVO detail = SysPermissionConverter.INSTANCE.toDetailVo(entity);

		// 查询绑定的角色和菜单
		String permissionCode = entity.getPermissionCode();
		detail.setBoundRoles(SysPermissionConverter.INSTANCE
			.toBoundRoleItemList(sysRolePermissionMapper.selectBoundRolesByPermissionCode(permissionCode)));
		if (PermissionTypeEnum.BUTTON.getCode().equals(entity.getPermissionType())) {
			detail.setBoundMenus(SysPermissionConverter.INSTANCE
				.toBoundMenuItemList(sysMenuPermissionMapper.selectBoundMenusByPermissionCode(permissionCode)));
		}
		else {
			detail.setBoundMenus(List.of());
		}

		// 关联用户
		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail));
		return detail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(SysPermissionCreateForm form) {
		String permissionCode = form.getPermissionCode();
		long exists = count(
				Wrappers.<SysPermissionEntity>lambdaQuery().eq(SysPermissionEntity::getPermissionCode, permissionCode));
		if (exists > 0) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_CODE_DUPLICATE, permissionCode);
		}

		SysPermissionEntity entity = SysPermissionConverter.INSTANCE.toEntity(form);
		save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(SysPermissionUpdateForm form) {
		Long id = form.getId();
		SysPermissionEntity existing = baseMapper.selectById(id);
		if (existing == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST, id);
		}

		// 新旧权限和状态
		String oldPermissionCode = existing.getPermissionCode();
		String newPermissionCode = form.getPermissionCode();
		Boolean oldStatus = existing.getStatus();
		Boolean newStatus = form.getStatus();

		// 只要权限码或状态更新
		boolean permissionCodeChanged = FieldChangeSupport.codeChanged(oldPermissionCode, newPermissionCode);
		boolean statusChanged = FieldChangeSupport.valueChanged(oldStatus, newStatus);
		if (permissionCodeChanged) {
			BusinessKeyAssert.requireAbsent(this,
					Wrappers.<SysPermissionEntity>lambdaQuery()
						.eq(SysPermissionEntity::getPermissionCode, newPermissionCode)
						.ne(SysPermissionEntity::getId, existing.getId()),
					SystemCommonResultCode.DATA_CODE_DUPLICATE, newPermissionCode);
		}

		SysPermissionConverter.INSTANCE.applyUpdateForm(form, existing);
		existing.setPermissionCode(newPermissionCode);
		updateById(existing);

		// 权限码或启用状态影响角色权限展开，其余元数据变更不刷新画像
		if (FieldChangeSupport.anyChanged(permissionCodeChanged, statusChanged)) {
			List<String> permissionCodes = FieldChangeSupport.renameCodes(oldPermissionCode, newPermissionCode);
			permissionInvalidationTrigger.submitByPermissionCodes(permissionCodes, "update");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteById(Long id) {
		SysPermissionEntity existing = baseMapper.selectById(id);
		if (existing == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST, id);
		}

		// 失效在事务提交后投递：PERMISSION 桥接依赖 sys_role_permission，须在删除前快照角色并走 ROLE 失效
		String permissionCode = existing.getPermissionCode();
		permissionInvalidationTrigger.submitByPermissionCodes(List.of(permissionCode), "delete");

		// 提交 ROLE 失效
		List<String> boundRoleCodes = sysRolePermissionMapper.selectBoundRolesByPermissionCode(permissionCode)
			.stream()
			.map(SysPermissionBoundRolePO::getRoleCode)
			.filter(CharSequenceUtil::isNotBlank)
			.distinct()
			.toList();
		if (CollUtil.isNotEmpty(boundRoleCodes)) {
			roleInvalidationTrigger.submitByRoleCodes(boundRoleCodes, "delete-permission");
		}

		// 删除权限
		removeById(id);
		log.info("Permission deleted: id={}, permissionCode={}", id, permissionCode);
	}

}
