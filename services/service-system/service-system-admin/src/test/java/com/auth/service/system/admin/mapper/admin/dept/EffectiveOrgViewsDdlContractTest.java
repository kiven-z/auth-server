package com.auth.service.system.admin.mapper.admin.dept;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：组织有效视图含祖先，任职依赖 L1，授权主体含闭包
 */
@DisplayName("effective-org-views DDL 契约")
class EffectiveOrgViewsDdlContractTest {

	private static final String SQL_RELATIVE = "db/effective-org-views.sql";

	@Test
	@DisplayName("五张视图：L1 含祖先、任职依赖 L1、授权主体含闭包")
	void effectiveViewsEncodeAncestorAwareOrgSemantics() throws IOException {
		String sql = Files.readString(locateSql(), StandardCharsets.UTF_8);

		assertThat(sql).contains("CREATE VIEW v_dept_effective")
			.contains("CREATE VIEW v_post_effective")
			.contains("CREATE VIEW v_user_dept_effective")
			.contains("CREATE VIEW v_user_post_effective")
			.contains("CREATE VIEW v_user_grant_subjects");

		String deptEffective = viewBlock(sql, "v_dept_effective");
		assertThat(deptEffective).contains("dept_closure")
			.contains("ancestor_id")
			.contains("depth > 0")
			.contains("d.status = 1")
			.doesNotContain("user_dept");

		String postEffective = viewBlock(sql, "v_post_effective");
		assertThat(postEffective).contains("v_dept_effective")
			.contains("p.status = 1")
			.doesNotContain("JOIN sys_dept")
			.doesNotContain("user_post");

		String userDept = viewBlock(sql, "v_user_dept_effective");
		assertThat(userDept).contains("v_dept_effective").doesNotContain("sys_dept").doesNotContain("ud.status");

		String userPost = viewBlock(sql, "v_user_post_effective");
		assertThat(userPost).contains("v_post_effective").doesNotContain("sys_post").doesNotContain("up.status");

		String grantSubjects = viewBlock(sql, "v_user_grant_subjects");
		assertThat(grantSubjects).contains("sys_user")
			.contains("'USER'")
			.contains("'DEPT'")
			.contains("'POST'")
			.contains("v_user_dept_effective")
			.contains("dept_closure")
			.contains("ancestor_id")
			.contains("v_dept_effective")
			.contains("v_user_post_effective")
			.doesNotContain("grant_table")
			.doesNotContain("ud.dept_id = gt")
			.doesNotContain("gt.subject_id = ud.dept_id");
	}

	private static Path locateSql() {
		Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
		while (dir != null) {
			Path candidate = dir.resolve(SQL_RELATIVE);
			if (Files.isRegularFile(candidate)) {
				return candidate;
			}
			dir = dir.getParent();
		}
		throw new IllegalStateException("missing " + SQL_RELATIVE + " from " + System.getProperty("user.dir"));
	}

	private static String viewBlock(String sql, String viewName) {
		String marker = "CREATE VIEW " + viewName;
		int start = sql.indexOf(marker);
		assertThat(start).as(viewName).isNotNegative();
		int nextCreate = sql.indexOf("CREATE VIEW ", start + marker.length());
		int end = nextCreate < 0 ? sql.length() : nextCreate;
		return sql.substring(start, end);
	}

}
