package com.auth.module.security.datapermission.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeDimension;
import com.auth.module.security.contract.api.granttable.ScopeGrantType;
import com.auth.module.security.datapermission.annotation.DataScope;

class DefaultDataScopeHandlerTest {

	private static DataScope defaultAnn() throws Exception {
		Method method = DefaultDataScopeHandlerTest.class.getDeclaredMethod("annotatedDefault");
		return method.getAnnotation(DataScope.class);
	}

	private static DataScope orderAnn() throws Exception {
		Method method = DefaultDataScopeHandlerTest.class.getDeclaredMethod("annotatedOrder");
		return method.getAnnotation(DataScope.class);
	}

	@DataScope
	void annotatedDefault() {
	}

	@DataScope(resourceCode = "order")
	void annotatedOrder() {
	}

	@DataScope(ignoreSuperAdmin = true)
	void annotatedIgnoreAdmin() {
	}

	@Test
	@DisplayName("SELF 范围使用 userColumn 并拼接别名")
	void self_shouldUseUserColumn() throws Exception {
		// 构造默认资源键命中 SELF 的画像
		AuthProfile user = AuthProfile.builder()
			.userId(10L)
			.dataScopes(Map.of(DataScopeDimension.DEPT.defaultProfileKey(),
					ScopeGrant.builder().scopeType(ScopeGrantType.SELF).values(List.of()).build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(user, defaultAnn());
		assertEquals("d.create_user = 10", cond);
	}

	@Test
	@DisplayName("NODE 范围使用 dimensionColumn 生成 IN 条件")
	void dept_shouldInDeptIds() throws Exception {
		// 构造部门节点范围，验证生成维度 IN 条件
		AuthProfile profile = AuthProfile.builder()
			.userId(10L)
			.dataScopes(Map.of(DataScopeDimension.DEPT.defaultProfileKey(),
					ScopeGrant.builder().scopeType(ScopeGrantType.NODE_ONLY).values(List.of("1", "2")).build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, defaultAnn());
		assertEquals("d.dept_id IN (1,2)", cond);
	}

	@Test
	@DisplayName("受限范围 values 为空时 fail-close")
	void emptyDeptIds_shouldFailClosed() throws Exception {
		// 构造受限但无值场景，应该拒绝放开查询
		AuthProfile profile = AuthProfile.builder()
			.userId(10L)
			.dataScopes(Map.of(DataScopeDimension.DEPT.defaultProfileKey(),
					ScopeGrant.builder().scopeType(ScopeGrantType.NODE_ONLY).values(List.of()).build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, defaultAnn());
		assertEquals("1 = 0", cond);
	}

	@Test
	@DisplayName("ALL 范围不追加条件")
	void all_shouldReturnNull() throws Exception {
		// 构造 ALL 场景，SQL 不应附加过滤
		AuthProfile profile = AuthProfile.builder()
			.userId(10L)
			.dataScopes(Map.of(DataScopeDimension.DEPT.defaultProfileKey(),
					ScopeGrant.builder().scopeType(ScopeGrantType.ALL).values(List.of()).build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, defaultAnn());
		assertNull(cond);
	}

	@Test
	@DisplayName("管理员且忽略开关关闭时直接放行")
	void superAdmin_shouldBypassWhenIgnoreFlagIsFalse() throws Exception {
		// 超级管理员且 ignoreSuperAdmin=false 时应直接跳过过滤
		AuthProfile profile = AuthProfile.builder()
			.userId(1L)
			.dataScopes(Map.of(DataScopeDimension.DEPT.defaultProfileKey(),
					ScopeGrant.builder().scopeType(ScopeGrantType.SELF).values(List.of()).build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, defaultAnn());
		assertNull(cond);
	}

	@Test
	@DisplayName("管理员忽略开关开启时按普通用户计算")
	void superAdmin_shouldApplyConditionWhenIgnoreFlagIsTrue() throws Exception {
		// 超级管理员但 ignoreSuperAdmin=true 时要继续走范围策略
		Method method = DefaultDataScopeHandlerTest.class.getDeclaredMethod("annotatedIgnoreAdmin");
		DataScope ann = method.getAnnotation(DataScope.class);
		AuthProfile profile = AuthProfile.builder()
			.userId(1L)
			.dataScopes(Map.of(DataScopeDimension.DEPT.defaultProfileKey(),
					ScopeGrant.builder().scopeType(ScopeGrantType.SELF).values(List.of()).build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, ann);
		assertEquals("d.create_user = 1", cond);
	}

	@Test
	@DisplayName("按 resourceCode 命中非默认范围键")
	void orderResource_shouldUseSpecificScopeKey() throws Exception {
		// 注解声明 resourceCode=order 时应命中 DEPT::order
		AuthProfile profile = AuthProfile.builder()
			.userId(10L)
			.dataScopes(Map.of(DataScopeDimension.DEPT.profileKey("order"),
					ScopeGrant.builder().scopeType(ScopeGrantType.CUSTOM).values(List.of("8", "9")).build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, orderAnn());
		assertEquals("d.dept_id IN (8,9)", cond);
	}

}
