package com.auth.service.auth.mapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：策略码桥接反查须与物理删除语义一致，不按 is_deleted 过滤。
 */
@DisplayName("AuthorizationImpactMapper 策略桥接 SQL 契约")
class AuthorizationImpactMapperPolicySqlContractTest {

	private static String readClasspathText() throws IOException {
		try (InputStream in = AuthorizationImpactMapperPolicySqlContractTest.class
			.getResourceAsStream("/mapper/AuthorizationImpactMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/AuthorizationImpactMapper.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	@DisplayName("策略码桥接角色：经 role_policy 关联，不按 is_deleted 过滤")
	void selectRoleCodesByPolicyCodesShouldNotFilterByLogicalDelete() throws IOException {
		String xml = readClasspathText();

		int selectStart = xml.indexOf("<select id=\"selectRoleCodesByPolicyCodes\"");
		assertThat(selectStart).isNotNegative();
		int selectEnd = xml.indexOf("</select>", selectStart);
		String selectBlock = xml.substring(selectStart, selectEnd);

		assertThat(selectBlock).contains("selectRoleCodesByPolicyCodes")
			.contains("role_policy rp")
			.contains("user_access_policy_definition d")
			.contains("d.policy_code IN")
			.doesNotContain("is_deleted");
	}

	@Test
	@DisplayName("策略码桥接用户：经 user_access_policy_binding 关联，不按 is_deleted 过滤")
	void selectUserIdsByPolicyBindingShouldNotFilterByLogicalDelete() throws IOException {
		String xml = readClasspathText();

		int selectStart = xml.indexOf("<select id=\"selectUserIdsByPolicyBinding\"");
		assertThat(selectStart).isNotNegative();
		int selectEnd = xml.indexOf("</select>", selectStart);
		String selectBlock = xml.substring(selectStart, selectEnd);

		assertThat(selectBlock).contains("selectUserIdsByPolicyBinding")
			.contains("user_access_policy_binding b")
			.contains("user_access_policy_definition d")
			.contains("d.policy_code IN")
			.doesNotContain("is_deleted");
	}

}
