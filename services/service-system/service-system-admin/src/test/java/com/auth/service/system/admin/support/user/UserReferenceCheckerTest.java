package com.auth.service.system.admin.support.user;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.mapper.user.SysUserMapper;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.auth.service.system.admin.service.admin.SysUserService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link UserReferenceChecker} 单元测试
 */
@DisplayName("UserReferenceChecker 用户引用校验")
@ExtendWith(MockitoExtension.class)
class UserReferenceCheckerTest {

	private static final Long EXISTING_USER_ID = 100L;

	@Mock
	private SysUserMapper sysUserMapper;

	@Mock
	private SysUserService userService;

	@InjectMocks
	private UserReferenceChecker userReferenceChecker;

	@Test
	@DisplayName("getExistingActive：用户存在时返回实体")
	void getExistingActiveReturnsEntity() {
		UserEntity user = new UserEntity();
		user.setId(EXISTING_USER_ID);
		when(sysUserMapper.selectById(EXISTING_USER_ID)).thenReturn(user);

		UserEntity result = userReferenceChecker.getExistingActive(EXISTING_USER_ID);

		assertThat(result.getId()).isEqualTo(EXISTING_USER_ID);
	}

	@Test
	@DisplayName("getExistingActive：用户不存在时抛出 USER_NOT_FOUND")
	void getExistingActiveThrowsWhenMissing() {
		when(sysUserMapper.selectById(EXISTING_USER_ID)).thenReturn(null);

		assertThatThrownBy(() -> userReferenceChecker.getExistingActive(EXISTING_USER_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("assertUniqueBusinessKeys：无冲突时不抛异常")
	void assertUniqueBusinessKeysPassesWhenAllAbsent() {
		SysUserForm form = buildForm();
		when(userService.count(any())).thenReturn(0L);

		assertThatCode(() -> userReferenceChecker.assertUniqueBusinessKeys(form, null)).doesNotThrowAnyException();

		verify(userService, times(4)).count(any());
	}

	@Test
	@DisplayName("assertUniqueBusinessKeys：用户名重复时抛出 PARAM_DUPLICATE")
	void assertUniqueBusinessKeysThrowsOnDuplicateUsername() {
		SysUserForm form = buildForm();
		when(userService.count(any())).thenReturn(1L);

		assertThatThrownBy(() -> userReferenceChecker.assertUniqueBusinessKeys(form, EXISTING_USER_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_DUPLICATE);
	}

	private static SysUserForm buildForm() {
		SysUserForm form = new SysUserForm();
		form.setUsername("alice");
		form.setEmail("alice@example.com");
		form.setPhone("13800000000");
		form.setEmployeeNo("E001");
		return form;
	}

}
