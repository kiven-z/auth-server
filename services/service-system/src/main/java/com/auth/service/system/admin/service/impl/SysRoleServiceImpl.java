package com.auth.service.system.admin.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.SysRoleConverter;
import com.auth.service.system.admin.convert.SysRoleDetailVoConverter;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleCreateForm;
import com.auth.service.system.admin.model.form.role.SysRoleUpdateForm;
import com.auth.service.system.admin.model.query.SysRoleQuery;
import com.auth.service.system.admin.model.vo.role.SysRoleDetailVO;
import com.auth.service.system.admin.model.vo.role.SysRoleOptionVO;
import com.auth.service.system.admin.model.vo.role.SysRolePageResponse;
import com.auth.service.system.admin.service.SysRoleService;
import com.auth.service.system.admin.support.sqlbuild.SysRolePageOrderSqlBuilder;
import com.auth.service.system.authorization.dispatch.trigger.RoleAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.common.support.BusinessKeyAssert;
import com.auth.service.system.common.support.FieldChangeSupport;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

	private final RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	public SysRoleServiceImpl(AuditUserDisplayService auditUserDisplayService,
			RoleAuthorizationInvalidationTrigger roleInvalidationTrigger) {
		this.auditUserDisplayService = auditUserDisplayService;
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

		BusinessKeyAssert.requireAbsent(this,
				Wrappers.<SysRoleEntity>lambdaQuery().eq(SysRoleEntity::getRoleCode, roleCode),
				SystemAdminResultCode.ROLE_CODE_DUPLICATE, roleCode);

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

		// 检查角色编码是否变更
		String oldRoleCode = existing.getRoleCode();
		String newRoleCode = CharSequenceUtil.trim(form.getRoleCode());
		Boolean oldStatus = existing.getStatus();
		Boolean newStatus = form.getStatus();

		// 检查角色编码是否变更
		boolean roleCodeChanged = FieldChangeSupport.codeChanged(oldRoleCode, newRoleCode);
		boolean statusChanged = FieldChangeSupport.valueChanged(oldStatus, newStatus);
		if (roleCodeChanged) {
			BusinessKeyAssert.requireAbsent(this,
					Wrappers.<SysRoleEntity>lambdaQuery()
						.eq(SysRoleEntity::getRoleCode, newRoleCode)
						.ne(SysRoleEntity::getId, existing.getId()),
					SystemAdminResultCode.ROLE_CODE_DUPLICATE, newRoleCode);
		}

		// 更新角色
		SysRoleConverter.INSTANCE.applyUpdateForm(form, existing);
		existing.setRoleCode(newRoleCode);
		updateById(existing);

		if (FieldChangeSupport.anyChanged(roleCodeChanged, statusChanged)) {
			List<String> roleCodes = FieldChangeSupport.renameCodes(oldRoleCode, newRoleCode);
			roleInvalidationTrigger.submitByRoleCodes(roleCodes, "update:" + newRoleCode);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysRoleOptionVO> listOptions(String roleName, String roleCode) {
		List<SysRoleEntity> entities = baseMapper.selectRoleOptions(roleName, roleCode);
		return SysRoleConverter.INSTANCE.toOptionVoList(entities);
	}

}
