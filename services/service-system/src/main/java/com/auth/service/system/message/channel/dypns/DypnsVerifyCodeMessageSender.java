package com.auth.service.system.message.channel.dypns;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.sender.modules.sms.dypns.DypnsVerifyCodeSendPayload;
import com.auth.module.message.sender.modules.sms.dypns.DypnsVerifyCodeSender;
import com.auth.module.message.sender.spi.MessageSender;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.SMS_CUSTOM_BODY_NOT_SUPPORTED;
import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.SMS_SENDER_NOT_ENABLED;

/**
 * 阿里云 Dypns 验证码短信渠道发送实现（{@link MessageChannel#SMS_ALIYUN_DYPNS}）
 *
 * @author Bunny
 */
@ConditionalOnProperty(prefix = "auth.message.sms", name = "enabled", havingValue = "true")
@Component
public class DypnsVerifyCodeMessageSender implements MessageSender {

	private final DypnsVerifyCodeTemplatePipeline dypnsVerifyCodeTemplatePipeline;

	private final ObjectProvider<DypnsVerifyCodeSender> dypnsVerifyCodeSender;

	public DypnsVerifyCodeMessageSender(DypnsVerifyCodeTemplatePipeline dypnsVerifyCodeTemplatePipeline,
			ObjectProvider<DypnsVerifyCodeSender> dypnsVerifyCodeSender) {
		this.dypnsVerifyCodeTemplatePipeline = dypnsVerifyCodeTemplatePipeline;
		this.dypnsVerifyCodeSender = dypnsVerifyCodeSender;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageChannel channel() {
		return MessageChannel.SMS_ALIYUN_DYPNS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendByTemplate(TemplateMessageCommand command) {
		if (Boolean.TRUE.equals(command.getCustomBody())) {
			throw new SystemBusinessException(SMS_CUSTOM_BODY_NOT_SUPPORTED);
		}

		DypnsVerifyCodeSendPayload prepared = dypnsVerifyCodeTemplatePipeline.prepare(command);
		DypnsVerifyCodeSender sender = dypnsVerifyCodeSender.getIfAvailable();
		if (sender == null) {
			throw new SystemBusinessException(SMS_SENDER_NOT_ENABLED);
		}

		for (String phone : command.getTargets()) {
			sender.sendVerifyCode(phone, prepared.providerTemplateCode(), prepared.templateParam());
		}
	}

}
