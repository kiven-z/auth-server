package com.auth.service.system.admin.mapper.post;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：岗位可挂载部门须校验闭包路径上祖先启用
 */
@DisplayName("SysPostMapper selectAssignableDeptIds SQL 契约")
class SysPostMapperAssignableDeptSqlContractTest {

	private static String readClasspathText() throws IOException {
		try (InputStream in = SysPostMapperAssignableDeptSqlContractTest.class
			.getResourceAsStream("/mapper/post/SysPostMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/post/SysPostMapper.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	@DisplayName("可挂载部门：本节点启用且排除路径上停用祖先")
	void selectAssignableDeptIdsChecksSelfAndAncestors() throws IOException {
		String xml = readClasspathText();

		int selectStart = xml.indexOf("<select id=\"selectAssignableDeptIds\"");
		assertThat(selectStart).isNotNegative();
		int selectEnd = xml.indexOf("</select>", selectStart);
		String selectBlock = xml.substring(selectStart, selectEnd);

		assertThat(selectBlock).contains("selectAssignableDeptIds")
			.contains("d.id")
			.contains("dept_closure")
			.contains("dc.depth")
			.contains("dc.descendant_id = d.id")
			.contains("d.status = 1")
			.contains("NOT EXISTS")
			.contains("foreach");
	}

}
