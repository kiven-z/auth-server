package com.auth.service.auth.authorization.infrastructure.persistence.query;

import com.auth.service.auth.authorization.infrastructure.persistence.AuthorizationImpactMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link AuthorizationImpactQueryService} 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizationImpactQueryService 影响面查询")
class AuthorizationImpactQueryServiceTest {

	@Mock
	private AuthorizationImpactMapper authorizationImpactMapper;

	@Test
	@DisplayName("空角色码列表不访问数据库")
	void findUserIdsByRoleCodes_emptyInput_shouldNotCallMapper() {
		AuthorizationImpactQueryService service = new AuthorizationImpactQueryService(authorizationImpactMapper);

		assertTrue(service.findUserIdsByRoleCodes(List.of()).isEmpty());
		verifyNoInteractions(authorizationImpactMapper);
	}

	@Test
	@DisplayName("去重并过滤空白角色码后查询 Mapper")
	void findUserIdsByRoleCodes_shouldDistinctAndDedupeUserIds() {
		when(authorizationImpactMapper.selectUserIdsByRoleCodes(List.of("R_A"))).thenReturn(List.of(1L, 1L, 2L));

		AuthorizationImpactQueryService service = new AuthorizationImpactQueryService(authorizationImpactMapper);
		Set<Long> userIds = service.findUserIdsByRoleCodes(List.of("R_A", " ", "R_A"));

		assertEquals(Set.of(1L, 2L), userIds);
		verify(authorizationImpactMapper).selectUserIdsByRoleCodes(List.of("R_A"));
	}

}
