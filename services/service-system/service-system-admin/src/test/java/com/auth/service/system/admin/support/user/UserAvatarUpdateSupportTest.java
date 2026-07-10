package com.auth.service.system.admin.support.user;

import com.auth.common.core.model.response.Result;
import com.auth.module.file.api.feign.SystemFileRecordFeignClient;
import com.auth.module.file.api.model.request.OwnedFileAssertByUrlRequest;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.mapper.user.SysUserMapper;
import com.auth.service.system.admin.support.me.OldAvatarCleanupTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link UserAvatarUpdateSupport} 单元测试
 */
@DisplayName("UserAvatarUpdateSupport 头像更新")
@ExtendWith(MockitoExtension.class)
class UserAvatarUpdateSupportTest {

	private static final Long USER_ID = 100L;

	private static final Long OPERATOR_USER_ID = 300L;

	@Mock
	private SysUserMapper sysUserMapper;

	@Mock
	private SystemFileRecordFeignClient systemFileRecordFeignClient;

	@Mock
	private OldAvatarCleanupTrigger oldAvatarCleanupTrigger;

	@InjectMocks
	private UserAvatarUpdateSupport userAvatarUpdateSupport;

	private static UserEntity existingUser() {
		UserEntity entity = new UserEntity();
		entity.setId(UserAvatarUpdateSupportTest.USER_ID);
		entity.setNickname("Tester");
		return entity;
	}

	@Test
	@DisplayName("applyAvatarUpdate：校验归属后更新头像并触发旧头像清理")
	void applyAvatarUpdate_updatesAvatarAndTriggersCleanup() {
		UserEntity existing = existingUser();
		String oldAvatar = "https://cdn.example.com/public/avatar/old.png";
		existing.setAvatar(oldAvatar);
		String newAvatar = "https://cdn.example.com/public/avatar/new.png";

		when(systemFileRecordFeignClient.verifyOwnership(any(OwnedFileAssertByUrlRequest.class)))
			.thenReturn(Result.success());
		when(sysUserMapper.updateById(any(UserEntity.class))).thenReturn(1);

		userAvatarUpdateSupport.applyAvatarUpdate(existing, newAvatar, USER_ID);

		ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
		verify(sysUserMapper).updateById(captor.capture());
		assertThat(captor.getValue().getAvatar()).isEqualTo(newAvatar);

		ArgumentCaptor<OwnedFileAssertByUrlRequest> assertCaptor = ArgumentCaptor
			.forClass(OwnedFileAssertByUrlRequest.class);
		verify(systemFileRecordFeignClient).verifyOwnership(assertCaptor.capture());
		assertThat(assertCaptor.getValue().getOwnerUserId()).isEqualTo(USER_ID);

		verify(oldAvatarCleanupTrigger).cleanupAfterCommit(USER_ID, oldAvatar);
	}

	@Test
	@DisplayName("applyAvatarUpdate：以操作者校验文件归属")
	void applyAvatarUpdate_usesOperatorOwnership() {
		UserEntity existing = existingUser();
		String newAvatar = "https://cdn.example.com/public/avatar/new.png";

		when(systemFileRecordFeignClient.verifyOwnership(any(OwnedFileAssertByUrlRequest.class)))
			.thenReturn(Result.success());
		when(sysUserMapper.updateById(any(UserEntity.class))).thenReturn(1);

		userAvatarUpdateSupport.applyAvatarUpdate(existing, newAvatar, OPERATOR_USER_ID);

		ArgumentCaptor<OwnedFileAssertByUrlRequest> assertCaptor = ArgumentCaptor
			.forClass(OwnedFileAssertByUrlRequest.class);
		verify(systemFileRecordFeignClient).verifyOwnership(assertCaptor.capture());
		assertThat(assertCaptor.getValue().getOwnerUserId()).isEqualTo(OPERATOR_USER_ID);
	}

	@Test
	@DisplayName("applyAvatarUpdate：新旧头像相同时跳过更新")
	void applyAvatarUpdate_sameAvatarSkipsUpdate() {
		String avatar = "https://cdn.example.com/public/avatar/same.png";
		UserEntity existing = existingUser();
		existing.setAvatar(avatar);

		userAvatarUpdateSupport.applyAvatarUpdate(existing, avatar, USER_ID);

		verify(sysUserMapper, never()).updateById(any(UserEntity.class));
		verify(oldAvatarCleanupTrigger, never()).cleanupAfterCommit(any(), any());
	}

	@Test
	@DisplayName("applyAvatarUpdate：归属校验失败时抛出业务异常")
	void applyAvatarUpdate_assertFailureThrows() {
		UserEntity existing = existingUser();
		existing.setAvatar("https://cdn.example.com/public/avatar/old.png");

		when(systemFileRecordFeignClient.verifyOwnership(any(OwnedFileAssertByUrlRequest.class)))
			.thenReturn(Result.error("file not owned"));

		ThrowingCallable executable = () -> userAvatarUpdateSupport.applyAvatarUpdate(existing,
				"https://cdn.example.com/public/avatar/new.png", USER_ID);
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.OPERATION_FAILED);

		verify(sysUserMapper, never()).updateById(any(UserEntity.class));
		verify(oldAvatarCleanupTrigger, never()).cleanupAfterCommit(any(), any());
	}

}
