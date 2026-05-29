package com.auth.module.security.datapermission.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.module.security.datapermission.annotation.DataScope;

class DefaultDataScopeHandlerTest {

	private static DataScope defaultAnn() throws Exception {
		Method method = DefaultDataScopeHandlerTest.class.getDeclaredMethod("annotatedDefault");
		return method.getAnnotation(DataScope.class);
	}

	@DataScope
	void annotatedDefault() {
	}

	@DataScope(ignoreSuperAdmin = true)
	void annotatedIgnoreAdmin() {
	}

	@Test
	@DisplayName("SELF 范围使用 userColumn 并拼接别名")
	void self_shouldUseUserColumn() throws Exception {
		AuthProfile user = AuthProfile.builder()
			.userId(10L)
			.deptScope(ScopeGrant.builder().scopeType(DataScopeStorageType.SELF).values(List.of()).build())
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(user, defaultAnn());
		assertEquals("d.create_user = 10", cond);
	}

	@Test
	@DisplayName("DEPT 范围使用 dimensionColumn 生成 IN 条件")
	void dept_shouldInDeptIds() throws Exception {
		AuthProfile profile = AuthProfile.builder()
			.userId(10L)
			.deptScope(ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT).values(List.of(1L, 2L)).build())
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, defaultAnn());
		assertEquals("d.dept_id IN (1,2)", cond);
	}

	@Test
	@DisplayName("DEPT_AND_CHILD 范围使用已展开 ID 列表")
	void deptAndChild_shouldInDeptIds() throws Exception {
		AuthProfile profile = AuthProfile.builder()
			.userId(10L)
			.deptScope(ScopeGrant.builder()
				.scopeType(DataScopeStorageType.DEPT_AND_CHILD)
				.values(List.of(1L, 2L, 3L))
				.build())
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, defaultAnn());
		assertEquals("d.dept_id IN (1,2,3)", cond);
	}

	@Test
	@DisplayName("受限范围 values 为空时 fail-close")
	void emptyDeptIds_shouldFailClosed() throws Exception {
		AuthProfile profile = AuthProfile.builder()
			.userId(10L)
			.deptScope(ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT).values(List.of()).build())
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, defaultAnn());
		assertEquals("1 = 0", cond);
	}

	@Test
	@DisplayName("ALL 范围不追加条件")
	void all_shouldReturnNull() throws Exception {
		AuthProfile profile = AuthProfile.builder()
			.userId(10L)
			.deptScope(ScopeGrant.builder().scopeType(DataScopeStorageType.ALL).values(List.of()).build())
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, defaultAnn());
		assertNull(cond);
	}

	@Test
	@DisplayName("管理员且忽略开关关闭时直接放行")
	void superAdmin_shouldBypassWhenIgnoreFlagIsFalse() throws Exception {
		AuthProfile profile = AuthProfile.builder()
			.userId(1L)
			.deptScope(ScopeGrant.builder().scopeType(DataScopeStorageType.SELF).values(List.of()).build())
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, defaultAnn());
		assertNull(cond);
	}

	@Test
	@DisplayName("管理员忽略开关开启时按普通用户计算")
	void superAdmin_shouldApplyConditionWhenIgnoreFlagIsTrue() throws Exception {
		Method method = DefaultDataScopeHandlerTest.class.getDeclaredMethod("annotatedIgnoreAdmin");
		DataScope ann = method.getAnnotation(DataScope.class);
		AuthProfile profile = AuthProfile.builder()
			.userId(1L)
			.deptScope(ScopeGrant.builder().scopeType(DataScopeStorageType.SELF).values(List.of()).build())
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, ann);
		assertEquals("d.create_user = 1", cond);
	}

	@Test
	@DisplayName("deptScope 为空时默认 SELF")
	void nullDeptScope_shouldDefaultToSelf() throws Exception {
		AuthProfile profile = AuthProfile.builder().userId(10L).build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, defaultAnn());
		assertEquals("d.create_user = 10", cond);
	}

}
