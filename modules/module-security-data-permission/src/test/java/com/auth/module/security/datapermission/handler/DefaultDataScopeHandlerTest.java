package com.auth.module.security.datapermission.handler;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.authorization.ScopeGrantType;
import com.auth.module.security.contract.api.authorization.ScopeMode;
import com.auth.module.security.datapermission.annotation.DataScope;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DefaultDataScopeHandlerTest {

	private static DataScope ann() throws Exception {
		Method m = DefaultDataScopeHandlerTest.class.getDeclaredMethod("annotated");
		return m.getAnnotation(DataScope.class);
	}

	@DataScope
	void annotated() {
	}

	@Test
	void self_shouldUseUserColumn() throws Exception {
		AuthProfile user = AuthProfile.builder()
			.userId(10L)
			.dataScopes(Map.of("dept",
					ScopeGrant.builder().mode(ScopeMode.TREE).scopeType(ScopeGrantType.SELF).values(List.of()).build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(user, ann());
		assertEquals("create_user = 10", cond);
	}

	@Test
	void dept_shouldInDeptIds() throws Exception {
		AuthProfile user = AuthProfile.builder()
			.userId(10L)
			.dataScopes(Map.of("dept",
					ScopeGrant.builder()
						.mode(ScopeMode.TREE)
						.scopeType(ScopeGrantType.NODE_ONLY)
						.values(List.of("1", "2"))
						.build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(user, ann());
		assertEquals("dept_id IN (1,2)", cond);
	}

	@Test
	void emptyDeptIds_shouldFailClosed() throws Exception {
		AuthProfile user = AuthProfile.builder()
			.userId(10L)
			.dataScopes(Map.of("dept",
					ScopeGrant.builder()
						.mode(ScopeMode.TREE)
						.scopeType(ScopeGrantType.NODE_ONLY)
						.values(List.of())
						.build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(user, ann());
		assertEquals("1 = 0", cond);
	}

	@Test
	void all_shouldReturnNull() throws Exception {
		AuthProfile user = AuthProfile.builder()
			.userId(10L)
			.dataScopes(Map.of("dept",
					ScopeGrant.builder().mode(ScopeMode.TREE).scopeType(ScopeGrantType.ALL).values(List.of()).build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(user, ann());
		assertNull(cond);
	}

}
