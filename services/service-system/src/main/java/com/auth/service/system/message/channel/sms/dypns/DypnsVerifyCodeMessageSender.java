package com.auth.service.system.message.channel.sms.dypns;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.MessageSender;
import org.springframework.stereotype.Component;

import static com.auth.service.system.common.exception.code.SystemMessageResultCode.SMS_CUSTOM_BODY_NOT_SUPPORTED;

/**
 * 阿里云 Dypns 验证码短信渠道发送实现（{@link MessageChannel#SMS_ALIYUN_DYPNS}）
 *
 * @author Bunny
 */
@Component
public class DypnsVerifyCodeMessageSender implements MessageSender {

	private final DypnsVerifyCodeTemplatePipeline dypnsVerifyCodeTemplatePipeline;

	private final DypnsVerifyCodeSender dypnsVerifyCodeSender;

	public DypnsVerifyCodeMessageSender(DypnsVerifyCodeTemplatePipeline dypnsVerifyCodeTemplatePipeline,
			DypnsVerifyCodeSender dypnsVerifyCodeSender) {
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
		for (String phone : command.getTargets()) {
			dypnsVerifyCodeSender.sendVerifyCode(phone, prepared.providerTemplateCode(), prepared.templateParam());
		}
	}

}
