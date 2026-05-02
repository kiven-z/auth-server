package com.auth.module.file.export.impl;

import com.alibaba.excel.EasyExcelFactory;
import com.auth.module.file.export.AbstractZipExportStrategy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 将Excel文件打包到Zip中的策略
 *
 * @author Bunny
 */
public class ExcelInAbstractZipStrategy<T> extends AbstractZipExportStrategy<List<T>> {

	private final Class<T> clazz;

	private final String sheetName;

	public ExcelInAbstractZipStrategy(Class<T> clazz, String sheetName) {
		this.clazz = clazz;
		this.sheetName = sheetName;
	}

	@Override
	protected String getFilename(List<T> data, String baseFilename) {
		return baseFilename.endsWith(".xlsx") ? baseFilename : baseFilename + ".xlsx";
	}

	@Override
	protected byte[] getFileContent(List<T> data) throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			EasyExcelFactory.write(baos, clazz).sheet(sheetName).doWrite(data);
			return baos.toByteArray();
		}
	}

}