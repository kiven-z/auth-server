package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysUserMapper;
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
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

	private SysUserServiceImpl sysUserService;

	@BeforeEach
	void setUp() throws Exception {
		sysUserService = new SysUserServiceImpl(auditUserDisplayService);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysUserService, sysUserMapper);
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
		assertThatThrownBy(() -> sysUserService.deleteByIds(List.of(1L, 2L)))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_DELETE_SUPER_ADMIN_FORBIDDEN);

		verify(sysUserMapper, never()).deleteByIds(anyCollection());
	}

	@Test
	@DisplayName("批量删除：包含当前登录用户时拒绝")
	void deleteByIdsThrowsWhenDeletingSelf() {
		try (MockedStatic<SecurityUserUtils> securityUserUtils = mockStatic(SecurityUserUtils.class)) {
			securityUserUtils.when(SecurityUserUtils::getUserId).thenReturn(8L);

			assertThatThrownBy(() -> sysUserService.deleteByIds(List.of(8L)))
				.isInstanceOf(SystemBusinessException.class)
				.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
				.isEqualTo(SystemAdminResultCode.USER_DELETE_SELF_FORBIDDEN);
		}

		verify(sysUserMapper, never()).deleteByIds(anyCollection());
	}

	@Test
	@DisplayName("批量删除：校验通过后逻辑删除")
	void deleteByIdsShouldRemoveExistingUsers() {
		try (MockedStatic<SecurityUserUtils> securityUserUtils = mockStatic(SecurityUserUtils.class)) {
			securityUserUtils.when(SecurityUserUtils::getUserId).thenReturn(99L);

			sysUserService.deleteByIds(Arrays.asList(2L, null, 3L, 3L));

			verify(sysUserMapper).deleteByIds(List.of(2L, 3L));
		}
	}

}
