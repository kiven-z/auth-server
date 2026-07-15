package com.auth.service.system.message.model.constants;

import com.auth.service.system.message.model.enums.InAppContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link InAppContentType} 解析单元测试
 */
@DisplayName("InAppContentType 站内信正文类型")
class InAppContentTypeTest {

	@Test
	@DisplayName("空与未知值按 TEXT；大小写不敏感；HTML 不在枚举内也按 TEXT")
	void from_shouldDefaultTextAndParseKnown() {
		// 空 / 未知 / 已移除的 HTML 一律 TEXT
		assertThat(InAppContentType.from(null)).isEqualTo(InAppContentType.TEXT);
		assertThat(InAppContentType.from("")).isEqualTo(InAppContentType.TEXT);
		assertThat(InAppContentType.from("unknown")).isEqualTo(InAppContentType.TEXT);
		assertThat(InAppContentType.from("HTML")).isEqualTo(InAppContentType.TEXT);
		assertThat(InAppContentType.from("markdown")).isEqualTo(InAppContentType.MARKDOWN);
		assertThat(InAppContentType.from("TEXT")).isEqualTo(InAppContentType.TEXT);
	}

}
