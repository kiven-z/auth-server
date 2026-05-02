package com.auth.module.excel.export.impl;

import com.auth.module.excel.model.TestData;
import com.auth.module.file.export.ExportStrategy;
import com.auth.module.file.export.impl.ExcelInAbstractZipStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

class ExcelInZipStrategyTest {

	private static final String TEST_FILE_PATH_PREFIX = "/home/bunny/develop/OpenSource/Auth/Test/";

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

	@Test
	void testExport() throws IOException {
		// 先创建文件夹
		Files.createDirectories(Path.of(TEST_FILE_PATH_PREFIX));

		// 多个文件打包到Zip
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ZipOutputStream zos = new ZipOutputStream(baos)) {

			// 添加Excel文件
			ExportStrategy<List<TestData>, Void> excelInZip = new ExcelInAbstractZipStrategy<>(TestData.class, "用户列表");
			excelInZip.export(createTestData(), zos, TEST_FILE_PATH_PREFIX + "ExcelInZipStrategy.xlsx");

			// 关闭流并获取结果
			zos.finish();
			byte[] zipBytes = baos.toByteArray();
			Path path = Path.of(TEST_FILE_PATH_PREFIX + "ExcelInZipStrategy.zip");
			Files.write(path, zipBytes);

			Assertions.assertTrue(Files.exists(path));
		}
	}

}