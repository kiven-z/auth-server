package com.auth.service.system.message.convert;

import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.command.ChannelOptionsSupport;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.modules.email.model.EmailChannelOptions;
import com.auth.module.message.modules.email.model.EmailSendDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TemplateMessageCommandConverter} 单元测试
 */
@DisplayName("TemplateMessageCommandConverter 命令转换")
class TemplateMessageCommandConverterTest {

	@Test
	@DisplayName("EmailSendDTO 与 TemplateMessageCommand 双向转换字段一致")
	void roundTrip_preservesEmailFields() {
		EmailSendDTO dto = EmailSendDTO.builder()
			.templateCode("T1")
			.to(List.of("u@example.com"))
			.cc(List.of("cc@example.com"))
			.bcc(List.of("bcc@example.com"))
			.replyTo("reply@example.com")
			.variables(Map.of("k", "v"))
			.hasHtml(Boolean.TRUE)
			.build();

		TemplateMessageCommand command = TemplateMessageCommandConverter.fromEmailDto(dto);
		assertThat(command.getChannel()).isEqualTo(MessageChannel.EMAIL);
		assertThat(command.getTemplateCode()).isEqualTo("T1");
		assertThat(command.getTargets()).containsExactly("u@example.com");
		assertThat(command.getVariables()).containsEntry("k", "v");
		EmailChannelOptions options = ChannelOptionsSupport.asEmail(command.getOptions());
		assertThat(options.getCc()).containsExactly("cc@example.com");
		assertThat(options.getHasHtml()).isTrue();

		EmailSendDTO restored = TemplateMessageCommandConverter.toEmailDto(command);
		assertThat(restored.getTemplateCode()).isEqualTo(dto.getTemplateCode());
		assertThat(restored.getTo()).isEqualTo(dto.getTo());
		assertThat(restored.getCc()).isEqualTo(dto.getCc());
		assertThat(restored.getHasHtml()).isEqualTo(dto.getHasHtml());
	}

}
