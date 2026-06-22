package com.auth.service.system.admin.service.admin.impl;

import com.auth.service.system.admin.convert.sys.SysRoleConverter;
import com.auth.service.system.admin.mapper.role.GrantTableMapper;
import com.auth.service.system.admin.mapper.role.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleForm;
import com.auth.service.system.admin.service.admin.SysRoleService;
import com.auth.service.system.authorization.dispatch.trigger.RoleAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.support.BusinessKeyAssert;
import com.auth.service.system.common.support.FieldChangeSupport;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统角色写服务实现。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleService {

	private final RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	private final GrantTableMapper grantTableMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(SysRoleForm form) {
		String roleCode = form.getRoleCode();

		BusinessKeyAssert.requireAbsent(this,
				Wrappers.<SysRoleEntity>lambdaQuery().eq(SysRoleEntity::getRoleCode, roleCode),
				SystemCommonResultCode.DATA_CODE_DUPLICATE, roleCode);

		SysRoleEntity entity = SysRoleConverter.INSTANCE.toEntity(form);
		entity.setRoleCode(roleCode);

		save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(SysRoleForm form) {
		Long id = form.getId();
		SysRoleEntity existing = baseMapper.selectById(id);
		if (existing == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST, id);
		}

		boolean statusChanged = FieldChangeSupport.valueChanged(existing.getStatus(), form.getStatus());
		String oldRoleCode = existing.getRoleCode();
		String newRoleCode = form.getRoleCode();
		boolean roleCodeChanged = FieldChangeSupport.codeChanged(oldRoleCode, newRoleCode);
		if (roleCodeChanged) {
			BusinessKeyAssert.requireAbsent(this,
					Wrappers.<SysRoleEntity>lambdaQuery()
						.eq(SysRoleEntity::getRoleCode, newRoleCode)
						.ne(SysRoleEntity::getId, existing.getId()),
					SystemCommonResultCode.DATA_CODE_DUPLICATE, newRoleCode);
		}

		SysRoleConverter.INSTANCE.applyUpdateForm(form, existing);
		updateById(existing);

		if (FieldChangeSupport.anyChanged(roleCodeChanged, statusChanged)) {
			List<String> roleCodes = FieldChangeSupport.renameCodes(oldRoleCode, newRoleCode);
			roleInvalidationTrigger.submitByRoleCodes(roleCodes, "update");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteById(Long id) {
		SysRoleEntity existing = baseMapper.selectById(id);
		if (existing == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST, id);
		}

		String roleCode = existing.getRoleCode();
		if (grantTableMapper.countByRoleId(existing.getId()) > 0) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_IN_USE, roleCode);
		}

		removeById(id);

		roleInvalidationTrigger.submitByRoleCodes(List.of(roleCode), "delete");
	}

}
