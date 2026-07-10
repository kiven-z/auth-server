package com.auth.service.auth.support.authorization;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.service.auth.mapper.DataScopeMapper;
import com.auth.service.auth.mapper.UserAuthorizationGrantMapper;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.model.po.authorization.UserGrantCodeRowPO;
import com.auth.service.auth.model.value.authorization.UserAccountSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
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
	private DataScopeMapper dataScopeMapper;

	@Mock
	private DeptDataScopeResolver deptDataScopeResolver;

	private static UserGrantCodeRowPO roleRow(Long userId, String code) {
		UserGrantCodeRowPO row = new UserGrantCodeRowPO();
		row.setUserId(userId);
		row.setCode(code);
		return row;
	}

	private static UserGrantCodeRowPO permissionRow(Long userId, String code) {
		UserGrantCodeRowPO row = new UserGrantCodeRowPO();
		row.setUserId(userId);
		row.setCode(code);
		return row;
	}

	@Test
	@DisplayName("构建画像时注入生效数据权限")
	void buildByAccount_shouldContainResolvedDeptScope() {
		UserAccountSnapshot account = UserAccountSnapshot.builder().id(10L).username("u10").permVersion(1L).build();
		stubBatchUser(10L, "u10");
		when(userAuthorizationGrantMapper.selectRoleRowsByUserIds(List.of(10L)))
			.thenReturn(List.of(roleRow(10L, "COMMON")));
		when(userAuthorizationGrantMapper.selectPermissionRowsByUserIds(List.of(10L)))
			.thenReturn(List.of(permissionRow(10L, "system:user:list")));
		when(dataScopeMapper.selectByUserIds(List.of(10L))).thenReturn(List.of());
		when(dataScopeMapper.selectByRoleCodes(List.of("COMMON"))).thenReturn(List.of());
		ScopeGrant grant = ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT).values(List.of(2L)).build();
		when(deptDataScopeResolver.resolveEffectiveGrants(List.of(10L), Map.of(10L, List.of("COMMON")), Map.of(),
				List.of()))
			.thenReturn(Map.of(10L, grant));

		AuthProfileRepository repository = new AuthProfileRepository(userAuthorizationGrantMapper,
				deptDataScopeResolver, userMapper, dataScopeMapper);

		assertEquals(grant, repository.buildByAccount(account).getDeptScope());
		verify(deptDataScopeResolver).resolveEffectiveGrants(List.of(10L), Map.of(10L, List.of("COMMON")), Map.of(),
				List.of());
	}

	@Test
	@DisplayName("管理员写入 ALL 数据范围且不调用 resolver")
	void admin_shouldReceiveAllDeptScope() {
		UserAccountSnapshot account = UserAccountSnapshot.builder().id(2L).username("admin").permVersion(1L).build();
		stubBatchUser(2L, "admin");
		when(userAuthorizationGrantMapper.selectRoleRowsByUserIds(List.of(2L)))
			.thenReturn(List.of(roleRow(2L, "ADMIN")));
		when(userAuthorizationGrantMapper.selectPermissionRowsByUserIds(List.of(2L)))
			.thenReturn(List.of(permissionRow(2L, "*")));
		when(dataScopeMapper.selectByUserIds(List.of(2L))).thenReturn(List.of());

		AuthProfileRepository repository = new AuthProfileRepository(userAuthorizationGrantMapper,
				deptDataScopeResolver, userMapper, dataScopeMapper);

		assertEquals(DataScopeStorageType.ALL, repository.buildByAccount(account).getDeptScope().getScopeType());
		verifyNoInteractions(deptDataScopeResolver);
	}

	@Test
	@DisplayName("批量构建画像时按用户 ID 批量查询授权数据")
	void buildByUserIds_shouldUseBatchQueries() {
		stubBatchUsers(List.of(10L, 11L));
		when(userAuthorizationGrantMapper.selectRoleRowsByUserIds(anyList())).thenReturn(List.of());
		when(userAuthorizationGrantMapper.selectPermissionRowsByUserIds(anyList())).thenReturn(List.of());
		when(dataScopeMapper.selectByUserIds(anyList())).thenReturn(List.of());
		when(deptDataScopeResolver.resolveEffectiveGrants(anyList(), anyMap(), anyMap(), anyList()))
			.thenReturn(Map.of());

		AuthProfileRepository repository = new AuthProfileRepository(userAuthorizationGrantMapper,
				deptDataScopeResolver, userMapper, dataScopeMapper);

		assertEquals(2, repository.buildByUserIds(List.of(10L, 11L)).size());
		verify(userAuthorizationGrantMapper).selectRoleRowsByUserIds(List.of(10L, 11L));
		verify(userAuthorizationGrantMapper).selectPermissionRowsByUserIds(List.of(10L, 11L));
		verify(dataScopeMapper).selectByUserIds(List.of(10L, 11L));
	}

	private void stubBatchUser(Long userId, String username) {
		stubBatchUsers(List.of(userId));
		UserEntity user = new UserEntity();
		user.setId(userId);
		user.setUsername(username);
		user.setPermVersion(1L);
		when(userMapper.selectByIds(List.of(userId))).thenReturn(List.of(user));
	}

	private void stubBatchUsers(List<Long> userIds) {
		List<UserEntity> users = userIds.stream().map(userId -> {
			UserEntity user = new UserEntity();
			user.setId(userId);
			user.setUsername("u" + userId);
			user.setPermVersion(1L);
			return user;
		}).toList();
		when(userMapper.selectByIds(userIds)).thenReturn(users);
	}

}
