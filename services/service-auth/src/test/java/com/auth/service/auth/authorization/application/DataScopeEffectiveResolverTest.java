package com.auth.service.auth.authorization.application;

import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeKeys;
import com.auth.module.security.contract.api.granttable.ScopeGrantType;
import com.auth.service.auth.authorization.application.datascope.DeptDataScopeResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * {@link DataScopeEffectiveResolver} 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DataScopeEffectiveResolverTest {

	@Mock
	private DeptDataScopeResolver deptDataScopeResolver;

	@Test
	@DisplayName("用户ID为空时返回空 Map")
	void nullUserId_shouldReturnEmptyMap() {
		DataScopeEffectiveResolver resolver = new DataScopeEffectiveResolver(deptDataScopeResolver);
		assertTrue(resolver.resolveByUserIdAndRoles(null, List.of("R1")).isEmpty());
	}

	@Test
	@DisplayName("返回单键 DEPT::default 的生效范围")
	void shouldReturnSingleKeyMap() {
		ScopeGrant selfGrant = ScopeGrant.builder().scopeType(ScopeGrantType.SELF).build();
		when(deptDataScopeResolver.resolveEffectiveGrant(10L, List.of("R1"))).thenReturn(selfGrant);

		DataScopeEffectiveResolver resolver = new DataScopeEffectiveResolver(deptDataScopeResolver);
		Map<String, ScopeGrant> scopes = resolver.resolveByUserIdAndRoles(10L, List.of("R1"));

		assertEquals(1, scopes.size());
		assertEquals(selfGrant, scopes.get(DataScopeKeys.DEPT_DEFAULT));
	}

}
