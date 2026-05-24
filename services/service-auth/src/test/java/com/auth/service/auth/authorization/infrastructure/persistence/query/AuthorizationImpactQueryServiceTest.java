package com.auth.service.auth.authorization.infrastructure.persistence.query;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
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

	@Test
	@DisplayName("按 grant 主体分组后分别调用对应 Mapper")
	void findUserIdsByGrantSubjects_shouldGroupBySubjectType() {
		when(authorizationImpactMapper.selectUserIdsByGrantUserSubjectIds(List.of(10L))).thenReturn(List.of(10L));
		when(authorizationImpactMapper.selectUserIdsByGrantDeptSubjectIds(List.of(20L))).thenReturn(List.of(21L));
		when(authorizationImpactMapper.selectUserIdsByGrantPostSubjectIds(List.of(30L))).thenReturn(List.of(31L));

		AuthorizationImpactQueryService service = new AuthorizationImpactQueryService(authorizationImpactMapper);
		Set<Long> userIds = service
			.findUserIdsByGrantSubjects(List.of(new GrantSubjectKey(GrantTableSubjectType.USER, 10L),
					new GrantSubjectKey(GrantTableSubjectType.DEPT, 20L),
					new GrantSubjectKey(GrantTableSubjectType.POST, 30L)));

		assertEquals(Set.of(10L, 21L, 31L), userIds);
		verify(authorizationImpactMapper).selectUserIdsByGrantUserSubjectIds(List.of(10L));
		verify(authorizationImpactMapper).selectUserIdsByGrantDeptSubjectIds(List.of(20L));
		verify(authorizationImpactMapper).selectUserIdsByGrantPostSubjectIds(List.of(30L));
	}

	@Test
	@DisplayName("权限码先桥接角色码再反查用户")
	void findUserIdsByPermissionCodes_shouldBridgeToRoleCodes() {
		when(authorizationImpactMapper.selectRoleCodesByPermissionCodes(List.of("P_A"))).thenReturn(List.of("R_A"));
		when(authorizationImpactMapper.selectUserIdsByRoleCodes(List.of("R_A"))).thenReturn(List.of(5L));

		AuthorizationImpactQueryService service = new AuthorizationImpactQueryService(authorizationImpactMapper);
		Set<Long> userIds = service.findUserIdsByPermissionCodes(List.of("P_A"));

		assertEquals(Set.of(5L), userIds);
		verify(authorizationImpactMapper).selectRoleCodesByPermissionCodes(List.of("P_A"));
		verify(authorizationImpactMapper).selectUserIdsByRoleCodes(List.of("R_A"));
	}

	@Test
	@DisplayName("策略码合并角色桥接与直连用户")
	void findUserIdsByPolicyCodes_shouldUnionRoleAndDirectUsers() {
		when(authorizationImpactMapper.selectRoleCodesByPolicyCodes(List.of("POL_A"))).thenReturn(List.of("R_A"));
		when(authorizationImpactMapper.selectUserIdsByRoleCodes(List.of("R_A"))).thenReturn(List.of(1L));
		when(authorizationImpactMapper.selectUserIdsByPolicyBinding(List.of("POL_A"))).thenReturn(List.of(2L));

		AuthorizationImpactQueryService service = new AuthorizationImpactQueryService(authorizationImpactMapper);
		Set<Long> userIds = service.findUserIdsByPolicyCodes(List.of("POL_A"));

		assertEquals(Set.of(1L, 2L), userIds);
	}

}
