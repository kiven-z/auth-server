package com.auth.service.system.file.utils;

import com.auth.service.system.file.exception.FileException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link FileConfigAssertions} 单元测试
 *
 * @author Bunny
 */
@DisplayName("FileConfigAssertions 配置断言")
class FileConfigAssertionsTest {

	@Test
	@DisplayName("assertNotBlank：非空值不抛异常")
	void assertNotBlank_passesWhenValuePresent() {
		assertThatCode(() -> FileConfigAssertions.assertNotBlank("value", "minio.endpoint")).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("assertNotBlank：空值抛出 FileException")
	void assertNotBlank_throwsWhenBlank() {
		assertThatThrownBy(() -> FileConfigAssertions.assertNotBlank(" ", "minio.endpoint"))
			.isInstanceOf(FileException.class)
			.extracting(ex -> ((FileException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_STORAGE_CONFIG_MISSING);
	}

}
