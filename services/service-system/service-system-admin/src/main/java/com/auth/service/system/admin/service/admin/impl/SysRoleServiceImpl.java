package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.utils.FieldChangeSupport;
import com.auth.common.data.support.BusinessKeyAssert;
import com.auth.service.system.admin.convert.admin.SysRoleConverter;
import com.auth.service.system.admin.mapper.role.GrantTableMapper;
import com.auth.service.system.admin.mapper.role.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleForm;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.service.admin.SysRoleService;
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
 * 系统角色写服务实现。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleService {

	/**
	 * Excel 导入与批量新增时的持久化批次大小
	 */
	private static final int IMPORT_SAVE_BATCH_SIZE = 500;

	private final RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	private final GrantTableMapper grantTableMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void createBatchFromImport(List<SysRoleForm> forms) {
		if (CollUtil.isEmpty(forms)) {
			return;
		}

		List<String> roleCodes = forms.stream().map(SysRoleForm::getRoleCode).toList();
		List<String> distinctCodes = BusinessKeyAssert.requireDistinct(roleCodes,
				code -> new SystemBusinessException(SystemCommonResultCode.DATA_CODE_DUPLICATE, code));

		List<RoleReferencePO> existing = baseMapper.selectReferenceByRoleCodes(distinctCodes);
		if (CollUtil.isNotEmpty(existing)) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_CODE_DUPLICATE,
					existing.get(0).getRoleCode());
		}

		List<SysRoleEntity> entities = forms.stream().map(SysRoleConverter.INSTANCE::toEntity).toList();
		super.saveBatch(entities, IMPORT_SAVE_BATCH_SIZE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(SysRoleForm form) {
		Long id = form.getId();
		SysRoleEntity existing = baseMapper.selectById(id);
		if (existing == null) {
			log.warn("update role not found: id={}", id);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST);
		}

		boolean statusChanged = FieldChangeSupport.valueChanged(existing.getStatus(), form.getStatus());
		String oldRoleCode = existing.getRoleCode();
		String newRoleCode = form.getRoleCode();
		boolean roleCodeChanged = FieldChangeSupport.codeChanged(oldRoleCode, newRoleCode);
		if (roleCodeChanged) {
			BusinessKeyAssert.requireAbsent(baseMapper,
					Wrappers.<SysRoleEntity>lambdaQuery()
						.eq(SysRoleEntity::getRoleCode, newRoleCode)
						.ne(SysRoleEntity::getId, existing.getId()),
					() -> new SystemBusinessException(SystemCommonResultCode.DATA_CODE_DUPLICATE, newRoleCode));
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
	@Transactional(rollbackFor = Exception.class)
	public void deleteById(Long id) {
		SysRoleEntity existing = baseMapper.selectById(id);
		if (existing == null) {
			log.warn("delete role not found: id={}", id);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST);
		}

		String roleCode = existing.getRoleCode();
		if (grantTableMapper.countByRoleId(existing.getId()) > 0) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_IN_USE, roleCode);
		}

		removeById(id);

		roleInvalidationTrigger.submitByRoleCodes(List.of(roleCode), "delete");
	}

}
