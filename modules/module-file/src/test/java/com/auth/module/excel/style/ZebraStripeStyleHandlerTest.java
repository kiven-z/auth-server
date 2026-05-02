package com.auth.module.excel.style;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.auth.module.excel.model.TestData;
import com.auth.module.file.model.ZebraStripeStyleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 斑马纹样式单元测试
 */
class ZebraStripeStyleHandlerTest {

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
	 * 测试Excel样式
	 */
	private static void writeFile(String blueGreyTechZebraStripeStyleHandler, ZebraStripeStyleType blueGreyTech) {
		EasyExcelFactory.write(blueGreyTechZebraStripeStyleHandler, TestData.class)
			.registerWriteHandler(ZebraStripeStyleType.getHandler(blueGreyTech))
			.registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
			.sheet("Sheet")
			.doWrite(createTestData());

		// 判断文件是否存在
		Assertions.assertTrue(Files.exists(Path.of(blueGreyTechZebraStripeStyleHandler)));
	}

	@Test
	void test() throws IOException {
		// 先创建文件夹
		Files.createDirectories(Path.of("/home/bunny/develop/OpenSource/Auth/Test/"));

		// 测试写入文件
		writeFile("/home/bunny/develop/OpenSource/Auth/Test/简洁蓝灰风格.xlsx", ZebraStripeStyleType.BLUE_GREY_TECH);

		writeFile("/home/bunny/develop/OpenSource/Auth/Test/柔和绿色风格.xlsx", ZebraStripeStyleType.GREEN_SOFT);

		writeFile("/home/bunny/develop/OpenSource/Auth/Test/经典灰色商务风格.xlsx", ZebraStripeStyleType.GREY_BUSINESS);

		writeFile("/home/bunny/develop/OpenSource/Auth/Test/温暖橙色风格.xlsx", ZebraStripeStyleType.ORANGE_WARM);

		writeFile("/home/bunny/develop/OpenSource/Auth/Test/优雅紫色风格.xlsx", ZebraStripeStyleType.PURPLE_ELEGANT);
	}

}