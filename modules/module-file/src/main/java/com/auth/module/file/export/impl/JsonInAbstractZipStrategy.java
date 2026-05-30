package com.auth.module.file.export.impl;

import com.alibaba.fastjson2.JSON;
import com.auth.module.file.export.AbstractZipExportStrategy;

import java.nio.charset.StandardCharsets;

/**
 * 将JSON文件打包到Zip中的策略
 *
 * @author Bunny
 */
public class JsonInAbstractZipStrategy extends AbstractZipExportStrategy<Object> {

	@Override
	protected String getFilename(Object data, String baseFilename) {
		return baseFilename.endsWith(".json") ? baseFilename : baseFilename + ".json";
	}

	@Override
	protected byte[] getFileContent(Object data) {
		return JSON.toJSONString(data).getBytes(StandardCharsets.UTF_8);
	}

}