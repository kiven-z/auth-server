package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.SysPermissionConverter;
import com.auth.service.system.admin.mapper.SysPermissionMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.form.permission.SysPermissionCreateForm;
import com.auth.service.system.admin.model.query.SysPermissionQuery;
import com.auth.service.system.admin.model.vo.permission.SysPermissionPageResponse;
import com.auth.service.system.admin.service.SysPermissionService;
import com.auth.service.system.admin.support.SysPermissionPageOrderSqlBuilder;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.SystemResultCodeEnum;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	public SysPermissionServiceImpl(AuditUserDisplayService auditUserDisplayService) {
		this.auditUserDisplayService = auditUserDisplayService;
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
	public void create(SysPermissionCreateForm form) {
		String permissionCode = form.getPermissionCode();
		long exists = count(
				Wrappers.<SysPermissionEntity>lambdaQuery().eq(SysPermissionEntity::getPermissionCode, permissionCode));
		if (exists > 0) {
			throw new SystemBusinessException(SystemResultCodeEnum.PERMISSION_CODE_DUPLICATE, permissionCode);
		}

		SysPermissionEntity entity = SysPermissionConverter.INSTANCE.toEntity(form);
		if (entity.getOrderNum() == null) {
			entity.setOrderNum(0);
		}
		if (entity.getStatus() == null) {
			entity.setStatus(Boolean.TRUE);
		}
		save(entity);
	}

}
