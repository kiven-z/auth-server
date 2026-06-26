package com.auth.service.system.admin.service.admin.impl;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.mapper.role.GrantTableMapper;
import com.auth.service.system.admin.mapper.user.SysUserMapper;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.auth.service.system.admin.support.user.SysUserDefaults;
import com.auth.service.system.admin.support.user.UserOperationChecker;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserSessionRevocationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysUserServiceImpl} 单元测试
 */
@DisplayName("SysUserServiceImpl 用户管理")
@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

	private static final Long USER_ID = 100L;

	@Mock
	private SysUserMapper sysUserMapper;

	@Mock
	private UserOperationChecker userOperationChecker;

	@Mock
	private UserReferenceChecker userReferenceChecker;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private UserSessionRevocationTrigger userSessionRevocationTrigger;

	@Mock
	private GrantTableMapper grantTableMapper;

	private SysUserServiceImpl sysUserService;

	private static SysUserForm buildCreateForm() {
		SysUserForm form = new SysUserForm();
		form.setUsername("alice");
		form.setNickname("Alice");
		form.setEmail("alice@example.com");
		form.setPhone("13800000000");
		form.setStatus(1);
		form.setInitialPassword("SecurePwd1");
		return form;
	}

	@BeforeEach
	void setUp() {
		sysUserService = new SysUserServiceImpl(userOperationChecker, userReferenceChecker, passwordEncoder,
				userSessionRevocationTrigger, grantTableMapper);
		ReflectionTestUtils.setField(sysUserService, "baseMapper", sysUserMapper);
	}

	@Test
	@DisplayName("新增用户：使用表单初始密码编码入库")
	void createEncodesInitialPasswordFromForm() {
		// 验证创建流程使用表单密码而非硬编码默认值
		SysUserForm form = buildCreateForm();

		when(passwordEncoder.encode("SecurePwd1")).thenReturn("$2a$encoded");
		when(sysUserMapper.insert(any(UserEntity.class))).thenReturn(1);

		sysUserService.create(form);

		ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
		verify(sysUserMapper).insert(captor.capture());
		assertThat(captor.getValue().getPassword()).isEqualTo("$2a$encoded");
		assertThat(captor.getValue().getPermVersion()).isEqualTo(SysUserDefaults.DEFAULT_PERM_VERSION);
		verify(passwordEncoder).encode("SecurePwd1");
		verify(userReferenceChecker).assertUniqueBusinessKeys(form, null);
	}

	@Test
	@DisplayName("新增用户：用户名重复时抛出业务异常")
	void createDuplicateUsernameThrows() {
		SysUserForm form = buildCreateForm();
		doThrow(new SystemBusinessException(SystemCommonResultCode.PARAM_DUPLICATE, "用户名", form.getUsername()))
			.when(userReferenceChecker)
			.assertUniqueBusinessKeys(eq(form), isNull());

		assertThatThrownBy(() -> sysUserService.create(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_DUPLICATE);

		verify(sysUserMapper, never()).insert(any(UserEntity.class));
	}

	@Test
	@DisplayName("批量删除：清理 grant_table 后物理删除并踢出会话")
	void deleteByIdsPurgesGrantTableAndPhysicallyDeletes() {
		List<Long> ids = List.of(USER_ID);

		sysUserService.deleteByIds(ids);

		verify(userOperationChecker).requireOperable(ids);
		verify(grantTableMapper).deleteBySubject("USER", USER_ID);
		verify(sysUserMapper).deleteByIds(ids);
		verify(userSessionRevocationTrigger).revokeAllSessionsAfterCommit(ids);
	}

}
