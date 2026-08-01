package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.service.system.admin.convert.admin.role.SysRoleScopeConverter;
import com.auth.service.system.admin.mapper.role.RoleScopeMapper;
import com.auth.service.system.admin.model.entity.RoleScopeEntity;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleScopeForm;
import com.auth.service.system.admin.model.vo.role.SysRoleScopeVO;
import com.auth.service.system.admin.service.admin.SysRoleScopeService;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.admin.support.grant.RbacReferenceChecker;
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
 * 角色数据范围服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SysRoleScopeServiceImpl extends ServiceImpl<RoleScopeMapper, RoleScopeEntity>
		implements SysRoleScopeService {

	private final RbacReferenceChecker rbacReferenceChecker;

	private final DeptReferenceChecker deptReferenceChecker;

	private final RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysRoleScopeVO getByRoleId(Long roleId) {
		rbacReferenceChecker.getExisting(roleId);
		RoleScopeEntity entity = super.getOne(
				Wrappers.<RoleScopeEntity>lambdaQuery().eq(RoleScopeEntity::getRoleId, roleId));
		return SysRoleScopeConverter.toVo(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void upsert(Long roleId, SysRoleScopeForm form) {
		SysRoleEntity role = rbacReferenceChecker.getExisting(roleId);
		DataScopeStorageType scopeType = DataScopeStorageType.parse(form.getScopeType());
		if (scopeType == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID);
		}
		List<Long> scopeDeptIds = normalizeDeptIds(scopeType, form.getScopeDeptIds());

		RoleScopeEntity existing = super.getOne(
				Wrappers.<RoleScopeEntity>lambdaQuery().eq(RoleScopeEntity::getRoleId, roleId));
		RoleScopeEntity entity = existing != null ? existing : new RoleScopeEntity();
		if (existing == null) {
			entity.setRoleId(roleId);
		}
		SysRoleScopeConverter.applyForm(entity, form, scopeType.name(), scopeDeptIds);
		super.saveOrUpdate(entity);

		roleInvalidationTrigger.submitByRoleCodes(List.of(role.getRoleCode()), "upsert-role-scope");
	}

	/**
	 * 规范化部门 ID：DEPT* 必填且须存在；ALL/SELF 清空
	 * @param scopeType 范围类型
	 * @param rawDeptIds 表单部门 ID
	 * @return 去重后的部门 ID；非部门维为空列表
	 */
	private List<Long> normalizeDeptIds(DataScopeStorageType scopeType, List<Long> rawDeptIds) {
		if (scopeType != DataScopeStorageType.DEPT && scopeType != DataScopeStorageType.DEPT_AND_CHILD) {
			return List.of();
		}
		if (CollUtil.isEmpty(rawDeptIds)) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED);
		}
		List<Long> distinctIds = rawDeptIds.stream().distinct().toList();
		deptReferenceChecker.requireExistingDeptIds(distinctIds);
		return distinctIds;
	}

}
