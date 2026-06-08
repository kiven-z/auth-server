package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.common.data.model.enums.GenderStatus;
import com.auth.common.data.model.enums.UserStatus;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysUserMapper;
import com.auth.service.system.admin.model.form.user.SysUserBatchStatusForm;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.auth.service.system.admin.support.user.SysUserDefaults;
import com.auth.service.system.admin.support.user.UserOperationChecker;
import com.auth.service.system.admin.support.user.UserSessionRevocationSupport;
import com.auth.service.system.admin.model.po.user.SysUserPageRowPO;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;

/**
 * {@link SysUserServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysUserServiceImpl 用户管理")
@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

	@Mock
	private SysUserMapper sysUserMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private UserSessionRevocationSupport userSessionRevocationSupport;

	private SysUserServiceImpl sysUserService;

	@BeforeEach
	void setUp() throws Exception {
		sysUserService = new SysUserServiceImpl(auditUserDisplayService, new UserOperationChecker(), passwordEncoder,
				userSessionRevocationSupport);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysUserService, sysUserMapper);
	}

	private static SysUserForm validCreateForm() {
		SysUserForm form = new SysUserForm();
		form.setUsername("new_user");
		form.setNickname("New User");
		form.setEmail("new@example.com");
		form.setPhone("13800000000");
		form.setEmployeeNo("E100");
		form.setStatus(UserStatus.NORMAL.getCode());
		form.setGender(GenderStatus.MALE.getCode());
		form.setBirthday(LocalDate.of(1990, 1, 1));
		form.setIntroduction("hello");
		form.setRemark("note");
		return form;
	}

	@Test
	@DisplayName("新增用户：写入默认密码与表单字段")
	void createShouldPersistUserWithDefaults() {
		SysUserForm form = validCreateForm();
		when(passwordEncoder.encode(SysUserDefaults.DEFAULT_PASSWORD)).thenReturn("{bcrypt}hash");
		SysUserServiceImpl spyService = spy(sysUserService);
		doAnswer(invocation -> {
			UserEntity entity = invocation.getArgument(0);
			entity.setId(100L);
			return true;
		}).when(spyService).save(org.mockito.ArgumentMatchers.any(UserEntity.class));

		spyService.create(form);

		ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
		verify(spyService).save(captor.capture());
		UserEntity saved = captor.getValue();
		assertThat(saved.getUsername()).isEqualTo("new_user");
		assertThat(saved.getPassword()).isEqualTo("{bcrypt}hash");
		assertThat(saved.getAvatar()).isNull();
		assertThat(saved.getGender()).isEqualTo(GenderStatus.MALE.getCode());
		assertThat(saved.getRemark()).isEqualTo("note");
		assertThat(saved.getAge()).isNotNull();
	}

	@Test
	@DisplayName("分页查询：委托 Mapper 并补充审计用户名")
	void getPageDelegatesToMapperAndEnrichesAuditUsernames() {
		Page<UserEntity> pageParams = new Page<>(1, 10);
		SysUserPageQuery query = new SysUserPageQuery();
		query.setUsername("admin");
		Page<SysUserPageRowPO> poPage = new Page<>(1, 10);
		poPage.setRecords(List.of(new SysUserPageRowPO()));
		when(sysUserMapper.selectListByPage(eq(pageParams), eq(query), anyString())).thenReturn(poPage);

		PageResponse<SysUserPageVO> result = sysUserService.getPage(pageParams, query);

		assertThat(result.getList()).hasSize(1);
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class));
	}

	@Test
	@DisplayName("批量删除：空列表直接返回")
	void deleteByIdsIgnoresEmptyIds() {
		sysUserService.deleteByIds(List.of());

		verifyNoInteractions(sysUserMapper);
	}

	@Test
	@DisplayName("批量删除：包含超级管理员时拒绝")
	void deleteByIdsThrowsWhenSuperAdminIncluded() {
		List<Long> ids = List.of(1L, 2L);
		assertThatThrownBy(() -> sysUserService.deleteByIds(ids)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);

		verify(sysUserMapper, never()).deleteByIds(anyCollection());
	}

	@Test
	@DisplayName("批量删除：包含当前登录用户时拒绝")
	void deleteByIdsThrowsWhenDeletingSelf() {
		try (MockedStatic<SecurityUserUtils> securityUserUtils = mockStatic(SecurityUserUtils.class)) {
			securityUserUtils.when(SecurityUserUtils::getUserId).thenReturn(8L);

			List<Long> ids = List.of(8L);
			assertThatThrownBy(() -> sysUserService.deleteByIds(ids)).isInstanceOf(SystemBusinessException.class)
				.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
				.isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);
		}

		verify(sysUserMapper, never()).deleteByIds(anyCollection());
	}

	@Test
	@DisplayName("批量删除：校验通过后逻辑删除并踢出会话")
	void deleteByIdsShouldRemoveExistingUsers() {
		SysUserServiceImpl spyService = spy(sysUserService);
		try (MockedStatic<SecurityUserUtils> securityUserUtils = mockStatic(SecurityUserUtils.class)) {
			securityUserUtils.when(SecurityUserUtils::getUserId).thenReturn(99L);

			spyService.deleteByIds(Arrays.asList(2L, null, 3L, 3L));

			verify(spyService).removeByIds(List.of(2L, 3L));
			verify(userSessionRevocationSupport).revokeAllSessionsAfterCommit(List.of(2L, 3L));
		}
	}

	@Test
	@DisplayName("批量改状态：空主键列表直接返回")
	void batchUpdateStatusIgnoresEmptyIds() {
		SysUserBatchStatusForm form = new SysUserBatchStatusForm();
		form.setIds(List.of());
		form.setStatus(UserStatus.NORMAL.getCode());

		sysUserService.batchUpdateStatus(form);

		verify(sysUserMapper, never()).updateById(any(UserEntity.class));
	}

	@Test
	@DisplayName("批量改状态：状态码非法时拒绝")
	void batchUpdateStatusThrowsWhenStatusInvalid() {
		SysUserBatchStatusForm form = new SysUserBatchStatusForm();
		form.setIds(List.of(2L));
		form.setStatus(9);

		assertThatThrownBy(() -> sysUserService.batchUpdateStatus(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_STATUS_INVALID);

		verify(sysUserMapper, never()).updateById(any(UserEntity.class));
	}

	@Test
	@DisplayName("批量改状态：包含超级管理员时拒绝")
	void batchUpdateStatusThrowsWhenSuperAdminIncluded() {
		SysUserBatchStatusForm form = new SysUserBatchStatusForm();
		form.setIds(List.of(1L, 2L));
		form.setStatus(UserStatus.DISABLED.getCode());

		assertThatThrownBy(() -> sysUserService.batchUpdateStatus(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);

		verify(sysUserMapper, never()).updateById(any(UserEntity.class));
	}

	@Test
	@DisplayName("批量改状态：包含当前登录用户时拒绝")
	void batchUpdateStatusThrowsWhenUpdatingSelf() {
		SysUserBatchStatusForm normalForm = new SysUserBatchStatusForm();
		normalForm.setIds(List.of(8L));
		normalForm.setStatus(UserStatus.NORMAL.getCode());

		SysUserBatchStatusForm disableForm = new SysUserBatchStatusForm();
		disableForm.setIds(List.of(8L));
		disableForm.setStatus(UserStatus.DISABLED.getCode());

		SysUserBatchStatusForm lockForm = new SysUserBatchStatusForm();
		lockForm.setIds(List.of(8L));
		lockForm.setStatus(UserStatus.LOCKED.getCode());

		try (MockedStatic<SecurityUserUtils> securityUserUtils = mockStatic(SecurityUserUtils.class)) {
			securityUserUtils.when(SecurityUserUtils::getUserId).thenReturn(8L);

			assertThatThrownBy(() -> sysUserService.batchUpdateStatus(normalForm))
				.isInstanceOf(SystemBusinessException.class)
				.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
				.isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);

			assertThatThrownBy(() -> sysUserService.batchUpdateStatus(disableForm))
				.isInstanceOf(SystemBusinessException.class)
				.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
				.isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);

			assertThatThrownBy(() -> sysUserService.batchUpdateStatus(lockForm))
				.isInstanceOf(SystemBusinessException.class)
				.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
				.isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);
		}

		verify(sysUserMapper, never()).updateById(any(UserEntity.class));
	}

	@Test
	@DisplayName("批量改状态：校验通过后批量更新并踢出会话")
	void batchUpdateStatusShouldUpdateUsers() {
		SysUserBatchStatusForm form = new SysUserBatchStatusForm();
		form.setIds(Arrays.asList(2L, null, 3L, 3L));
		form.setStatus(UserStatus.LOCKED.getCode());
		SysUserServiceImpl spyService = spy(sysUserService);
		doReturn(true).when(spyService).updateBatchById(anyList());

		try (MockedStatic<SecurityUserUtils> securityUserUtils = mockStatic(SecurityUserUtils.class)) {
			securityUserUtils.when(SecurityUserUtils::getUserId).thenReturn(99L);

			spyService.batchUpdateStatus(form);

			verify(spyService).updateBatchById(argThat(updates -> updates.size() == 2
					&& updates.stream()
						.anyMatch(entity -> Long.valueOf(2L).equals(entity.getId())
								&& Integer.valueOf(UserStatus.LOCKED.getCode()).equals(entity.getStatus()))
					&& updates.stream()
						.anyMatch(entity -> Long.valueOf(3L).equals(entity.getId())
								&& Integer.valueOf(UserStatus.LOCKED.getCode()).equals(entity.getStatus()))));
			verify(userSessionRevocationSupport).revokeAllSessionsAfterCommit(List.of(2L, 3L));
		}
	}

}
