package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.core.utils.FieldChangeSupport;
import com.auth.common.data.support.BusinessKeyAssert;
import com.auth.service.system.admin.convert.admin.SysPermissionConverter;
import com.auth.service.system.admin.mapper.permission.SysPermissionMapper;
import com.auth.service.system.admin.mapper.role.SysRolePermissionMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.form.permission.SysPermissionForm;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.service.admin.SysPermissionService;
import com.auth.service.system.authorization.dispatch.trigger.PermissionAuthorizationInvalidationTrigger;
import com.auth.service.system.authorization.dispatch.trigger.RoleAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统权限写服务实现。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermissionEntity>
		implements SysPermissionService {

	private final PermissionAuthorizationInvalidationTrigger permissionInvalidationTrigger;

	private final RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	private final SysRolePermissionMapper sysRolePermissionMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(SysPermissionForm form) {
		String permissionCode = form.getPermissionCode();
		long exists = count(Wrappers.lambdaQuery(SysPermissionEntity.class)
			.eq(SysPermissionEntity::getPermissionCode, permissionCode));
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
	public void update(SysPermissionForm form) {
		Long id = form.getId();
		SysPermissionEntity existing = baseMapper.selectById(id);
		if (existing == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST, id);
		}

		boolean statusChanged = FieldChangeSupport.valueChanged(existing.getStatus(), form.getStatus());
		String oldPermissionCode = existing.getPermissionCode();
		String newPermissionCode = form.getPermissionCode();
		boolean permissionCodeChanged = FieldChangeSupport.codeChanged(oldPermissionCode, newPermissionCode);
		if (permissionCodeChanged) {
			BusinessKeyAssert.requireAbsent(baseMapper,
					Wrappers.<SysPermissionEntity>lambdaQuery()
						.eq(SysPermissionEntity::getPermissionCode, newPermissionCode)
						.ne(SysPermissionEntity::getId, existing.getId()),
					() -> new SystemBusinessException(SystemCommonResultCode.DATA_CODE_DUPLICATE, newPermissionCode));
		}

		SysPermissionConverter.INSTANCE.applyUpdateForm(form, existing);
		existing.setPermissionCode(newPermissionCode);
		updateById(existing);

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

		String permissionCode = existing.getPermissionCode();
		permissionInvalidationTrigger.submitByPermissionCodes(List.of(permissionCode), "delete");

		List<String> boundRoleCodes = sysRolePermissionMapper.selectBoundRolesByPermissionId(id)
			.stream()
			.map(RoleReferencePO::getRoleCode)
			.filter(CharSequenceUtil::isNotBlank)
			.distinct()
			.toList();
		if (CollUtil.isNotEmpty(boundRoleCodes)) {
			roleInvalidationTrigger.submitByRoleCodes(boundRoleCodes, "delete-permission");
		}

		removeById(id);
	}

}
