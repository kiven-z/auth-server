package com.auth.service.system.admin.service.admin.impl;

import java.util.Collections;
import java.util.List;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.admin.ReferenceConverter;
import com.auth.service.system.admin.convert.admin.SysPermissionConverter;
import com.auth.service.system.admin.mapper.permission.SysPermissionMapper;
import com.auth.service.system.admin.mapper.role.SysRolePermissionMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.query.permission.SysPermissionQuery;
import com.auth.service.system.admin.model.vo.permission.SysPermissionDetailVO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionPageVO;
import com.auth.service.system.admin.service.admin.SysPermissionQueryService;
import com.auth.service.system.admin.support.sqlbuild.SysPermissionPageOrderSqlBuilder;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统权限只读查询实现。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(readOnly = true)
public class SysPermissionQueryServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermissionEntity>
		implements SysPermissionQueryService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final SysRolePermissionMapper sysRolePermissionMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysPermissionPageVO> getPage(Page<SysPermissionEntity> pageParams, SysPermissionQuery query) {
		String orderBySql = SysPermissionPageOrderSqlBuilder.buildOrderBySql(query.getSort());
		IPage<SysPermissionEntity> entityPage = baseMapper.selectListByPage(pageParams, query, orderBySql);
		IPage<SysPermissionPageVO> page = entityPage.convert(SysPermissionConverter.INSTANCE::toPageVo);

		auditUserDisplayService.enrichAuditUsernames(page, null, null);
		return PageResponse.of(page);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysPermissionDetailVO getDetail(Long id) {
		SysPermissionEntity entity = baseMapper.selectById(id);
		if (entity == null) {
			log.warn("permission not found: id={}", id);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST);
		}

		SysPermissionDetailVO detail = SysPermissionConverter.INSTANCE.toDetailVo(entity);
		List<RoleReferencePO> boundRoles = sysRolePermissionMapper.selectBoundRolesByPermissionId(id);
		detail.setBoundRoles(ReferenceConverter.INSTANCE.toRoleReferenceList(boundRoles));

		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail), null, null);
		return detail;
	}

}
