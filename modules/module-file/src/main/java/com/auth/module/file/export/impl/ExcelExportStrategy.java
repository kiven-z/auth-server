package com.auth.module.file.export.impl;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.auth.module.file.export.ExportStrategy;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 导出策略
 *
 * @author Bunny
 */
public class ExcelExportStrategy<T> implements ExportStrategy<List<T>, ResponseEntity<byte[]>> {

	private final Class<T> clazz;

	private final String sheetName;

	private final List<CellWriteHandler> writeHandlers;

	public ExcelExportStrategy(Class<T> clazz, String sheetName) {
		this.clazz = clazz;
		this.sheetName = sheetName;
		this.writeHandlers = new ArrayList<>();
	}

	public ExcelExportStrategy<T> registerWriteHandler(CellWriteHandler writeHandler) {
		this.writeHandlers.add(writeHandler);
		return this;
	}

	@Override
	public ResponseEntity<byte[]> export(List<T> data, OutputStream outputStream, String filename) throws IOException {
		if (!(outputStream instanceof ByteArrayOutputStream byteArrayOutputStream)) {
			throw new IllegalArgumentException("Excel 导出需要使用ByteArrayOutputStream");
		}

		try {
			ExcelWriterBuilder excelWriter = EasyExcelFactory.write(byteArrayOutputStream, clazz)
				.registerWriteHandler(new LongestMatchColumnWidthStyleStrategy());

			// 注册所有自定义处理器
			for (CellWriteHandler handler : writeHandlers) {
				excelWriter.registerWriteHandler(handler);
			}

			excelWriter.sheet(sheetName).doWrite(data);

			byte[] bytes = byteArrayOutputStream.toByteArray();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDisposition(ContentDisposition.attachment()
				.filename(URLEncoder.encode(filename, StandardCharsets.UTF_8))
				.build());

			return ResponseEntity.ok().headers(headers).body(bytes);
		}
		catch (Exception e) {
			throw new IOException("Excel export failed", e);
		}
	}

}