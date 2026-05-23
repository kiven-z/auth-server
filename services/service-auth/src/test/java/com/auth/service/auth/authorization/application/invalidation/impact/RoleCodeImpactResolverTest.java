package com.auth.service.auth.authorization.application.invalidation.impact;

import com.auth.service.auth.authorization.domain.enums.AuthorizationChangeKind;
import com.auth.service.auth.authorization.domain.invalidation.model.RoleCodes;
import com.auth.service.auth.authorization.domain.invalidation.port.AuthorizationImpactQueryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link RoleCodeImpactResolver} 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleCodeImpactResolver 角色码影响面")
class RoleCodeImpactResolverTest {

	@Mock
	private AuthorizationImpactQueryPort authorizationImpactQueryPort;

	@Test
	@DisplayName("委托端口按角色码反查用户 ID")
	void resolve_shouldDelegateToQueryPort() {
		when(authorizationImpactQueryPort.findUserIdsByRoleCodes(List.of("R_A", "R_B"))).thenReturn(Set.of(10L, 20L));

		RoleCodeImpactResolver resolver = new RoleCodeImpactResolver(authorizationImpactQueryPort);
		Set<Long> userIds = resolver.resolve(new RoleCodes(List.of("R_A", "R_B")));

		assertEquals(Set.of(10L, 20L), userIds);
		verify(authorizationImpactQueryPort).findUserIdsByRoleCodes(List.of("R_A", "R_B"));
	}

	@Test
	@DisplayName("元数据：ROLE 维度与 RoleCodes 类型")
	void metadata_shouldMatchRoleKind() {
		RoleCodeImpactResolver resolver = new RoleCodeImpactResolver(authorizationImpactQueryPort);

		assertEquals(AuthorizationChangeKind.ROLE, resolver.kind());
		assertEquals(RoleCodes.class, resolver.targetType());
	}

}
