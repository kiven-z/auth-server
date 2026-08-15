package com.auth.service.system.admin.mapper.authorization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：角色影响用户展开须与 auth 影响面同源
 *
 * @author Bunny
 */
@DisplayName("RoleImpactQueryMapper SQL 契约")
class RoleImpactQueryMapperSqlContractTest {

	private static String readClasspathText() throws IOException {
		try (InputStream in = RoleImpactQueryMapperSqlContractTest.class
			.getResourceAsStream("/mapper/authorization/RoleImpactQueryMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/authorization/RoleImpactQueryMapper.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	@DisplayName("影响用户展开：覆盖 USER/DEPT 闭包/POST，且 count 与 page 过滤同源")
	void impactUserExpansionMatchesAuthSemantics() throws IOException {
		String xml = readClasspathText();

		assertThat(xml).contains("impactUserIdsByRoleId")
			.contains("subject_type = 'USER'")
			.contains("dept_closure")
			.contains("v_user_dept_effective")
			.contains("v_user_post_effective")
			.contains("da.status = 1")
			.doesNotContain("ud.status")
			.doesNotContain("up.status")
			.contains("query.keyword")
			.contains("u.status = #{query.status}")
			.contains("countImpactUsersByRoleId")
			.contains("selectImpactUsersByRoleIdPage")
			.contains("selectImpactUserIdsByRoleId");
	}

}
