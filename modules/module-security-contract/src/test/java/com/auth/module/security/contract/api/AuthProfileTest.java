package com.auth.module.security.contract.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.auth.module.security.contract.api.authorization.AuthProfile;

class AuthProfileTest {

	@Test
	@DisplayName("测试默认构建器应返回非空且为空的角色、权限和数据范围")
	void builder_defaultsShouldBeNonNullAndEmpty() {
		AuthProfile p = AuthProfile.builder().userId(1L).username("u1").build();

		assertNotNull(p.getRoles());
		assertTrue(p.getRoles().isEmpty());

		assertNotNull(p.getPermissions());
		assertTrue(p.getPermissions().isEmpty());

		assertNotNull(p.getPolicies());

		assertNull(p.getDeptScope());
	}

}
