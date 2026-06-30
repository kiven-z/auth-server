package com.auth.service.auth.support.authorization;

import java.util.List;

import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.service.auth.mapper.DataScopeMapper;
import com.auth.service.auth.mapper.DeptClosureMapper;
import com.auth.service.auth.model.po.scope.RoleScopePO;
import com.auth.service.auth.model.po.scope.UserScopePO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link DeptDataScopeResolver} 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DeptDataScopeResolverTest {

	@Mock
	private DataScopeMapper dataScopeMapper;

	@Mock
	private DeptClosureMapper deptClosureMapper;

	@InjectMocks
	private DeptDataScopeResolver resolver;

	@Test
	@DisplayName("user_scope 存在时覆盖 role_scope 且类型与库表一致")
	void userScope_shouldOverrideRoleScope() {
		UserScopePO projection = new UserScopePO();
		projection.setScopeType("DEPT");
		projection.setScopeDeptIds("[1,2]");
		when(dataScopeMapper.selectByUserId(10L)).thenReturn(projection);

		ScopeGrant grant = resolver.resolveEffectiveGrant(10L, List.of("ADMIN"));

		assertEquals(DataScopeStorageType.DEPT, grant.getScopeType());
		assertEquals(List.of(1L, 2L), grant.getValues());
		verify(dataScopeMapper, never()).selectByRoleCodes(anyList());
	}

	@Test
	@DisplayName("user_scope 为 SELF 时仍走 SELF")
	void userScope_self_shouldStaySelf() {
		UserScopePO projection = new UserScopePO();
		projection.setScopeType("SELF");
		projection.setScopeDeptIds(null);
		when(dataScopeMapper.selectByUserId(10L)).thenReturn(projection);

		ScopeGrant grant = resolver.resolveEffectiveGrant(10L, List.of("ADMIN"));

		assertEquals(DataScopeStorageType.SELF, grant.getScopeType());
	}

	@Test
	@DisplayName("user_scope 为 DEPT_AND_CHILD 时展开闭包后仍为 DEPT_AND_CHILD")
	void userScope_deptAndChild_shouldExpandClosure() {
		UserScopePO projection = new UserScopePO();
		projection.setScopeType("DEPT_AND_CHILD");
		projection.setScopeDeptIds("[10]");
		when(dataScopeMapper.selectByUserId(10L)).thenReturn(projection);
		when(deptClosureMapper.selectDescendantDeptIdsByAncestorIds(List.of(10L))).thenReturn(List.of(10L, 11L, 12L));

		ScopeGrant grant = resolver.resolveEffectiveGrant(10L, List.of("R1"));

		assertEquals(DataScopeStorageType.DEPT_AND_CHILD, grant.getScopeType());
		assertEquals(List.of(10L, 11L, 12L), grant.getValues());
	}

	@Test
	@DisplayName("无 user_scope 时多角色 DEPT merge 仍为 DEPT")
	void roleScopes_shouldMergeAsDept() {
		when(dataScopeMapper.selectByUserId(10L)).thenReturn(null);
		RoleScopePO roleScope1 = new RoleScopePO();
		roleScope1.setScopeType("DEPT");
		roleScope1.setScopeDeptIds("[1,2]");
		RoleScopePO roleScope2 = new RoleScopePO();
		roleScope2.setScopeType("DEPT");
		roleScope2.setScopeDeptIds("[2,3]");
		when(dataScopeMapper.selectByRoleCodes(List.of("R1", "R2"))).thenReturn(List.of(roleScope1, roleScope2));

		ScopeGrant grant = resolver.resolveEffectiveGrant(10L, List.of("R1", "R2"));

		assertEquals(DataScopeStorageType.DEPT, grant.getScopeType());
		assertEquals(List.of(1L, 2L, 3L), grant.getValues());
	}

	@Test
	@DisplayName("无 user_scope 且无 role_scope 时默认 SELF")
	void noScopeRows_shouldDefaultSelf() {
		when(dataScopeMapper.selectByUserId(10L)).thenReturn(null);
		when(dataScopeMapper.selectByRoleCodes(List.of("R1"))).thenReturn(List.of());

		ScopeGrant grant = resolver.resolveEffectiveGrant(10L, List.of("R1"));

		assertEquals(DataScopeStorageType.SELF, grant.getScopeType());
	}

	@Test
	@DisplayName("DEPT_AND_CHILD 展开闭包后仍为 DEPT_AND_CHILD")
	void deptAndChild_shouldExpandClosure() {
		when(dataScopeMapper.selectByUserId(10L)).thenReturn(null);
		RoleScopePO roleScope = new RoleScopePO();
		roleScope.setScopeType("DEPT_AND_CHILD");
		roleScope.setScopeDeptIds("[10]");
		when(dataScopeMapper.selectByRoleCodes(List.of("R1"))).thenReturn(List.of(roleScope));
		when(deptClosureMapper.selectDescendantDeptIdsByAncestorIds(List.of(10L))).thenReturn(List.of(10L, 11L, 12L));

		ScopeGrant grant = resolver.resolveEffectiveGrant(10L, List.of("R1"));

		assertEquals(DataScopeStorageType.DEPT_AND_CHILD, grant.getScopeType());
		assertEquals(List.of(10L, 11L, 12L), grant.getValues());
	}

	@Test
	@DisplayName("scope_dept_ids 为空时 DEPT fail-close 为空列表")
	void blankScopeDeptIds_shouldMaterializeEmptyValues() {
		when(dataScopeMapper.selectByUserId(10L)).thenReturn(null);
		RoleScopePO roleScope = new RoleScopePO();
		roleScope.setScopeType("DEPT");
		roleScope.setScopeDeptIds(null);
		when(dataScopeMapper.selectByRoleCodes(List.of("R1"))).thenReturn(List.of(roleScope));

		ScopeGrant grant = resolver.resolveEffectiveGrant(10L, List.of("R1"));

		assertEquals(DataScopeStorageType.DEPT, grant.getScopeType());
		assertEquals(List.of(), grant.getValues());
	}

}
