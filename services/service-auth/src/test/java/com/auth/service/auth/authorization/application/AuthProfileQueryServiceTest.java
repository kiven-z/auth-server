package com.auth.service.auth.authorization.application;

import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeKeys;
import com.auth.module.security.contract.api.granttable.ScopeGrantType;
import com.auth.service.auth.authorization.domain.model.UserAccountSnapshot;
import com.auth.service.auth.authorization.domain.port.UserReadPort;
import com.auth.service.auth.authorization.infrastructure.persistence.UserAuthorizationGrantMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

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
	private DataScopeEffectiveResolver dataScopeEffectiveResolver;

	@Test
	@DisplayName("构建画像时注入生效数据权限")
	void buildByAccount_shouldContainResolvedDataScopes() {
		UserAccountSnapshot account = new UserAccountSnapshot(10L, "u10", 1L);
		when(userAuthorizationGrantMapper.selectRolesByUserId(10L)).thenReturn(List.of("COMMON"));
		when(userAuthorizationGrantMapper.selectPermissionsByUserId(10L)).thenReturn(List.of("system:user:list"));
		ScopeGrant grant = ScopeGrant.builder().scopeType(ScopeGrantType.SELF).build();
		when(dataScopeEffectiveResolver.resolveByUserIdAndRoles(10L, List.of("COMMON")))
			.thenReturn(Map.of(DataScopeKeys.DEPT_DEFAULT, grant));

		AuthProfileQueryService service = new AuthProfileQueryService(userAuthorizationGrantMapper, userReadPort,
				dataScopeEffectiveResolver);
		Map<String, ScopeGrant> dataScopes = service.buildByAccount(account).getDataScopes();

		assertEquals(1, dataScopes.size());
		assertEquals(grant, dataScopes.get(DataScopeKeys.DEPT_DEFAULT));
		verify(dataScopeEffectiveResolver).resolveByUserIdAndRoles(10L, List.of("COMMON"));
	}

	@Test
	@DisplayName("管理员写入 ALL 数据范围且不调用 resolver")
	void admin_shouldReceiveAllDataScope() {
		UserAccountSnapshot account = new UserAccountSnapshot(2L, "admin", 1L);
		when(userAuthorizationGrantMapper.selectRolesByUserId(2L)).thenReturn(List.of("ADMIN"));
		when(userAuthorizationGrantMapper.selectPermissionsByUserId(2L)).thenReturn(List.of("*"));

		AuthProfileQueryService service = new AuthProfileQueryService(userAuthorizationGrantMapper, userReadPort,
				dataScopeEffectiveResolver);
		Map<String, ScopeGrant> dataScopes = service.buildByAccount(account).getDataScopes();

		assertEquals(ScopeGrantType.ALL, dataScopes.get(DataScopeKeys.DEPT_DEFAULT).getScopeType());
	}

}
