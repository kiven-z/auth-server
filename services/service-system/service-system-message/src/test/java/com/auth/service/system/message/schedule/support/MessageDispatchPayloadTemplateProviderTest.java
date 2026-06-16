package com.auth.service.system.message.schedule.support;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.message.model.MessageDispatchPayload;
import com.auth.service.system.message.support.MessageDispatchPayloadTemplateProvider;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MessageDispatchPayloadTemplateProvider} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("MessageDispatchPayloadTemplateProvider 消息派发载荷模板")
class MessageDispatchPayloadTemplateProviderTest {

	private final MessageDispatchPayloadTemplateProvider provider = new MessageDispatchPayloadTemplateProvider();

	@Test
	@DisplayName("taskType：返回 MESSAGE_DISPATCH")
	void taskTypeReturnsMessageDispatch() {
		assertThat(provider.taskType()).isEqualTo(SysJobTaskType.MESSAGE_DISPATCH);
	}

	@Test
	@DisplayName("buildTemplatePayload：返回带示例字段的标准载荷模板")
	void buildTemplatePayloadReturnsExampleEnvelope() {
		MessageDispatchPayload payload = provider.buildTemplatePayload();

		assertThat(payload.getVersion()).isEqualTo(MessageDispatchPayload.VERSION_1);
		assertThat(payload.getCommand()).isNotNull();
		assertThat(payload.getCommand().getChannel()).isEqualTo(MessageChannel.EMAIL);
		assertThat(payload.getCommand().getTemplateCode()).isEqualTo("LOGIN_NOTICE");
		assertThat(payload.getCommand().getTargets()).isEqualTo(List.of("dev@example.com"));
	}

}
