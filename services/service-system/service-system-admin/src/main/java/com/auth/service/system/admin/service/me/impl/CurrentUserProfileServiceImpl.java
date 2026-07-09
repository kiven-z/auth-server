package com.auth.service.system.admin.service.me.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.core.utils.FieldChangeSupport;
import com.auth.common.data.support.BusinessKeyAssert;
import com.auth.common.web.resttemplate.FeignUtil;
import com.auth.module.file.api.feign.SystemFileGovernanceFeignClient;
import com.auth.module.file.api.model.enums.FileBizType;
import com.auth.module.file.api.model.request.OwnedFileAssertByUrlRequest;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.mapper.user.SysUserMapper;
import com.auth.service.system.admin.model.form.me.MeAvatarUpdateForm;
import com.auth.service.system.admin.model.form.me.MeProfileUpdateForm;
import com.auth.service.system.admin.service.me.CurrentUserProfileService;
import com.auth.service.system.admin.support.me.OldAvatarCleanupTrigger;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserSessionRevocationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * 当前登录用户个人资料维护实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class CurrentUserProfileServiceImpl extends ServiceImpl<SysUserMapper, UserEntity>
		implements CurrentUserProfileService {

	private final UserReferenceChecker userReferenceChecker;

	private final UserSessionRevocationTrigger userSessionRevocationTrigger;

	private final SystemFileGovernanceFeignClient systemFileGovernanceFeignClient;

	private final OldAvatarCleanupTrigger oldAvatarCleanupTrigger;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateMyProfile(MeProfileUpdateForm form) {
		Long userId = SecurityUserUtils.getUserId();
		UserEntity existing = userReferenceChecker.getExistingActive(userId);

		BusinessKeyAssert.requireAbsent(baseMapper,
				Wrappers.<UserEntity>lambdaQuery()
					.eq(UserEntity::getEmail, form.getEmail())
					.ne(UserEntity::getId, userId),
				() -> new SystemBusinessException(SystemCommonResultCode.PARAM_DUPLICATE, "邮箱", form.getEmail()));
		BusinessKeyAssert.requireAbsent(baseMapper,
				Wrappers.<UserEntity>lambdaQuery()
					.eq(UserEntity::getPhone, form.getPhone())
					.ne(UserEntity::getId, userId),
				() -> new SystemBusinessException(SystemCommonResultCode.PARAM_DUPLICATE, "手机号", form.getPhone()));

		boolean revoke = FieldChangeSupport.anyChanged(
				FieldChangeSupport.codeChanged(existing.getEmail(), form.getEmail()),
				FieldChangeSupport.codeChanged(existing.getPhone(), form.getPhone()));
		if (revoke) {
			userSessionRevocationTrigger.revokeAllSessionsAfterCommit(List.of(existing.getId()));
		}

		existing.setNickname(form.getNickname());
		existing.setEmail(form.getEmail());
		existing.setPhone(form.getPhone());
		existing.setGender(form.getGender());
		existing.setBirthday(form.getBirthday());
		existing.setIntroduction(form.getIntroduction());
		if (form.getBirthday() != null) {
			existing.setAge(Period.between(form.getBirthday(), LocalDate.now()).getYears());
		}

		updateById(existing);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateMyAvatar(MeAvatarUpdateForm form) {
		Long userId = SecurityUserUtils.getUserId();
		UserEntity existing = userReferenceChecker.getExistingActive(userId);

		String avatar = form.getAvatar();
		String oldAvatar = existing.getAvatar();
		if (CharSequenceUtil.isBlank(avatar) || CharSequenceUtil.equals(oldAvatar, avatar)) {
			return;
		}

		var assertResult = systemFileGovernanceFeignClient.verifyOwnership(OwnedFileAssertByUrlRequest.builder()
			.url(avatar)
			.ownerUserId(userId)
			.bizType(FileBizType.AVATAR.getCode())
			.build());
		if (!FeignUtil.isSuccess(assertResult)) {
			throw new SystemBusinessException(SystemCommonResultCode.OPERATION_FAILED, assertResult.getMessage());
		}

		existing.setAvatar(avatar);
		updateById(existing);

		oldAvatarCleanupTrigger.cleanupAfterCommit(userId, oldAvatar);
	}

}
