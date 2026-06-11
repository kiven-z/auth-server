package com.auth.service.system.admin.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.UserDeptConverter;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.UserDeptMapper;
import com.auth.service.system.admin.model.entity.UserDeptEntity;
import com.auth.service.system.admin.model.enums.UserOrgRelationStatusEnum;
import com.auth.service.system.admin.model.form.userdept.UserDeptAssignForm;
import com.auth.service.system.admin.model.po.userdept.UserDeptPageRowPO;
import com.auth.service.system.admin.model.query.user.UserDeptPageQuery;
import com.auth.service.system.admin.model.vo.userdept.UserDeptPageVO;
import com.auth.service.system.admin.service.SysUserDeptService;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.admin.support.user.UserOperationChecker;
import com.auth.service.system.admin.support.user.UserOrgRelationBatchRemoveSupport;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 用户部门关联服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserDeptServiceImpl extends ServiceImpl<UserDeptMapper, UserDeptEntity> implements SysUserDeptService {

	private final UserReferenceChecker userReferenceChecker;

	private final UserOperationChecker userOperationChecker;

	private final DeptReferenceChecker deptReferenceChecker;

	private final UserAuthorizationInvalidationTrigger userAuthorizationInvalidationTrigger;

	private final AuditUserDisplayService auditUserDisplayService;

	private final UserOrgRelationBatchRemoveSupport userOrgRelationBatchRemoveSupport;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<UserDeptPageVO> getPage(Long userId, UserDeptPageQuery query) {
		userReferenceChecker.getExistingActive(userId);

		Page<UserDeptEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<UserDeptPageRowPO> page = baseMapper.selectListByPage(pageParams, userId, query);
		IPage<UserDeptPageVO> voPage = page.convert(UserDeptConverter.INSTANCE::toPageVo);

		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(Long userId, UserDeptAssignForm form) {
		// 校验用户是否存在
		userReferenceChecker.getExistingActive(userId);
		userOperationChecker.requireOperable(List.of(userId));
		deptReferenceChecker.getExistingActive(form.getDeptId());

		if (baseMapper.countByUserIdAndDeptId(userId, form.getDeptId()) > 0) {
			throw new SystemBusinessException(SystemAdminResultCode.USER_DEPT_DUPLICATE, form.getDeptId());
		}

		applyPrimaryDeptRules(userId, form);

		UserDeptEntity entity = UserDeptConverter.INSTANCE.toEntity(form);
		entity.setUserId(userId);
		save(entity);

		userAuthorizationInvalidationTrigger.submitByUserIds(List.of(userId), "create-dept");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(Long userId, Long id, UserDeptAssignForm form) {
		userReferenceChecker.getExistingActive(userId);
		userOperationChecker.requireOperable(List.of(userId));

		UserDeptEntity existing = baseMapper.selectByIdAndUserId(id, userId);
		if (existing == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST, id);
		}

		if (!Objects.equals(existing.getDeptId(), form.getDeptId())) {
			deptReferenceChecker.getExistingActive(form.getDeptId());
			UserDeptEntity conflict = baseMapper.selectByUserIdAndDeptId(userId, form.getDeptId());
			if (conflict != null && !Objects.equals(conflict.getId(), id)) {
				throw new SystemBusinessException(SystemAdminResultCode.USER_DEPT_DUPLICATE, form.getDeptId());
			}
		}

		applyPrimaryDeptRules(userId, form);
		UserDeptConverter.INSTANCE.applyUpdateForm(form, existing);
		updateById(existing);
		userAuthorizationInvalidationTrigger.submitByUserIds(List.of(userId), "update-dept");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeBatch(Long userId, List<Long> ids) {
		userOrgRelationBatchRemoveSupport.removeBatch(userId, ids, this::removeByIds, "delete-dept");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeAll(Long userId) {
		userReferenceChecker.getExistingActive(userId);
		userOperationChecker.requireOperable(List.of(userId));

		if (baseMapper.deleteByUserId(userId) > 0) {
			userAuthorizationInvalidationTrigger.submitByUserIds(List.of(userId), "clear-dept");
		}
	}

	/**
	 * 主部门规则：非正常状态不可设为主部门；设为主部门时降级该用户其他主部门。
	 * @param userId 用户 ID
	 * @param form 关联表单
	 */
	private void applyPrimaryDeptRules(Long userId, UserDeptAssignForm form) {
		boolean isPrimary = BooleanUtil.isTrue(form.getIsPrimary());
		if (isPrimary && !UserOrgRelationStatusEnum.canBePrimary(form.getStatus())) {
			throw new SystemBusinessException(SystemAdminResultCode.USER_DEPT_PRIMARY_NOT_ELIGIBLE);
		}
		if (isPrimary) {
			baseMapper.demotePrimaryByUserId(userId);
		}
	}

}
