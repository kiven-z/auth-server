package com.auth.service.system.admin.mapper.admin.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：岗位列表带计算有效；countEffective 打 v_post_effective
 *
 * @author Bunny
 */
@DisplayName("SysPostMapper 计算有效 SQL 契约")
class SysPostMapperEffectiveSqlContractTest {

	private static String readClasspathXml() throws IOException {
		try (InputStream in = SysPostMapperEffectiveSqlContractTest.class
			.getResourceAsStream("/mapper/admin/post/SysPostMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/admin/post/SysPostMapper.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static String locateMapperJava() throws IOException {
		Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
		String relative = "src/main/java/com/auth/service/system/admin/mapper/admin/post/SysPostMapper.java";
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
		throw new IllegalStateException("missing SysPostMapper.java from " + System.getProperty("user.dir"));
	}

	private static String selectBlock(String xml, String id) {
		int start = xml.indexOf("<select id=\"" + id + "\"");
		assertThat(start).as(id).isNotNegative();
		int end = xml.indexOf("</select>", start);
		return xml.substring(start, end);
	}

	@Test
	@DisplayName("分页列表：LEFT JOIN v_post_effective 投影 effective")
	void pageQueryProjectsEffectiveFromView() throws IOException {
		String xml = readClasspathXml();

		assertThat(selectBlock(xml, "selectListByPage")).contains("v_post_effective vpe")
			.contains("AS effective")
			.contains("sys_post p");
	}

	@Test
	@DisplayName("countEffectiveById：注解 SQL 打 v_post_effective")
	void countEffectiveByIdUsesPostEffectiveView() throws IOException {
		String javaSource = locateMapperJava();
		assertThat(javaSource).contains("FROM v_post_effective WHERE id = #{postId}");
	}

}
