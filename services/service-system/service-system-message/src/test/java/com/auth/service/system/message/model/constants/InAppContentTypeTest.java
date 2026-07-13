package com.auth.service.system.message.model.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link InAppContentType} 解析单元测试
 */
@DisplayName("InAppContentType 站内信正文类型")
class InAppContentTypeTest {

	@Test
	@DisplayName("空与未知值按 TEXT；大小写不敏感")
	void from_shouldDefaultTextAndParseKnown() {
		// 默认与枚举解析
		assertThat(InAppContentType.from(null)).isEqualTo(InAppContentType.TEXT);
		assertThat(InAppContentType.from("")).isEqualTo(InAppContentType.TEXT);
		assertThat(InAppContentType.from("unknown")).isEqualTo(InAppContentType.TEXT);
		assertThat(InAppContentType.from("markdown")).isEqualTo(InAppContentType.MARKDOWN);
		assertThat(InAppContentType.from("HTML")).isEqualTo(InAppContentType.HTML);
	}

}
