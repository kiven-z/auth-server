package com.auth.service.system.admin.service.me.impl;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.system.admin.mapper.user.SysUserMapper;
import com.auth.service.system.admin.model.form.me.MeAvatarUpdateForm;
import com.auth.service.system.admin.model.form.me.MeProfileUpdateForm;
import com.auth.service.system.admin.support.user.UserAvatarUpdateSupport;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserSessionRevocationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link CurrentUserProfileServiceImpl} 单元测试
 */
@DisplayName("CurrentUserProfileServiceImpl 个人资料更新")
@ExtendWith(MockitoExtension.class)
class CurrentUserProfileServiceImplTest {

	private static final Long USER_ID = 100L;

	@Mock
	private SysUserMapper sysUserMapper;

	@Mock
	private UserReferenceChecker userReferenceChecker;

	@Mock
	private UserSessionRevocationTrigger userSessionRevocationTrigger;

	@Mock
	private UserAvatarUpdateSupport userAvatarUpdateSupport;

	@InjectMocks
	private CurrentUserProfileServiceImpl currentUserProfileService;

	private static UserEntity existingUser() {
		UserEntity entity = new UserEntity();
		entity.setId(USER_ID);
		entity.setNickname("Old Nick");
		entity.setEmail("old@example.com");
		entity.setPhone("13800000000");
		entity.setGender(0);
		return entity;
	}

	private static MeProfileUpdateForm buildForm() {
		MeProfileUpdateForm form = new MeProfileUpdateForm();
		form.setNickname("Alice");
		form.setEmail("old@example.com");
		form.setPhone("13800000000");
		form.setGender(1);
		return form;
	}

	private static MeAvatarUpdateForm buildAvatarForm(String avatar) {
		MeAvatarUpdateForm form = new MeAvatarUpdateForm();
		form.setAvatar(avatar);
		return form;
	}

	@BeforeEach
	void setUp() throws Exception {
		AuthProfile profile = AuthProfile.builder().userId(USER_ID).username("tester").build();
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(profile, null));

		var baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(currentUserProfileService, sysUserMapper);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("正常更新：写入可编辑字段并计算年龄")
	void updateMyProfile_updatesEditableFields() {
		UserEntity existing = existingUser();
		MeProfileUpdateForm form = buildForm();
		form.setNickname("New Nick");
		form.setIntroduction("New intro");
		form.setBirthday(LocalDate.of(1995, 6, 15));

		when(userReferenceChecker.getExistingActive(USER_ID)).thenReturn(existing);
		when(sysUserMapper.selectCount(any())).thenReturn(0L);
		when(sysUserMapper.updateById(any(UserEntity.class))).thenReturn(1);

		currentUserProfileService.updateMyProfile(form);

		ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
		verify(sysUserMapper).updateById(captor.capture());
		UserEntity updated = captor.getValue();
		assertThat(updated.getNickname()).isEqualTo("New Nick");
		assertThat(updated.getEmail()).isEqualTo(form.getEmail());
		assertThat(updated.getPhone()).isEqualTo(form.getPhone());
		assertThat(updated.getGender()).isEqualTo(form.getGender());
		assertThat(updated.getBirthday()).isEqualTo(form.getBirthday());
		assertThat(updated.getIntroduction()).isEqualTo("New intro");
		assertThat(updated.getAvatar()).isNull();
		assertThat(updated.getAge())
			.isEqualTo(java.time.Period.between(form.getBirthday(), LocalDate.now()).getYears());
		verify(userSessionRevocationTrigger, never()).revokeAllSessionsAfterCommit(any());
	}

	@Test
	@DisplayName("邮箱重复时抛出业务异常")
	void updateMyProfile_duplicateEmailThrows() {
		UserEntity existing = existingUser();
		MeProfileUpdateForm form = buildForm();
		form.setEmail("taken@example.com");

		when(userReferenceChecker.getExistingActive(USER_ID)).thenReturn(existing);
		when(sysUserMapper.selectCount(any())).thenReturn(1L);

		assertThatThrownBy(() -> currentUserProfileService.updateMyProfile(form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_DUPLICATE);

		verify(sysUserMapper, never()).updateById(any(UserEntity.class));
		verify(userSessionRevocationTrigger, never()).revokeAllSessionsAfterCommit(any());
	}

	@Test
	@DisplayName("手机号重复时抛出业务异常")
	void updateMyProfile_duplicatePhoneThrows() {
		UserEntity existing = existingUser();
		MeProfileUpdateForm form = buildForm();
		form.setPhone("13999999999");

		when(userReferenceChecker.getExistingActive(USER_ID)).thenReturn(existing);
		when(sysUserMapper.selectCount(any())).thenReturn(0L).thenReturn(1L);

		assertThatThrownBy(() -> currentUserProfileService.updateMyProfile(form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_DUPLICATE);

		verify(sysUserMapper, never()).updateById(any(UserEntity.class));
		verify(userSessionRevocationTrigger, never()).revokeAllSessionsAfterCommit(any());
	}

	@Test
	@DisplayName("邮箱变更时踢出全部会话")
	void updateMyProfile_emailChangeRevokesSessions() {
		UserEntity existing = existingUser();
		MeProfileUpdateForm form = buildForm();
		form.setEmail("new-mail@example.com");

		when(userReferenceChecker.getExistingActive(USER_ID)).thenReturn(existing);
		when(sysUserMapper.selectCount(any())).thenReturn(0L);
		when(sysUserMapper.updateById(any(UserEntity.class))).thenReturn(1);

		currentUserProfileService.updateMyProfile(form);

		verify(userSessionRevocationTrigger).revokeAllSessionsAfterCommit(List.of(USER_ID));
	}

	@Test
	@DisplayName("仅昵称变更时不踢出会话")
	void updateMyProfile_nicknameOnlyChangeDoesNotRevokeSessions() {
		UserEntity existing = existingUser();
		MeProfileUpdateForm form = buildForm();
		form.setNickname("Only Nickname");

		when(userReferenceChecker.getExistingActive(USER_ID)).thenReturn(existing);
		when(sysUserMapper.selectCount(any())).thenReturn(0L);
		when(sysUserMapper.updateById(any(UserEntity.class))).thenReturn(1);

		currentUserProfileService.updateMyProfile(form);

		verify(userSessionRevocationTrigger, never()).revokeAllSessionsAfterCommit(any());
	}

	@Test
	@DisplayName("updateMyAvatar：委托 applyAvatarUpdate")
	void updateMyAvatar_delegatesToSupport() {
		String avatar = "https://cdn.example.com/public/avatar/new.png";
		UserEntity existing = existingUser();
		when(userReferenceChecker.getExistingActive(USER_ID)).thenReturn(existing);

		currentUserProfileService.updateMyAvatar(buildAvatarForm(avatar));

		verify(userAvatarUpdateSupport).applyAvatarUpdate(existing, avatar, USER_ID);
	}

}
