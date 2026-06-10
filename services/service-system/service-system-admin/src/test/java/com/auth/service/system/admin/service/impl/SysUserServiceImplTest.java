package com.auth.service.system.admin.service.impl;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysUserMapper;
import com.auth.service.system.admin.model.form.user.SysUserAdminResetPasswordForm;
import com.auth.service.system.admin.model.form.user.SysUserChangePasswordForm;
import com.auth.service.system.admin.service.LogUserPasswordHistoryService;
import com.auth.service.system.admin.support.user.UserOperationChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserSessionRevocationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysUserServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysUserServiceImpl 用户密码")
@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

	private static final Long TARGET_USER_ID = 10L;

	private static final Long CURRENT_USER_ID = 20L;

	private static final String CHANGE_IP = "127.0.0.1";

	@Mock
	private SysUserMapper sysUserMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private UserSessionRevocationTrigger userSessionRevocationTrigger;

	@Mock
	private LogUserPasswordHistoryService logUserPasswordHistoryService;

	private SysUserServiceImpl sysUserService;

	@BeforeEach
	void setUp() throws Exception {
		sysUserService = new SysUserServiceImpl(auditUserDisplayService, new UserOperationChecker(), passwordEncoder,
				userSessionRevocationTrigger, logUserPasswordHistoryService);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysUserService, sysUserMapper);
	}

	@Test
	@DisplayName("管理员重置密码：成功时更新密码、记录历史并踢出会话")
	void resetPasswordByAdminUpdatesPasswordAndRevokesSessions() {
		UserEntity existing = userWithId(TARGET_USER_ID, "{bcrypt}old");
		SysUserAdminResetPasswordForm form = adminResetForm("newPass12", "newPass12");

		when(sysUserMapper.selectById(TARGET_USER_ID)).thenReturn(existing);
		when(passwordEncoder.encode("newPass12")).thenReturn("{bcrypt}new");
		when(sysUserMapper.updateById(any(UserEntity.class))).thenReturn(1);

		try (MockedStatic<SecurityUserUtils> securityUserUtils = mockStatic(SecurityUserUtils.class)) {
			securityUserUtils.when(SecurityUserUtils::getUserId).thenReturn(CURRENT_USER_ID);
			sysUserService.resetPasswordByAdmin(TARGET_USER_ID, form, CHANGE_IP);
		}

		ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
		verify(sysUserMapper).updateById(userCaptor.capture());
		assertThat(userCaptor.getValue().getPassword()).isEqualTo("{bcrypt}new");
		verify(logUserPasswordHistoryService).recordChange(TARGET_USER_ID, "{bcrypt}new", CHANGE_IP);
		verify(userSessionRevocationTrigger).revokeAllSessionsAfterCommit(List.of(TARGET_USER_ID));
	}

	@Test
	@DisplayName("管理员重置密码：用户不存在时抛出 USER_NOT_FOUND")
	void resetPasswordByAdminRejectsMissingUser() {
		when(sysUserMapper.selectById(99L)).thenReturn(null);

		assertThatThrownBy(67890
				- () -> sysUserService.resetPasswordByAdmin(99L, adminResetForm("newPass12", "newPass12"), CHANGE_IP))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("管理员重置密码：目标为当前用户时抛出 USER_OPERATION_FORBIDDEN")
	void resetPasswordByAdminRejectsCurrentUser() {
		when(sysUserMapper.selectById(CURRENT_USER_ID)).thenReturn(userWithId(CURRENT_USER_ID, "{bcrypt}old"));

		try (MockedStatic<SecurityUserUtils> securityUserUtils = mockStatic(SecurityUserUtils.class)) {
			securityUserUtils.when(SecurityUserUtils::getUserId).thenReturn(CURRENT_USER_ID);

			assertThatThrownBy(() -> sysUserService.resetPasswordByAdmin(CURRENT_USER_ID,
					adminResetForm("newPass12", "newPass12"), CHANGE_IP))
				.isInstanceOf(SystemBusinessException.class)
				.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
				.isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);
		}
	}

	@Test
	@DisplayName("管理员重置密码：两次密码不一致时抛出 PASSWORD_CONFIRM_MISMATCH")
	void resetPasswordByAdminRejectsPasswordMismatch() {
		when(sysUserMapper.selectById(TARGET_USER_ID)).thenReturn(userWithId(TARGET_USER_ID, "{bcrypt}old"));

		try (MockedStatic<SecurityUserUtils> securityUserUtils = mockStatic(SecurityUserUtils.class)) {
			securityUserUtils.when(SecurityUserUtils::getUserId).thenReturn(CURRENT_USER_ID);

			assertThatThrownBy(() -> sysUserService.resetPasswordByAdmin(TARGET_USER_ID,
					adminResetForm("newPass12", "otherPass1"), CHANGE_IP))
				.isInstanceOf(SystemBusinessException.class)
				.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
				.isEqualTo(SystemAdminResultCode.PASSWORD_CONFIRM_MISMATCH);
		}
	}

	@Test
	@DisplayName("自改密码：旧密码错误时抛出 OLD_PASSWORD_INCORRECT")
	void changeOwnPasswordRejectsWrongOldPassword() {
		UserEntity existing = userWithId(CURRENT_USER_ID, "{bcrypt}old");
		SysUserChangePasswordForm form = selfChangeForm("wrongOld1", "newPass12", "newPass12");

		when(sysUserMapper.selectById(CURRENT_USER_ID)).thenReturn(existing);
		when(passwordEncoder.matches("wrongOld1", "{bcrypt}old")).thenReturn(false);

		try (MockedStatic<SecurityUserUtils> securityUserUtils = mockStatic(SecurityUserUtils.class)) {
			securityUserUtils.when(SecurityUserUtils::getUserId).thenReturn(CURRENT_USER_ID);

			assertThatThrownBy(() -> sysUserService.changeOwnPassword(form, CHANGE_IP))
				.isInstanceOf(SystemBusinessException.class)
				.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
				.isEqualTo(SystemAdminResultCode.OLD_PASSWORD_INCORRECT);
		}
	}

	@Test
	@DisplayName("自改密码：成功时更新密码并记录历史")
	void changeOwnPasswordUpdatesPassword() {
		UserEntity existing = userWithId(CURRENT_USER_ID, "{bcrypt}old");
		SysUserChangePasswordForm form = selfChangeForm("oldPass12", "newPass12", "newPass12");

		when(sysUserMapper.selectById(CURRENT_USER_ID)).thenReturn(existing);
		when(passwordEncoder.matches("oldPass12", "{bcrypt}old")).thenReturn(true);
		when(passwordEncoder.encode("newPass12")).thenReturn("{bcrypt}new");
		when(sysUserMapper.updateById(any(UserEntity.class))).thenReturn(1);

		try (MockedStatic<SecurityUserUtils> securityUserUtils = mockStatic(SecurityUserUtils.class)) {
			securityUserUtils.when(SecurityUserUtils::getUserId).thenReturn(CURRENT_USER_ID);
			sysUserService.changeOwnPassword(form, CHANGE_IP);
		}

		verify(logUserPasswordHistoryService).recordChange(CURRENT_USER_ID, "{bcrypt}new", CHANGE_IP);
		verify(userSessionRevocationTrigger).revokeAllSessionsAfterCommit(List.of(CURRENT_USER_ID));
	}

	private static UserEntity userWithId(Long id, String password) {
		UserEntity user = new UserEntity();
		user.setId(id);
		user.setPassword(password);
		return user;
	}

	private static SysUserAdminResetPasswordForm adminResetForm(String newPassword, String confirmPassword) {
		SysUserAdminResetPasswordForm form = new SysUserAdminResetPasswordForm();
		form.setNewPassword(newPassword);
		form.setConfirmPassword(confirmPassword);
		return form;
	}

	private static SysUserChangePasswordForm selfChangeForm(String oldPassword, String newPassword,
			String confirmPassword) {
		SysUserChangePasswordForm form = new SysUserChangePasswordForm();
		form.setOldPassword(oldPassword);
		form.setNewPassword(newPassword);
		form.setConfirmPassword(confirmPassword);
		return form;
	}

}
