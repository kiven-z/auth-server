package com.auth.service.system.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.common.data.model.enums.UserStatus;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.convert.SysUserConverter;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysUserMapper;
import com.auth.service.system.admin.model.form.user.SysUserAdminResetPasswordForm;
import com.auth.service.system.admin.model.form.user.SysUserBatchStatusForm;
import com.auth.service.system.admin.model.form.user.SysUserChangePasswordForm;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.auth.service.system.admin.model.po.user.SysUserPageRowPO;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
import com.auth.service.system.admin.service.LogUserPasswordHistoryService;
import com.auth.service.system.admin.service.SysUserService;
import com.auth.service.system.admin.support.sqlbuild.SysUserPageOrderSqlBuilder;
import com.auth.service.system.admin.support.user.SysUserDefaults;
import com.auth.service.system.admin.support.user.UserOperationChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserSessionRevocationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.common.support.FieldChangeSupport;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;

/**
 * 系统用户服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, UserEntity> implements SysUserService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final UserOperationChecker userOperationChecker;

	private final PasswordEncoder passwordEncoder;

	private final UserSessionRevocationTrigger userSessionRevocationTrigger;

	private final LogUserPasswordHistoryService logUserPasswordHistoryService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysUserPageVO> getPage(Page<UserEntity> pageParams, SysUserPageQuery query) {
		String orderBySql = SysUserPageOrderSqlBuilder.buildOrderBySql(query.getSort());
		IPage<SysUserPageRowPO> page = baseMapper.selectListByPage(pageParams, query, orderBySql);
		IPage<SysUserPageVO> voPage = page.convert(SysUserConverter.INSTANCE::toPageVo);

		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(SysUserForm form) {
		UserEntity entity = SysUserConverter.INSTANCE.toEntity(form);
		entity.setPassword(passwordEncoder.encode(SysUserDefaults.DEFAULT_PASSWORD));
		if (form.getBirthday() != null) {
			entity.setAge(Period.between(form.getBirthday(), LocalDate.now()).getYears());
		}
		entity.setPermVersion(SysUserDefaults.DEFAULT_PERM_VERSION);

		save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(Long id, SysUserForm form) {
		UserEntity existing = getById(id);
		if (existing == null) {
			throw new SystemBusinessException(SystemCommonResultCode.USER_NOT_FOUND, id);
		}

		List<Long> userIds = List.of(id);
		userOperationChecker.requireOperable(userIds);

		// 敏感字段变更时踢出全部会话
		boolean revoke = FieldChangeSupport.anyChanged(
				FieldChangeSupport.codeChanged(existing.getUsername(), form.getUsername()),
				FieldChangeSupport.codeChanged(existing.getEmail(), form.getEmail()),
				FieldChangeSupport.codeChanged(existing.getPhone(), form.getPhone()),
				FieldChangeSupport.valueChanged(existing.getStatus(), form.getStatus()));
		if (revoke) {
			userSessionRevocationTrigger.revokeAllSessionsAfterCommit(userIds);
		}

		SysUserConverter.INSTANCE.applyUpdateForm(form, existing);
		if (form.getBirthday() != null) {
			existing.setAge(Period.between(form.getBirthday(), LocalDate.now()).getYears());
		}

		updateById(existing);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteByIds(List<Long> ids) {
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		List<Long> idsToDelete = ids.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(idsToDelete)) {
			return;
		}

		userOperationChecker.requireOperable(idsToDelete);

		removeByIds(idsToDelete);
		userSessionRevocationTrigger.revokeAllSessionsAfterCommit(idsToDelete);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void batchUpdateStatus(SysUserBatchStatusForm form) {
		Integer status = form.getStatus();
		if (UserStatus.of(status) == null) {
			throw new SystemBusinessException(SystemAdminResultCode.USER_STATUS_INVALID);
		}

		List<Long> idsToUpdate = form.getIds().stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(idsToUpdate)) {
			return;
		}

		userOperationChecker.requireOperable(idsToUpdate);

		List<UserEntity> updates = idsToUpdate.stream().map(userId -> {
			UserEntity entity = new UserEntity();
			entity.setId(userId);
			entity.setStatus(status);
			return entity;
		}).toList();
		updateBatchById(updates);
		userSessionRevocationTrigger.revokeAllSessionsAfterCommit(idsToUpdate);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void resetPasswordByAdmin(Long userId, SysUserAdminResetPasswordForm form, String changeIp) {
		UserEntity existing = getById(userId);
		if (existing == null) {
			throw new SystemBusinessException(SystemCommonResultCode.USER_NOT_FOUND, userId);
		}

		userOperationChecker.requireOperable(List.of(userId));
		applyPasswordChange(existing, form.getNewPassword(), form.getConfirmPassword(), changeIp);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void changeOwnPassword(SysUserChangePasswordForm form, String changeIp) {
		Long currentUserId = SecurityUserUtils.getUserId();
		if (currentUserId == null) {
			throw new SystemBusinessException(SystemCommonResultCode.JWT_INVALID);
		}

		UserEntity existing = getById(currentUserId);
		if (existing == null) {
			throw new SystemBusinessException(SystemCommonResultCode.USER_NOT_FOUND, currentUserId);
		}

		if (!passwordEncoder.matches(form.getOldPassword(), existing.getPassword())) {
			throw new SystemBusinessException(SystemAdminResultCode.OLD_PASSWORD_INCORRECT);
		}

		applyPasswordChange(existing, form.getNewPassword(), form.getConfirmPassword(), changeIp);
	}

	/**
	 * 校验确认密码、更新密码、记录历史并踢出全部会话
	 * @param user 目标用户
	 * @param newPassword 新密码明文
	 * @param confirmPassword 确认密码明文
	 * @param changeIp 客户端 IP
	 */
	private void applyPasswordChange(UserEntity user, String newPassword, String confirmPassword, String changeIp) {
		if (!Objects.equals(newPassword, confirmPassword)) {
			throw new SystemBusinessException(SystemAdminResultCode.PASSWORD_CONFIRM_MISMATCH);
		}

		String encodedPassword = passwordEncoder.encode(newPassword);
		user.setPassword(encodedPassword);
		updateById(user);

		logUserPasswordHistoryService.recordChange(user.getId(), encodedPassword, changeIp);
		userSessionRevocationTrigger.revokeAllSessionsAfterCommit(List.of(user.getId()));
	}

}
