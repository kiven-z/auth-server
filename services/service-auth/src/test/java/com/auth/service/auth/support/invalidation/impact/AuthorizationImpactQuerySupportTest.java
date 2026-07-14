package com.auth.service.auth.support.invalidation.impact;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.auth.config.AuthorizationInvalidationProperties;
import com.auth.service.auth.mapper.AuthorizationImpactMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link AuthorizationImpactQuerySupport} 单元测试
 */
@DisplayName("AuthorizationImpactQuerySupport 影响面查询")
@ExtendWith(MockitoExtension.class)
class AuthorizationImpactQuerySupportTest {

	@Mock
	private AuthorizationImpactMapper authorizationImpactMapper;

	private AuthorizationInvalidationProperties invalidationProperties;

	private AuthorizationImpactQuerySupport impactQuery;

	@BeforeEach
	void setUp() {
		invalidationProperties = new AuthorizationInvalidationProperties();
		impactQuery = new AuthorizationImpactQuerySupport(authorizationImpactMapper, invalidationProperties);
	}

	@Test
	@DisplayName("空角色码列表不访问数据库")
	void findUserIdsByRoleCodes_emptyInput_shouldNotCallMapper() {

		assertTrue(impactQuery.findUserIdsByRoleCodes(List.of()).isEmpty());
		verifyNoInteractions(authorizationImpactMapper);
	}

	@Test
	@DisplayName("入参去重后查询 Mapper，SQL 层已 DISTINCT 返回结果")
	void findUserIdsByRoleCodes_shouldDistinctBeforeQuery() {
		when(authorizationImpactMapper.selectUserIdsByRoleCodes(List.of("R_A"))).thenReturn(List.of(1L, 2L));

		Set<Long> userIds = impactQuery.findUserIdsByRoleCodes(List.of("R_A", "R_A"));

		assertEquals(Set.of(1L, 2L), userIds);
		verify(authorizationImpactMapper).selectUserIdsByRoleCodes(List.of("R_A"));
	}

	@Test
	@DisplayName("按 grant 主体分组后分别调用对应 Mapper")
	void findUserIdsByGrantSubjects_shouldGroupBySubjectType() {
		when(authorizationImpactMapper.selectUserIdsByGrantUserSubjectIds(List.of(10L))).thenReturn(List.of(10L));
		when(authorizationImpactMapper.selectUserIdsByGrantDeptSubjectIds(List.of(20L))).thenReturn(List.of(21L));
		when(authorizationImpactMapper.selectUserIdsByGrantPostSubjectIds(List.of(30L))).thenReturn(List.of(31L));

		Set<Long> userIds = impactQuery
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

		Set<Long> userIds = impactQuery.findUserIdsByPermissionCodes(List.of("P_A"));

		assertEquals(Set.of(5L), userIds);
		verify(authorizationImpactMapper).selectRoleCodesByPermissionCodes(List.of("P_A"));
		verify(authorizationImpactMapper).selectUserIdsByRoleCodes(List.of("R_A"));
	}

	@Test
	@DisplayName("部门 ID 超过分片大小时应分批查询并合并去重")
	void findUserIdsByDeptIds_largeInput_shouldQueryInChunks() {
		invalidationProperties.setBatchSize(500);
		List<Long> deptIds = IntStream.rangeClosed(1, 1200).mapToObj(Long::valueOf).toList();

		when(authorizationImpactMapper.selectUserIdsByDeptIds(anyList())).thenAnswer(invocation -> {
			List<Long> chunk = invocation.getArgument(0);
			return List.of(chunk.get(0));
		});

		Set<Long> userIds = impactQuery.findUserIdsByLongKeys(deptIds,
				authorizationImpactMapper::selectUserIdsByDeptIds);

		assertEquals(3, userIds.size());
		verify(authorizationImpactMapper, times(3)).selectUserIdsByDeptIds(anyList());
	}

	@Test
	@DisplayName("角色码超过分片大小时应分批查询")
	void findUserIdsByRoleCodes_largeInput_shouldQueryInChunks() {
		invalidationProperties.setBatchSize(2);
		List<String> roleCodes = List.of("R_1", "R_2", "R_3", "R_4", "R_5");

		when(authorizationImpactMapper.selectUserIdsByRoleCodes(anyList())).thenAnswer(invocation -> {
			List<String> chunk = invocation.getArgument(0);
			return List.of((long) chunk.size());
		});

		Set<Long> userIds = impactQuery.findUserIdsByRoleCodes(roleCodes);

		assertEquals(Set.of(1L, 2L), userIds);
		verify(authorizationImpactMapper, times(3)).selectUserIdsByRoleCodes(anyList());
	}

}
