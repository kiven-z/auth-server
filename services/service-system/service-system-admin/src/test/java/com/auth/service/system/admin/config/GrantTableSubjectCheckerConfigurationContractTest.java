package com.auth.service.system.admin.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 源码契约：grant_table 主体校验 USER 存在性、DEPT/POST 计算有效
 *
 * @author Bunny
 */
@DisplayName("GrantTableSubjectCheckerConfiguration 装配契约")
class GrantTableSubjectCheckerConfigurationContractTest {

	@Test
	@DisplayName("USER 用 getExistingActive；DEPT/POST 用 requireEffective")
	void subjectCheckersWireExistenceVersusEffective() throws IOException {
		String source = locateConfigSource();

		assertThat(source).contains("GrantTableSubjectType.USER, user::getExistingActive")
			.contains("GrantTableSubjectType.DEPT, dept::requireEffective")
			.contains("GrantTableSubjectType.POST, post::requireEffective")
			.doesNotContain("GrantTableSubjectType.DEPT, dept::getExistingActive")
			.doesNotContain("GrantTableSubjectType.POST, post::getExistingActive");
	}

	private static String locateConfigSource() throws IOException {
		Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
		String relative = "src/main/java/com/auth/service/system/admin/config/GrantTableSubjectCheckerConfiguration.java";
		while (dir != null) {
			Path candidate = dir.resolve(relative);
			if (Files.isRegularFile(candidate)) {
				return Files.readString(candidate);
			}
			Path moduleCandidate = dir.resolve("services/service-system/service-system-admin").resolve(relative);
			if (Files.isRegularFile(moduleCandidate)) {
				return Files.readString(moduleCandidate);
			}
			dir = dir.getParent();
		}
		throw new IllegalStateException(
				"missing GrantTableSubjectCheckerConfiguration.java from " + System.getProperty("user.dir"));
	}

}
