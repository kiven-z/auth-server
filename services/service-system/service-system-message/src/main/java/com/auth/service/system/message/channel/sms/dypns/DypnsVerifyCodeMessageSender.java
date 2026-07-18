package com.auth.service.system.message.channel.sms.dypns;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.channel.MessageSender;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.message.channel.sms.dypns.transport.AliyunDypnsTransport;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.model.value.sms.dypns.DypnsVerifyCodeSendPayload;
import org.springframework.stereotype.Component;

import static com.auth.service.system.message.exception.MessageResultCode.SMS_CUSTOM_BODY_NOT_SUPPORTED;

/**
 * 阿里云 Dypns 验证码短信发送实现
 *
 * @author Bunny
 */
@Component
public class DypnsVerifyCodeMessageSender implements MessageSender {

	private final DypnsVerifyCodeTemplatePipeline dypnsVerifyCodeTemplatePipeline;

	private final AliyunDypnsTransport aliyunDypnsTransport;

	public DypnsVerifyCodeMessageSender(DypnsVerifyCodeTemplatePipeline dypnsVerifyCodeTemplatePipeline,
			AliyunDypnsTransport aliyunDypnsTransport) {
		this.dypnsVerifyCodeTemplatePipeline = dypnsVerifyCodeTemplatePipeline;
		this.aliyunDypnsTransport = aliyunDypnsTransport;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageChannel channel() {
		return MessageChannel.SMS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendByTemplate(TemplateMessageCommand command) {
		if (command.getCustomBody() != null && command.getCustomBody()) {
			throw new MessageException(SMS_CUSTOM_BODY_NOT_SUPPORTED);
		}

		DypnsVerifyCodeSendPayload prepared = dypnsVerifyCodeTemplatePipeline.prepare(command);
		for (String phone : command.getTargets()) {
			aliyunDypnsTransport.sendVerifyCode(phone, prepared.providerTemplateCode(), prepared.templateParam());
		}
	}

}
