package com.auth.service.auth.authorization.application;

import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.granttable.DataScopeKeyUtil;
import com.auth.module.security.contract.api.granttable.ScopeGrantType;
import com.auth.service.auth.authorization.domain.datascope.DataScopeBuildResult;
import com.auth.service.auth.authorization.infrastructure.persistence.UserAuthorizationGrantMapper;
import com.auth.service.auth.authorization.infrastructure.persistence.projection.DataScopeProjection;
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

/**
 * {@link DataScopeEffectiveResolver} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class DataScopeEffectiveResolverTest {

	@Mock
	private UserAuthorizationGrantMapper mapper;

	@Test
	@DisplayName("用户ID为空时返回空结果")
	void nullUserId_shouldReturnEmptyResult() {
		DataScopeEffectiveResolver resolver = new DataScopeEffectiveResolver(mapper);
		DataScopeBuildResult result = resolver.resolveWithTrace(null);
		assertTrue(result.getDataScopes().isEmpty());
		assertTrue(result.getTraces().isEmpty());
	}

	@Test
	@DisplayName("可返回生效范围与轨迹")
	void shouldResolveScopesAndTrace() {
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
