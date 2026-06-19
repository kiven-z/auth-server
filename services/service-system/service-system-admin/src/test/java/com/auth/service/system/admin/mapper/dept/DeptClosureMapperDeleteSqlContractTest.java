package com.auth.service.system.admin.mapper.dept;

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
			.getResourceAsStream("/mapper/dept/DeptClosureMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/dept/DeptClosureMapper.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	@DisplayName("删除子树闭包：调用删除存储过程")
	void callDeleteUsesDerivedTableDelete() throws IOException {
		// 读取 Mapper，截取 callDelete 片段做关键 SQL 契约断言。
		String xml = readMapperXml();
		int updateStart = xml.indexOf("<update id=\"callDelete\"");
		assertThat(updateStart).isNotNegative();
		int updateEnd = xml.indexOf("</update>", updateStart);
		assertThat(updateEnd).isGreaterThan(updateStart);
		String updateBlock = xml.substring(updateStart, updateEnd);

		assertThat(updateBlock).contains("statementType=\"CALLABLE\"")
			.contains("CALL sp_dept_delete")
			.contains("#{deptId, mode=IN, jdbcType=BIGINT}");
	}

}
