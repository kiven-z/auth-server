package com.auth.service.system.admin.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

		long exists = count(Wrappers.<SysRoleEntity>lambdaQuery().eq(SysRoleEntity::getRoleCode, roleCode));
		if (exists > 0) {
			throw new SystemBusinessException(SystemAdminResultCode.ROLE_CODE_DUPLICATE, roleCode);
		}

		SysRoleEntity entity = SysRoleConverter.INSTANCE.toEntity(form);
		entity.setRoleCode(roleCode);
		if (entity.getOrderNum() == null) {
			entity.setOrderNum(0);
		}
		save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(SysRoleUpdateForm form) {
		SysRoleEntity existing = baseMapper.selectById(form.getId());
		if (existing == null) {
			throw new SystemBusinessException(SystemAdminResultCode.ROLE_NOT_FOUND, form.getId());
		}

		String oldRoleCode = existing.getRoleCode();
		String newRoleCode = CharSequenceUtil.trim(form.getRoleCode());
		Boolean oldStatus = existing.getStatus();
		Boolean newStatus = form.getStatus();

		boolean roleCodeChanged = !CharSequenceUtil.equals(oldRoleCode, newRoleCode);
		if (roleCodeChanged) {
			long duplicate = count(Wrappers.<SysRoleEntity>lambdaQuery()
				.eq(SysRoleEntity::getRoleCode, newRoleCode)
				.ne(SysRoleEntity::getId, existing.getId()));
			if (duplicate > 0) {
				throw new SystemBusinessException(SystemAdminResultCode.ROLE_CODE_DUPLICATE, newRoleCode);
			}
		}

		SysRoleConverter.INSTANCE.applyUpdateForm(form, existing);
		existing.setRoleCode(newRoleCode);
		if (existing.getOrderNum() == null) {
			existing.setOrderNum(0);
		}
		updateById(existing);

		if (roleCodeChanged) {
			cascadeRoleCodeRename(oldRoleCode, newRoleCode);
		}

		if (shouldInvalidateAfterUpdate(oldRoleCode, newRoleCode, oldStatus, newStatus)) {
			List<String> invalidateCodes = new ArrayList<>();
			invalidateCodes.add(oldRoleCode);
			if (!CharSequenceUtil.equals(oldRoleCode, newRoleCode)) {
				invalidateCodes.add(newRoleCode);
			}
			roleInvalidationTrigger.submitByRoleCodes(invalidateCodes, "update:" + newRoleCode);
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

		String roleCode = existing.getRoleCode();
		long grantRefs = grantTableMapper.countByRoleCode(roleCode);
		if (grantRefs > 0) {
			throw new SystemBusinessException(SystemAdminResultCode.ROLE_IN_USE, roleCode);
		}

		sysRolePermissionMapper.deleteByRoleCode(roleCode);
		sysRoleCascadeMapper.deleteMenuRolesByRoleCode(roleCode);
		removeById(id);

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
	 * 判断是否需在更新后触发授权失效：role_code 或 status 变化时触发；仅改名称/排序/备注不触发。
	 */
	private static boolean shouldInvalidateAfterUpdate(String oldRoleCode, String newRoleCode, Boolean oldStatus,
			Boolean newStatus) {
		if (!CharSequenceUtil.equals(oldRoleCode, newRoleCode)) {
			return true;
		}
		return !Objects.equals(oldStatus, newStatus);
	}

	/**
	 * {@code role_code} 变更时级联更新关联表（与文档列出的 grant / 权限 / 菜单角色一致）。
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
