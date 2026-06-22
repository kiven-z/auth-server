package com.auth.service.auth.application.authorization.invalidation.datascope;

import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.service.auth.application.port.authorization.DataScopeQueryPort;
import com.auth.service.auth.application.port.authorization.model.RoleScopeRow;
import com.auth.service.auth.application.port.authorization.model.UserScopeRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * {@link DeptDataScopeResolver} 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DeptDataScopeResolverTest {

	@Mock
	private DataScopeQueryPort dataScopeQueryPort;

	@InjectMocks
	private DeptDataScopeResolver resolver;

	@Test
	@DisplayName("user_scope 存在时覆盖 role_scope 且类型与库表一致")
	void userScope_shouldOverrideRoleScope() {
		when(dataScopeQueryPort.findUserScopeByUserId(10L)).thenReturn(Optional.of(new UserScopeRow("DEPT", "[1,2]")));

		ScopeGrant grant = resolver.resolveEffectiveGrant(10L, List.of("ADMIN"));

		assertEquals(DataScopeStorageType.DEPT, grant.getScopeType());
		assertEquals(List.of(1L, 2L), grant.getValues());
		verify(dataScopeQueryPort, never()).findRoleScopesByRoleCodes(anyList());
	}

	@Test
	@DisplayName("user_scope 为 SELF 时仍走 SELF")
	void userScope_self_shouldStaySelf() {
		when(dataScopeQueryPort.findUserScopeByUserId(10L)).thenReturn(Optional.of(new UserScopeRow("SELF", null)));

		ScopeGrant grant = resolver.resolveEffectiveGrant(10L, List.of("ADMIN"));

		assertEquals(DataScopeStorageType.SELF, grant.getScopeType());
	}

	@Test
	@DisplayName("user_scope 为 DEPT_AND_CHILD 时展开闭包后仍为 DEPT_AND_CHILD")
	void userScope_deptAndChild_shouldExpandClosure() {
		when(dataScopeQueryPort.findUserScopeByUserId(10L))
			.thenReturn(Optional.of(new UserScopeRow("DEPT_AND_CHILD", "[10]")));
		when(dataScopeQueryPort.expandDescendantDeptIds(List.of(10L))).thenReturn(List.of(10L, 11L, 12L));

		ScopeGrant grant = resolver.resolveEffectiveGrant(10L, List.of("R1"));

		assertEquals(DataScopeStorageType.DEPT_AND_CHILD, grant.getScopeType());
		assertEquals(List.of(10L, 11L, 12L), grant.getValues());
	}

	@Test
	@DisplayName("无 user_scope 时多角色 DEPT merge 仍为 DEPT")
	void roleScopes_shouldMergeAsDept() {
		when(dataScopeQueryPort.findUserScopeByUserId(10L)).thenReturn(Optional.empty());
		when(dataScopeQueryPort.findRoleScopesByRoleCodes(List.of("R1", "R2")))
			.thenReturn(List.of(new RoleScopeRow("DEPT", "[1,2]"), new RoleScopeRow("DEPT", "[2,3]")));

		ScopeGrant grant = resolver.resolveEffectiveGrant(10L, List.of("R1", "R2"));

		assertEquals(DataScopeStorageType.DEPT, grant.getScopeType());
		assertEquals(List.of(1L, 2L, 3L), grant.getValues());
	}

	@Test
	@DisplayName("无 user_scope 且无 role_scope 时默认 SELF")
	void noScopeRows_shouldDefaultSelf() {
		when(dataScopeQueryPort.findUserScopeByUserId(10L)).thenReturn(Optional.empty());
		when(dataScopeQueryPort.findRoleScopesByRoleCodes(List.of("R1"))).thenReturn(List.of());

		ScopeGrant grant = resolver.resolveEffectiveGrant(10L, List.of("R1"));

		assertEquals(DataScopeStorageType.SELF, grant.getScopeType());
	}

	@Test
	@DisplayName("DEPT_AND_CHILD 展开闭包后仍为 DEPT_AND_CHILD")
	void deptAndChild_shouldExpandClosure() {
		when(dataScopeQueryPort.findUserScopeByUserId(10L)).thenReturn(Optional.empty());
		when(dataScopeQueryPort.findRoleScopesByRoleCodes(List.of("R1")))
			.thenReturn(List.of(new RoleScopeRow("DEPT_AND_CHILD", "[10]")));
		when(dataScopeQueryPort.expandDescendantDeptIds(List.of(10L))).thenReturn(List.of(10L, 11L, 12L));

		ScopeGrant grant = resolver.resolveEffectiveGrant(10L, List.of("R1"));

		assertEquals(DataScopeStorageType.DEPT_AND_CHILD, grant.getScopeType());
		assertEquals(List.of(10L, 11L, 12L), grant.getValues());
	}

	@Test
	@DisplayName("scope_dept_ids 为空时 DEPT fail-close 为空列表")
	void blankScopeDeptIds_shouldMaterializeEmptyValues() {
		when(dataScopeQueryPort.findUserScopeByUserId(10L)).thenReturn(Optional.empty());
		when(dataScopeQueryPort.findRoleScopesByRoleCodes(List.of("R1")))
			.thenReturn(List.of(new RoleScopeRow("DEPT", null)));

		ScopeGrant grant = resolver.resolveEffectiveGrant(10L, List.of("R1"));

		assertEquals(DataScopeStorageType.DEPT, grant.getScopeType());
		assertEquals(List.of(), grant.getValues());
	}

}
