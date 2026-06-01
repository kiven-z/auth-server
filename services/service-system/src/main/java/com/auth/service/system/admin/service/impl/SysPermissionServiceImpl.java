package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.SysPermissionConverter;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysMenuPermissionMapper;
import com.auth.service.system.admin.mapper.SysPermissionMapper;
import com.auth.service.system.admin.mapper.SysRolePermissionMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.enums.PermissionTypeEnum;
import com.auth.service.system.admin.model.form.permission.SysPermissionCreateForm;
import com.auth.service.system.admin.model.query.SysPermissionQuery;
import com.auth.service.system.admin.model.vo.permission.SysPermissionDetailVO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionPageResponse;
import com.auth.service.system.admin.service.SysPermissionService;
import com.auth.service.system.admin.support.sqlbuild.SysPermissionPageOrderSqlBuilder;
import com.auth.service.system.authorization.dispatch.trigger.PermissionAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermissionEntity>
		implements SysPermissionService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final PermissionAuthorizationInvalidationTrigger permissionInvalidationTrigger;

	private final SysRolePermissionMapper sysRolePermissionMapper;

	private final SysMenuPermissionMapper sysMenuPermissionMapper;

	public SysPermissionServiceImpl(AuditUserDisplayService auditUserDisplayService,
			PermissionAuthorizationInvalidationTrigger permissionInvalidationTrigger,
			SysRolePermissionMapper sysRolePermissionMapper, SysMenuPermissionMapper sysMenuPermissionMapper) {
		this.auditUserDisplayService = auditUserDisplayService;
		this.permissionInvalidationTrigger = permissionInvalidationTrigger;
		this.sysRolePermissionMapper = sysRolePermissionMapper;
		this.sysMenuPermissionMapper = sysMenuPermissionMapper;
	}

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
			throw new SystemBusinessException(SystemAdminResultCode.PERMISSION_NOT_FOUND, id);
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
			throw new SystemBusinessException(SystemAdminResultCode.PERMISSION_CODE_DUPLICATE, permissionCode);
		}

		SysPermissionEntity entity = SysPermissionConverter.INSTANCE.toEntity(form);
		save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteById(Long id) {
		SysPermissionEntity existing = baseMapper.selectById(id);
		if (existing == null) {
			throw new SystemBusinessException(SystemAdminResultCode.PERMISSION_NOT_FOUND, id);
		}

		String permissionCode = existing.getPermissionCode();
		removeById(id);

		permissionInvalidationTrigger.submitByPermissionCodes(List.of(permissionCode), "delete:" + permissionCode);
		log.info("Permission deleted: id={}, permissionCode={}", id, permissionCode);
	}

}
