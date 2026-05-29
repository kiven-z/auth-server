package com.auth.service.auth.authorization.application;

import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.granttable.ScopeGrantType;
import com.auth.service.auth.authorization.application.datascope.DeptDataScopeResolver;
import com.auth.service.auth.authorization.domain.model.UserAccountSnapshot;
import com.auth.service.auth.authorization.domain.port.UserReadPort;
import com.auth.service.auth.authorization.infrastructure.persistence.UserAuthorizationGrantMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthProfileQueryService} 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthProfileQueryServiceTest {

	@Mock
	private UserAuthorizationGrantMapper userAuthorizationGrantMapper;

	@Mock
	private UserReadPort userReadPort;

	@Mock
	private DeptDataScopeResolver deptDataScopeResolver;

	@Test
	@DisplayName("构建画像时注入生效数据权限")
	void buildByAccount_shouldContainResolvedDeptScope() {
		UserAccountSnapshot account = new UserAccountSnapshot(10L, "u10", 1L);
		when(userAuthorizationGrantMapper.selectRolesByUserId(10L)).thenReturn(List.of("COMMON"));
		when(userAuthorizationGrantMapper.selectPermissionsByUserId(10L)).thenReturn(List.of("system:user:list"));
		ScopeGrant grant = ScopeGrant.builder().scopeType(ScopeGrantType.SELF).build();
		when(deptDataScopeResolver.resolveEffectiveGrant(10L, List.of("COMMON"))).thenReturn(grant);

		AuthProfileQueryService service = new AuthProfileQueryService(userAuthorizationGrantMapper, userReadPort,
				deptDataScopeResolver);

		assertEquals(grant, service.buildByAccount(account).getDeptScope());
		verify(deptDataScopeResolver).resolveEffectiveGrant(10L, List.of("COMMON"));
	}

	@Test
	@DisplayName("管理员写入 ALL 数据范围且不调用 resolver")
	void admin_shouldReceiveAllDeptScope() {
		UserAccountSnapshot account = new UserAccountSnapshot(2L, "admin", 1L);
		when(userAuthorizationGrantMapper.selectRolesByUserId(2L)).thenReturn(List.of("ADMIN"));
		when(userAuthorizationGrantMapper.selectPermissionsByUserId(2L)).thenReturn(List.of("*"));

		AuthProfileQueryService service = new AuthProfileQueryService(userAuthorizationGrantMapper, userReadPort,
				deptDataScopeResolver);

		assertEquals(ScopeGrantType.ALL, service.buildByAccount(account).getDeptScope().getScopeType());
	}

}
