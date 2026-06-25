package com.auth.service.auth.support.authorization;

import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.service.auth.mapper.UserAuthorizationGrantMapper;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.model.value.authorization.UserAccountSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * {@link AuthProfileRepository} 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthProfileRepositoryTest {

	@Mock
	private UserAuthorizationGrantMapper userAuthorizationGrantMapper;

	@Mock
	private UserMapper userMapper;

	@Mock
	private DeptDataScopeResolver deptDataScopeResolver;

	@Test
	@DisplayName("构建画像时注入生效数据权限")
	void buildByAccount_shouldContainResolvedDeptScope() {
		UserAccountSnapshot account = UserAccountSnapshot.builder().id(10L).username("u10").permVersion(1L).build();
		when(userAuthorizationGrantMapper.selectRolesByUserId(10L)).thenReturn(List.of("COMMON"));
		when(userAuthorizationGrantMapper.selectPermissionsByUserId(10L)).thenReturn(List.of("system:user:list"));
		ScopeGrant grant = ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT).values(List.of(2L)).build();
		when(deptDataScopeResolver.resolveEffectiveGrant(10L, List.of("COMMON"))).thenReturn(grant);

		AuthProfileRepository repository = new AuthProfileRepository(userAuthorizationGrantMapper,
				deptDataScopeResolver, userMapper);

		assertEquals(grant, repository.buildByAccount(account).getDeptScope());
		verify(deptDataScopeResolver).resolveEffectiveGrant(10L, List.of("COMMON"));
	}

	@Test
	@DisplayName("管理员写入 ALL 数据范围且不调用 resolver")
	void admin_shouldReceiveAllDeptScope() {
		UserAccountSnapshot account = UserAccountSnapshot.builder().id(2L).username("admin").permVersion(1L).build();
		when(userAuthorizationGrantMapper.selectRolesByUserId(2L)).thenReturn(List.of("ADMIN"));
		when(userAuthorizationGrantMapper.selectPermissionsByUserId(2L)).thenReturn(List.of("*"));

		AuthProfileRepository repository = new AuthProfileRepository(userAuthorizationGrantMapper,
				deptDataScopeResolver, userMapper);

		assertEquals(DataScopeStorageType.ALL, repository.buildByAccount(account).getDeptScope().getScopeType());
		verifyNoInteractions(deptDataScopeResolver);
	}

}
