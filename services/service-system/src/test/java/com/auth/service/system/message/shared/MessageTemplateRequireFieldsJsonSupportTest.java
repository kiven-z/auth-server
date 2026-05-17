package com.auth.service.system.message.shared;

import com.auth.service.system.admin.model.vo.emailtemplate.EmailTemplateRequireFieldRow;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.auth.service.system.common.exception.code.message.SystemEmailResultCode.EMAIL_TEMPLATE_VARS_MISSING;
import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.SMS_TEMPLATE_VARS_MISSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link MessageTemplateRequireFieldsJsonSupport} 单元测试
 */
@DisplayName("MessageTemplateRequireFieldsJsonSupport 模板变量 JSON 支持")
class MessageTemplateRequireFieldsJsonSupportTest {

	@Test
	@DisplayName("findMissingKeys 在无声明时返回空列表")
	void findMissingKeys_returnsEmptyWhenNoRequireFields() {
		assertThat(MessageTemplateRequireFieldsJsonSupport.findMissingKeys(List.of(), Map.of("code", "1"))).isEmpty();
	}

	@Test
	@DisplayName("findMissingKeys 在变量齐全时返回空列表")
	void findMissingKeys_returnsEmptyWhenAllPresent() {
		List<EmailTemplateRequireFieldRow> rows = MessageTemplateRequireFieldsJsonSupport
			.parse("[{\"key\":\"code\",\"exampleValue\":\"123456\"},{\"key\":\"min\",\"exampleValue\":5}]");
		assertThat(MessageTemplateRequireFieldsJsonSupport.findMissingKeys(rows, Map.of("code", "1", "min", 5)))
			.isEmpty();
	}

	@Test
	@DisplayName("findMissingKeys 返回缺失的 key")
	void findMissingKeys_returnsMissingKeys() {
		List<EmailTemplateRequireFieldRow> rows = MessageTemplateRequireFieldsJsonSupport
			.parse("[{\"key\":\"code\",\"exampleValue\":\"123456\"}]");
		assertThat(MessageTemplateRequireFieldsJsonSupport.findMissingKeys(rows, Map.of())).containsExactly("code");
	}

	@Test
	@DisplayName("assertVariablesPresent 缺失变量时按渠道业务码抛错")
	void assertVariablesPresent_throwsWhenMissing() {
		String json = "[{\"key\":\"code\",\"exampleValue\":\"123456\"}]";
		assertThatThrownBy(() -> MessageTemplateRequireFieldsJsonSupport.assertVariablesPresent(json, Map.of(),
				SMS_TEMPLATE_VARS_MISSING))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SMS_TEMPLATE_VARS_MISSING);

		assertThatThrownBy(() -> MessageTemplateRequireFieldsJsonSupport.assertVariablesPresent(json, Map.of(),
				EMAIL_TEMPLATE_VARS_MISSING))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(EMAIL_TEMPLATE_VARS_MISSING);
	}

	@Test
	@DisplayName("toExampleModel 使用 exampleValue 构建模型")
	void toExampleModel_buildsFromExampleValues() {
		EmailTemplateRequireFieldRow row = new EmailTemplateRequireFieldRow();
		row.setKey("code");
		row.setExampleValue(new TextNode("123456"));
		Map<String, Object> model = MessageTemplateRequireFieldsJsonSupport.toExampleModel(List.of(row));
		assertThat(model).containsEntry("code", "123456");
	}

}
