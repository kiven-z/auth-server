package com.auth.service.system.message.utils;

import com.auth.service.system.message.exception.MessageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link MessageConfigAssertions} 单元测试
 *
 * @author Bunny
 */
@DisplayName("MessageConfigAssertions 配置断言")
class MessageConfigAssertionsTest {

	@Test
	@DisplayName("assertNotBlank：非空值不抛异常")
	void assertNotBlank_passesWhenValuePresent() {
		assertThatCode(() -> MessageConfigAssertions.assertNotBlank("value", "短信区域")).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("assertNotBlank：空值抛出 MessageException")
	void assertNotBlank_throwsWhenBlank() {
		assertThatThrownBy(() -> MessageConfigAssertions.assertNotBlank("", "短信区域"))
			.isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(PARAM_REQUIRED);
	}

	@Test
	@DisplayName("assertNotNull：非 null 值不抛异常")
	void assertNotNull_passesWhenValuePresent() {
		assertThatCode(() -> MessageConfigAssertions.assertNotNull(1L, "钉钉 AgentId")).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("assertNotNull：null 值抛出 MessageException")
	void assertNotNull_throwsWhenNull() {
		assertThatThrownBy(() -> MessageConfigAssertions.assertNotNull(null, "钉钉 AgentId"))
			.isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(PARAM_REQUIRED);
	}

}
