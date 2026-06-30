package com.auth.service.system.admin.mapper.dept;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：子树引用校验须覆盖用户-部门、grant DEPT 与岗位
 */
@DisplayName("SysDeptMapper existsSubtreeReference SQL 契约")
class DeptReferenceCheckerMapperSqlContractTest {

	private static String readClasspathText() throws IOException {
		try (InputStream in = DeptReferenceCheckerMapperSqlContractTest.class
			.getResourceAsStream("/mapper/dept/SysDeptMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/dept/SysDeptMapper.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	@DisplayName("子树引用：一次查询覆盖 user_dept、grant_table、sys_post")
	void existsSubtreeReferenceCoversAllReferenceSources() throws IOException {
		String xml = readClasspathText();

		int selectStart = xml.indexOf("<select id=\"existsSubtreeReference\"");
		assertThat(selectStart).isNotNegative();
		int selectEnd = xml.indexOf("</select>", selectStart);
		String selectBlock = xml.substring(selectStart, selectEnd);

		assertThat(selectBlock).contains("existsSubtreeReference")
			.contains("dept_closure")
			.contains("user_dept")
			.contains("grant_table")
			.contains("sys_post")
			.contains("EXISTS");
	}

}
