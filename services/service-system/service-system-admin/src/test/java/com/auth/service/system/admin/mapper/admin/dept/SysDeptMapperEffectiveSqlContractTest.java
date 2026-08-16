package com.auth.service.system.admin.mapper.admin.dept;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：部门列表带计算有效；countEffective 打 v_dept_effective
 *
 * @author Bunny
 */
@DisplayName("SysDeptMapper 计算有效 SQL 契约")
class SysDeptMapperEffectiveSqlContractTest {

	private static String readClasspathXml() throws IOException {
		try (InputStream in = SysDeptMapperEffectiveSqlContractTest.class
			.getResourceAsStream("/mapper/admin/dept/SysDeptMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/admin/dept/SysDeptMapper.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static String locateMapperJava() throws IOException {
		Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
		String relative = "src/main/java/com/auth/service/system/admin/mapper/admin/dept/SysDeptMapper.java";
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
		throw new IllegalStateException("missing SysDeptMapper.java from " + System.getProperty("user.dir"));
	}

	private static String selectBlock(String xml, String id) {
		int start = xml.indexOf("<select id=\"" + id + "\"");
		assertThat(start).as(id).isNotNegative();
		int end = xml.indexOf("</select>", start);
		return xml.substring(start, end);
	}

	@Test
	@DisplayName("扁平/分页列表：LEFT JOIN v_dept_effective 投影 effective")
	void listQueriesProjectEffectiveFromView() throws IOException {
		String xml = readClasspathXml();

		assertThat(selectBlock(xml, "selectListByQuery")).contains("v_dept_effective ve")
			.contains("AS effective")
			.contains("sys_dept d");
		assertThat(selectBlock(xml, "selectListByPage")).contains("v_dept_effective ve")
			.contains("AS effective")
			.contains("sys_dept d");
	}

	@Test
	@DisplayName("countEffectiveById：注解 SQL 打 v_dept_effective")
	void countEffectiveByIdUsesDeptEffectiveView() throws IOException {
		String javaSource = locateMapperJava();
		assertThat(javaSource).contains("FROM v_dept_effective WHERE id = #{deptId}");
	}

}
