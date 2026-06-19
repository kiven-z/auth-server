package com.auth.service.system.admin.service.admin.impl;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.mapper.user.SysUserMapper;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.auth.service.system.admin.support.user.SysUserDefaults;
import com.auth.service.system.admin.support.user.UserOperationChecker;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserSessionRevocationTrigger;
import com.auth.service.system.common.service.AuditUserDisplayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysUserServiceImpl} 单元测试
 */
@DisplayName("SysUserServiceImpl 用户管理")
@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

	@Mock
	private SysUserMapper sysUserMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private UserOperationChecker userOperationChecker;

	@Mock
	private UserReferenceChecker userReferenceChecker;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private UserSessionRevocationTrigger userSessionRevocationTrigger;

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
		sysUserService = new SysUserServiceImpl(auditUserDisplayService, userOperationChecker, userReferenceChecker,
				passwordEncoder, userSessionRevocationTrigger);
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
	}

}
