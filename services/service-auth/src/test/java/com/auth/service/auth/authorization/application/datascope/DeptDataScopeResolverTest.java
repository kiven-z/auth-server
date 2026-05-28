package com.auth.service.auth.authorization.application.datascope;

import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.granttable.ScopeGrantType;
import com.auth.service.auth.authorization.infrastructure.persistence.DeptClosureMapper;
import com.auth.service.auth.authorization.infrastructure.persistence.RoleScopeMapper;
import com.auth.service.auth.authorization.infrastructure.persistence.UserScopeMapper;
import com.auth.service.auth.authorization.infrastructure.persistence.projection.RoleScopeProjection;
import com.auth.service.auth.authorization.infrastructure.persistence.projection.UserScopeProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
	private UserScopeMapper userScopeMapper;

	@Mock
	private RoleScopeMapper roleScopeMapper;

	@Mock
	private DeptClosureMapper deptClosureMapper;

	@InjectMocks
	private DeptDataScopeResolver resolver;

	@Test
	@DisplayName("user_scope 存在时覆盖 role_scope")
	void userScope_shouldOverrideRoleScope() {
		UserScopeProjection userScope = new UserScopeProjection();
		userScope.setScopeType("SELF");
		when(userScopeMapper.selectByUserId(10L)).thenReturn(userScope);

		ScopeGrant grant = resolver.resolveEffectiveGrant(10L, List.of("ADMIN"));

		assertEquals(ScopeGrantType.SELF, grant.getScopeType());
		verify(roleScopeMapper, never()).selectByRoleCodes(anyList());
	}

	@Test
	@DisplayName("无 user_scope 时多角色 mergeLoose 取最宽")
	void roleScopes_shouldMergeLoosely() {
		when(userScopeMapper.selectByUserId(10L)).thenReturn(null);

		RoleScopeProjection roleA = new RoleScopeProjection();
		roleA.setRoleCode("R1");
		roleA.setScopeType("DEPT");
		roleA.setScopeDeptIds("[1,2]");

		RoleScopeProjection roleB = new RoleScopeProjection();
		roleB.setRoleCode("R2");
		roleB.setScopeType("DEPT");
		roleB.setScopeDeptIds("[2,3]");

		when(roleScopeMapper.selectByRoleCodes(List.of("R1", "R2"))).thenReturn(List.of(roleA, roleB));

		ScopeGrant grant = resolver.resolveEffectiveGrant(10L, List.of("R1", "R2"));

		assertEquals(ScopeGrantType.CUSTOM, grant.getScopeType());
		assertEquals(List.of("1", "2", "3"), grant.getValues());
	}

	@Test
	@DisplayName("无 user_scope 且无 role_scope 时默认 SELF")
	void noScopeRows_shouldDefaultSelf() {
		when(userScopeMapper.selectByUserId(10L)).thenReturn(null);
		when(roleScopeMapper.selectByRoleCodes(List.of("R1"))).thenReturn(List.of());

		ScopeGrant grant = resolver.resolveEffectiveGrant(10L, List.of("R1"));

		assertEquals(ScopeGrantType.SELF, grant.getScopeType());
	}

	@Test
	@DisplayName("DEPT_AND_CHILD 展开闭包后写入 CUSTOM")
	void deptAndChild_shouldExpandClosure() {
		when(userScopeMapper.selectByUserId(10L)).thenReturn(null);

		RoleScopeProjection roleScope = new RoleScopeProjection();
		roleScope.setRoleCode("R1");
		roleScope.setScopeType("DEPT_AND_CHILD");
		roleScope.setScopeDeptIds("[10]");
		when(roleScopeMapper.selectByRoleCodes(List.of("R1"))).thenReturn(List.of(roleScope));
		when(deptClosureMapper.selectDescendantDeptIdsByAncestorIds(List.of(10L))).thenReturn(List.of(10L, 11L, 12L));

		ScopeGrant grant = resolver.resolveEffectiveGrant(10L, List.of("R1"));

		assertEquals(ScopeGrantType.CUSTOM, grant.getScopeType());
		assertEquals(List.of("10", "11", "12"), grant.getValues());
	}

}
