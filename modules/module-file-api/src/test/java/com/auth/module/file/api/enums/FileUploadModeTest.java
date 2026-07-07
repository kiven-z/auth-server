package com.auth.module.file.api.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FileUploadMode} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileUploadMode 上传模式")
class FileUploadModeTest {

	@Test
	@DisplayName("SIMPLE：暴露稳定模式编码")
	void simpleExposesStableCode() {
		// 验证简单直传模式编码与落库约定一致
		assertThat(FileUploadMode.SIMPLE.getCode()).isEqualTo("SIMPLE");
	}

}
