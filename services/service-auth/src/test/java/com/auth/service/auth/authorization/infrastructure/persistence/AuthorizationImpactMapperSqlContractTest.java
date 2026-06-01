package com.auth.service.auth.authorization.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约测试：防止授权失效「影响面反查」出现不符合预期的过滤条件。
 */
@DisplayName("AuthorizationImpactMapper SQL 契约")
class AuthorizationImpactMapperSqlContractTest {

    private static String readClasspathText() throws IOException {
        try (InputStream in = AuthorizationImpactMapperSqlContractTest.class.getResourceAsStream("/mapper/AuthorizationImpactMapper.xml")) {
            assertThat(in).as("resource %s must exist", "/mapper/AuthorizationImpactMapper.xml").isNotNull();
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    @DisplayName("权限码桥接不应按权限启用状态过滤")
    void permissionCodeBridgeShouldNotFilterByPermissionStatus() throws IOException {
        String xml = readClasspathText();

        assertThat(xml).contains("selectRoleCodesByPermissionCodes");

        // 反查桥接只需要 sys_role_permission；不应依赖 sys_permission.status=1
        assertThat(xml).doesNotContain("JOIN sys_permission");
        assertThat(xml).doesNotContain("INNER JOIN sys_permission");
        // 注意：XML 中存在 ud.status / up.status；这里只禁止 sys_permission 的别名 p.status
        assertThat(xml).doesNotContain(" p.status");
        assertThat(xml).doesNotContain(" p.status ");
        assertThat(xml).doesNotContain("p.status =");
    }

}
