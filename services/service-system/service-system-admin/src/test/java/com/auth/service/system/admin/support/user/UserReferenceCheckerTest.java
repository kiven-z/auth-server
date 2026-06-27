package com.auth.service.system.admin.support.user;

import com.auth.module.security.autoconfigure.pipeline.authenticate.AuthProfileSecurityContextPopulator;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.user.SysUserMapper;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

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

	private static final Long SUPER_ADMIN_USER_ID = PermissionConstant.SUPER_ADMIN_USER_IDS.iterator().next();

	private final AuthProfileSecurityContextPopulator securityContextPopulator = new AuthProfileSecurityContextPopulator();

	@AfterEach
	void tearDownSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Mock
	private SysUserMapper sysUserMapper;

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

		SystemBusinessException exception = (SystemBusinessException) assertThatThrownBy(
				() -> userReferenceChecker.getExistingActive(EXISTING_USER_ID))
			.isInstanceOf(SystemBusinessException.class)
			.actual();
		assertThat(exception.getResultCode()).isEqualTo(SystemCommonResultCode.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("assertUniqueBusinessKeys：无冲突时不抛异常")
	void assertUniqueBusinessKeysPassesWhenAllAbsent() {
		SysUserForm form = buildForm();
		when(sysUserMapper.selectCount(any())).thenReturn(0L);

		assertThatCode(() -> userReferenceChecker.assertUniqueBusinessKeys(form, null)).doesNotThrowAnyException();

		verify(sysUserMapper, times(4)).selectCount(any());
	}

	@Test
	@DisplayName("assertUniqueBusinessKeys：用户名重复时抛出 PARAM_DUPLICATE")
	void assertUniqueBusinessKeysThrowsOnDuplicateUsername() {
		SysUserForm form = buildForm();
		when(sysUserMapper.selectCount(any())).thenReturn(1L);

		assertThatThrownBy(() -> userReferenceChecker.assertUniqueBusinessKeys(form, EXISTING_USER_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_DUPLICATE);
	}

	@Test
	@DisplayName("requireOperable：普通用户且无登录上下文时不抛异常")
	void requireOperablePassesForNormalUserWithoutSecurityContext() {
		assertThatCode(() -> userReferenceChecker.requireOperable(List.of(EXISTING_USER_ID)))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("requireOperable：当前用户与目标不同且非超级管理员时不抛异常")
	void requireOperablePassesWhenCurrentUserDiffersFromTarget() {
		securityContextPopulator.populate(AuthProfile.builder().userId(200L).username("admin").build());

		assertThatCode(() -> userReferenceChecker.requireOperable(List.of(EXISTING_USER_ID)))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("requireOperable：超级管理员不可被操作")
	void requireOperableThrowsForSuperAdmin() {
		List<Long> userIds = List.of(SUPER_ADMIN_USER_ID);

		SystemBusinessException exception = (SystemBusinessException) assertThatThrownBy(
				() -> userReferenceChecker.requireOperable(userIds))
			.isInstanceOf(SystemBusinessException.class)
			.actual();
		assertThat(exception.getResultCode()).isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);
	}

	@Test
	@DisplayName("requireOperable：当前登录用户不可操作自身")
	void requireOperableThrowsWhenTargetIsCurrentUser() {
		securityContextPopulator.populate(AuthProfile.builder().userId(EXISTING_USER_ID).username("self").build());
		List<Long> userIds = List.of(EXISTING_USER_ID);

		SystemBusinessException exception = (SystemBusinessException) assertThatThrownBy(
				() -> userReferenceChecker.requireOperable(userIds))
			.isInstanceOf(SystemBusinessException.class)
			.actual();
		assertThat(exception.getResultCode()).isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);
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
