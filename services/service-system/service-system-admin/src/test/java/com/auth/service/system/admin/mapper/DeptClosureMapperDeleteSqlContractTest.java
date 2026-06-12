package com.auth.service.system.admin.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DeptClosureMapper 删除 SQL 契约测试
 *
 * @author Bunny
 */
@DisplayName("DeptClosureMapper callDelete SQL 契约")
class DeptClosureMapperDeleteSqlContractTest {

	private static String readMapperXml() throws IOException {
		try (InputStream in = DeptClosureMapperDeleteSqlContractTest.class
			.getResourceAsStream("/mapper/DeptClosureMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/DeptClosureMapper.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	@DisplayName("删除子树闭包：使用派生表删除并避免存储过程调用")
	void callDeleteUsesDerivedTableDelete() throws IOException {
		// 读取 Mapper，截取 callDelete 片段做关键 SQL 契约断言。
		String xml = readMapperXml();
		int updateStart = xml.indexOf("<update id=\"callDelete\"");
		assertThat(updateStart).isNotNegative();
		int updateEnd = xml.indexOf("</update>", updateStart);
		assertThat(updateEnd).isGreaterThan(updateStart);
		String updateBlock = xml.substring(updateStart, updateEnd);

		assertThat(updateBlock).contains("DELETE dc")
			.contains("FROM dept_closure dc")
			.contains("INNER JOIN")
			.contains("subtree_seed")
			.contains("ancestor_id = #{deptId, jdbcType=BIGINT}")
			.doesNotContain("CALL sp_dept_delete");
	}

}
