package com.auth.service.auth.infrastructure.profile;

import com.auth.module.platform.persistence.UserEntity;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.service.auth.infrastructure.profile.datascope.DataScopeEffectiveResolver;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.mapper.UserRolePermissionDataScopePolicyMapper;
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

@ExtendWith(MockitoExtension.class)
class AuthProfileServiceTest {

	@Mock
	private UserRolePermissionDataScopePolicyMapper userRolePermissionDataScopePolicyMapper;

	@Mock
	private UserMapper userMapper;

	@Mock
	private DataScopeEffectiveResolver dataScopeEffectiveResolver;

	@Test
	@DisplayName("构建画像时注入生效数据权限")
	void buildByUser_shouldContainResolvedDataScopes() {
		// 构造用户、角色和权限，验证 dataScopes 来自 resolver
		UserEntity user = new UserEntity();
		user.setId(10L);
		user.setUsername("u10");
		when(userRolePermissionDataScopePolicyMapper.selectRolesByUserId(10L)).thenReturn(List.of("COMMON"));
		when(userRolePermissionDataScopePolicyMapper.selectPermissionsByUserId(10L))
			.thenReturn(List.of("system:user:list"));
		ScopeGrant grant = ScopeGrant.builder().build();
		when(dataScopeEffectiveResolver.resolveByUserId(10L)).thenReturn(Map.of("DEPT::default", grant));

		AuthProfileService service = new AuthProfileService(userRolePermissionDataScopePolicyMapper, userMapper,
				dataScopeEffectiveResolver);
		Map<String, ScopeGrant> dataScopes = service.buildByUser(user).getDataScopes();

		assertEquals(1, dataScopes.size());
		assertEquals(grant, dataScopes.get("DEPT::default"));
		verify(dataScopeEffectiveResolver).resolveByUserId(10L);
	}

}
