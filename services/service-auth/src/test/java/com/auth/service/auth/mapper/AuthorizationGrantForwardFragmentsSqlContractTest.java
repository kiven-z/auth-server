package com.auth.service.auth.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：正向 DEPT 主体展开须识别部门启用状态。
 */
@DisplayName("AuthorizationGrantForwardFragments SQL 契约")
class AuthorizationGrantForwardFragmentsSqlContractTest {

	private static String readClasspathText() throws IOException {
		try (InputStream in = AuthorizationGrantForwardFragmentsSqlContractTest.class
			.getResourceAsStream("/mapper/AuthorizationGrantForwardFragments.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/AuthorizationGrantForwardFragments.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	@DisplayName("DEPT 正向展开：成员部门与祖先部门均须 status = 1")
	void deptForwardExpansionRequiresEnabledDeptStatus() throws IOException {
		String xml = readClasspathText();

		assertThat(xml).contains("fwdUserSubjectsForFunctionalGrantByUserIds")
			.contains("v_user_dept_effective")
			.contains("da.id = dc.ancestor_id")
			.contains("da.status = 1")
			.doesNotContain("ud.status");
	}

	@Test
	@DisplayName("POST 正向展开：走有效任职 view")
	void postForwardExpansionRequiresEnabledPostStatus() throws IOException {
		String xml = readClasspathText();

		assertThat(xml).contains("fwdUserSubjectsForFunctionalGrantByUserIds")
			.contains("v_user_post_effective")
			.doesNotContain("up.status")
			.doesNotContain("is_deleted");
	}

}
