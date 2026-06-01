package com.auth.service.auth.authorization.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * AuthorizationImpactMapper.xml SQL 语义回归测试。
 *
 * @author Bunny
 */
@DisplayName("AuthorizationImpactMapper SQL 回归")
class AuthorizationImpactMapperSqlRegressionTest {

	@Test
	@DisplayName("权限码桥接不应依赖 sys_permission.status")
	void permissionBridge_shouldNotDependOnSysPermissionStatus() throws IOException {
		// 防回归：禁用/改码时 impacted 不能因为 join/status 过滤而变为 0
		InputStream in = AuthorizationImpactMapperSqlRegressionTest.class.getClassLoader()
			.getResourceAsStream("mapper/AuthorizationImpactMapper.xml");
		assertNotNull(in, "AuthorizationImpactMapper.xml must exist in test classpath");
		byte[] bytes = in.readAllBytes();
		String xml = new String(bytes, StandardCharsets.UTF_8);

		assertNotNull(xml);
		assertFalse(xml.contains("INNER JOIN sys_permission"),
				"Permission bridge should not INNER JOIN sys_permission (breaks when code renamed)");
		assertFalse(xml.contains("AND p.status = 1"),
				"Permission bridge should not filter by sys_permission.status (breaks on disable)");
	}
}

