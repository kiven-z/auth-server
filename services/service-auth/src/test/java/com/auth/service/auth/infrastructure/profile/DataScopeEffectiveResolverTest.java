package com.auth.service.auth.infrastructure.profile;

import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.granttable.DataScopeKeyUtil;
import com.auth.module.security.contract.api.granttable.ScopeGrantType;
import com.auth.service.auth.infrastructure.profile.datascope.DataScopeBuildResult;
import com.auth.service.auth.infrastructure.profile.datascope.DataScopeEffectiveResolver;
import com.auth.service.auth.mapper.UserAuthorizationGrantMapper;
import com.auth.service.auth.model.projection.DataScopeProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataScopeEffectiveResolverTest {

	@Mock
	private UserAuthorizationGrantMapper mapper;

	@Test
	@DisplayName("用户ID为空时返回空结果")
	void nullUserId_shouldReturnEmptyResult() {
		// userId 为空时不查询数据库，直接返回空范围
		DataScopeEffectiveResolver resolver = new DataScopeEffectiveResolver(mapper);
		DataScopeBuildResult result = resolver.resolveWithTrace(null);
		assertTrue(result.getDataScopes().isEmpty());
		assertTrue(result.getTraces().isEmpty());
	}

	@Test
	@DisplayName("可返回生效范围与轨迹")
	void shouldResolveScopesAndTrace() {
		// 模拟角色范围片段，验证 resolver 能产出生效 map 与轨迹
		DataScopeProjection projection = new DataScopeProjection();
		projection.setSubjectType("ROLE");
		projection.setScopeResourceCode("order");
		projection.setDimensionType("DEPT");
		projection.setScopeType("NODE_ONLY");
		projection.setScopeSubjectIds("[\"7\"]");
		when(mapper.selectEffectiveDataScopesByUserId(10L)).thenReturn(List.of(projection));

		DataScopeEffectiveResolver resolver = new DataScopeEffectiveResolver(mapper);
		DataScopeBuildResult result = resolver.resolveWithTrace(10L);
		Map<String, ScopeGrant> scopes = result.getDataScopes();

		assertEquals(ScopeGrantType.CUSTOM, scopes.get(DataScopeKeyUtil.compose("DEPT", "order")).getScopeType());
		assertEquals(1, result.getTraces().size());
		verify(mapper, times(1)).selectEffectiveDataScopesByUserId(10L);
	}

}
