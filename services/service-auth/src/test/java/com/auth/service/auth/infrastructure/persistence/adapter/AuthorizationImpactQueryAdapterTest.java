package com.auth.service.auth.infrastructure.persistence.adapter;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.auth.config.AuthorizationInvalidationProperties;
import com.auth.service.auth.infrastructure.persistence.AuthorizationImpactMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * {@link AuthorizationImpactQueryAdapter} 单元测试
 */
@DisplayName("AuthorizationImpactQueryAdapter 影响面查询")
@ExtendWith(MockitoExtension.class)
class AuthorizationImpactQueryAdapterTest {

	@Mock
	private AuthorizationImpactMapper authorizationImpactMapper;

	private AuthorizationInvalidationProperties invalidationProperties;

	private AuthorizationImpactQueryAdapter adapter;

	@BeforeEach
	void setUp() {
		invalidationProperties = new AuthorizationInvalidationProperties();
		adapter = new AuthorizationImpactQueryAdapter(authorizationImpactMapper, invalidationProperties);
	}

	@Test
	@DisplayName("空角色码列表不访问数据库")
	void findUserIdsByRoleCodes_emptyInput_shouldNotCallMapper() {

		assertTrue(adapter.findUserIdsByRoleCodes(List.of()).isEmpty());
		verifyNoInteractions(authorizationImpactMapper);
	}

	@Test
	@DisplayName("入参去重后查询 Mapper，SQL 层已 DISTINCT 返回结果")
	void findUserIdsByRoleCodes_shouldDistinctBeforeQuery() {
		when(authorizationImpactMapper.selectUserIdsByRoleCodes(List.of("R_A"))).thenReturn(List.of(1L, 2L));

		Set<Long> userIds = adapter.findUserIdsByRoleCodes(List.of("R_A", "R_A"));

		assertEquals(Set.of(1L, 2L), userIds);
		verify(authorizationImpactMapper).selectUserIdsByRoleCodes(List.of("R_A"));
	}

	@Test
	@DisplayName("按 grant 主体分组后分别调用对应 Mapper")
	void findUserIdsByGrantSubjects_shouldGroupBySubjectType() {
		when(authorizationImpactMapper.selectUserIdsByGrantUserSubjectIds(List.of(10L))).thenReturn(List.of(10L));
		when(authorizationImpactMapper.selectUserIdsByGrantDeptSubjectIds(List.of(20L))).thenReturn(List.of(21L));
		when(authorizationImpactMapper.selectUserIdsByGrantPostSubjectIds(List.of(30L))).thenReturn(List.of(31L));

		Set<Long> userIds = adapter
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

		Set<Long> userIds = adapter.findUserIdsByPermissionCodes(List.of("P_A"));

		assertEquals(Set.of(5L), userIds);
		verify(authorizationImpactMapper).selectRoleCodesByPermissionCodes(List.of("P_A"));
		verify(authorizationImpactMapper).selectUserIdsByRoleCodes(List.of("R_A"));
	}

	@Test
	@DisplayName("策略码：桥接关闭时仅查询直连绑定")
	void findUserIdsByPolicyCodes_bridgeDisabled_shouldOnlyQueryBinding() {
		invalidationProperties.setPolicyRoleBridgeEnabled(false);
		when(authorizationImpactMapper.selectUserIdsByPolicyBinding(List.of("POL_A"))).thenReturn(List.of(2L));

		Set<Long> userIds = adapter.findUserIdsByPolicyCodes(List.of("POL_A"));

		assertEquals(Set.of(2L), userIds);
		verify(authorizationImpactMapper, never()).selectRoleCodesByPolicyCodes(any());
	}

	@Test
	@DisplayName("策略码：桥接开启时合并角色与直连用户")
	void findUserIdsByPolicyCodes_bridgeEnabled_shouldUnionRoleAndDirectUsers() {
		invalidationProperties.setPolicyRoleBridgeEnabled(true);
		when(authorizationImpactMapper.selectRoleCodesByPolicyCodes(List.of("POL_A"))).thenReturn(List.of("R_A"));
		when(authorizationImpactMapper.selectUserIdsByRoleCodes(List.of("R_A"))).thenReturn(List.of(1L));
		when(authorizationImpactMapper.selectUserIdsByPolicyBinding(List.of("POL_A"))).thenReturn(List.of(2L));

		Set<Long> userIds = adapter.findUserIdsByPolicyCodes(List.of("POL_A"));

		assertEquals(Set.of(1L, 2L), userIds);
	}

}
