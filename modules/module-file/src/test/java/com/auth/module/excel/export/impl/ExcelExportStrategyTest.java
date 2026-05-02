package com.auth.module.excel.export.impl;

import com.auth.module.excel.model.TestData;
import com.auth.module.file.export.impl.ExcelExportStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class ExcelExportStrategyTest {

	/**
	 * 创建测试数据 - 30条数据
	 */
	private static List<TestData> createTestData() {
		List<TestData> dataList = new ArrayList<>();

		// 前15条来自Excel中的20电商31班
		dataList.add(new TestData("机电学院", "21机电01", "2101201001", "张三", "优秀"));
		dataList.add(new TestData("机电学院", "21机电01", "2101201002", "李四", "良好"));
		dataList.add(new TestData("机电学院", "21机电01", "2101201003", "王五", "及格"));
		dataList.add(new TestData("机电学院", "21机电01", "2101201004", "赵六", "不及格"));
		dataList.add(new TestData("机电学院", "21机电02", "2101202001", "钱七", "优秀"));

		return dataList;
	}

	/**
	 * 测试Excel导出功能 验证ExcelExportStrategy的export方法是否能正确生成Excel文件并返回响应
	 * @throws IOException 当文件操作出现IO异常时抛出
	 */
	@Test
	void testExport() throws IOException {
		// 创建Excel导出策略实例，指定数据类型为TestData，表名为"测试数据"
		ExcelExportStrategy<TestData> strategy = new ExcelExportStrategy<>(TestData.class, "测试数据");

		// 执行导出操作，将测试数据导出到字节数组输出流中
		ResponseEntity<byte[]> export = strategy.export(createTestData(), new ByteArrayOutputStream(), "测试数据.xlsx");
		byte[] body = export.getBody();
		// 验证导出的字节数组不为空
		Assertions.assertNotNull(body);

		// 将导出的字节数据写入到指定路径的文件中
		Path path = Path.of("/home/bunny/develop/OpenSource/Auth/Test/ExcelExportStrategy.xlsx");
		Files.write(path, body);

		// 验证文件是否成功创建
		Assertions.assertTrue(Files.exists(path));
	}

}