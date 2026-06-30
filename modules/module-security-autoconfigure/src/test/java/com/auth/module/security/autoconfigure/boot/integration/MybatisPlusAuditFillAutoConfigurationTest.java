package com.auth.module.security.autoconfigure.boot.integration;

import com.auth.common.data.model.BaseEntity;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link MybatisPlusAuditFillAutoConfiguration} 审计字段自动填充测试。
 */
@DisplayName("MybatisPlusAuditFillAutoConfiguration 审计字段填充")
class MybatisPlusAuditFillAutoConfigurationTest {

	private final MybatisPlusAuditFillAutoConfiguration configuration = new MybatisPlusAuditFillAutoConfiguration();

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("updateFill：无登录用户时仍填充 updateTime，updateUser 保持为空")
	void updateFill_withoutAuthenticatedUser_fillsUpdateTimeOnly() {
		BaseEntity entity = new BaseEntity();
		MetaObject metaObject = SystemMetaObject.forObject(entity);

		configuration.updateFill(metaObject);

		assertThat(entity.getUpdateTime()).isNotNull();
		assertThat(entity.getUpdateUser()).isNull();
	}

	@Test
	@DisplayName("updateFill：有登录用户时同时填充 updateTime 与 updateUser")
	void updateFill_withAuthenticatedUser_fillsUpdateTimeAndUser() {
		AuthProfile profile = mock(AuthProfile.class);
		when(profile.getUserId()).thenReturn(42L);
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(profile, null));

		BaseEntity entity = new BaseEntity();
		MetaObject metaObject = SystemMetaObject.forObject(entity);

		configuration.updateFill(metaObject);

		assertThat(entity.getUpdateTime()).isNotNull();
		assertThat(entity.getUpdateUser()).isEqualTo(42L);
	}

}
