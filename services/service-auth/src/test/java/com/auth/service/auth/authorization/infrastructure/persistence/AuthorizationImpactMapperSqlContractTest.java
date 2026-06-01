package com.auth.service.auth.authorization.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * SQL 契约测试：防止授权失效「影响面反查」出现不符合预期的过滤条件。
 *
 * <p>
 * 背景：权限停用/改名属于授权语义变更，必须能反查到受影响用户并触发版本递增与画像刷新；
 * 因此「权限码→角色码」桥接 SQL 不应依赖 {@code sys_permission.status=1}，否则停用时反查会命中 0 人。
 * </p>
 */
@DisplayName("AuthorizationImpactMapper SQL 契约")
class AuthorizationImpactMapperSqlContractTest {

	@Test
	@DisplayName("权限码桥接不应按权限启用状态过滤")
	void permissionCodeBridgeShouldNotFilterByPermissionStatus() throws IOException {
		String xml = readClasspathText("/mapper/AuthorizationImpactMapper.xml");

		assertThat(xml).contains("selectRoleCodesByPermissionCodes");

		// 反查桥接只需要 sys_role_permission；不应依赖 sys_permission.status=1
		assertThat(xml).doesNotContain("JOIN sys_permission");
		assertThat(xml).doesNotContain("INNER JOIN sys_permission");
		// 注意：XML 中存在 ud.status / up.status；这里只禁止 sys_permission 的别名 p.status
		assertThat(xml).doesNotContain(" p.status");
		assertThat(xml).doesNotContain(" p.status ");
		assertThat(xml).doesNotContain("p.status =");
	}

	private static String readClasspathText(String path) throws IOException {
		try (InputStream in = AuthorizationImpactMapperSqlContractTest.class.getResourceAsStream(path)) {
			assertThat(in).as("resource %s must exist", path).isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

}

