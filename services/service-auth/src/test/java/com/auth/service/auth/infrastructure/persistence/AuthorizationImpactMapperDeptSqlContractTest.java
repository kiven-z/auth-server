package com.auth.service.auth.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：部门失效反查须覆盖禁用节点（仅 is_deleted），与正向画像重算规则分离。
 */
@DisplayName("AuthorizationImpactMapper 部门反查 SQL 契约")
class AuthorizationImpactMapperDeptSqlContractTest {

	private static String readClasspathText() throws IOException {
		try (InputStream in = AuthorizationImpactMapperDeptSqlContractTest.class
			.getResourceAsStream("/mapper/AuthorizationImpactMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/AuthorizationImpactMapper.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	@DisplayName("按部门 ID 反查成员：不按 sys_dept.status 过滤，以便禁用/移动仍能刷新画像")
	void selectUserIdsByDeptIdsDoesNotFilterByDeptStatus() throws IOException {
		String xml = readClasspathText();

		int selectStart = xml.indexOf("<select id=\"selectUserIdsByDeptIds\"");
		assertThat(selectStart).isNotNegative();
		int selectEnd = xml.indexOf("</select>", selectStart);
		String selectBlock = xml.substring(selectStart, selectEnd);

		assertThat(selectBlock).contains("selectUserIdsByDeptIds")
			.contains("is_deleted = 0")
			.doesNotContain("d.status =")
			.contains("ud.status IN (1, 2)");
	}

}
