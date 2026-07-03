package com.auth.service.system.file.utils;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.file.exception.FileException;
import lombok.experimental.UtilityClass;

import static com.auth.service.system.file.exception.FileUploadResultCode.FILE_STORAGE_CONFIG_MISSING;

/**
 * 文件模块配置断言工具
 *
 * @author Bunny
 */
@UtilityClass
public class FileConfigAssertions {

	/**
	 * 断言配置项非空
	 * @param value 配置值
	 * @param configKey 配置键
	 */
	public static void assertNotBlank(String value, String configKey) {
		if (CharSequenceUtil.isBlank(value)) {
			throw new FileException(FILE_STORAGE_CONFIG_MISSING, configKey);
		}
	}

}
