package com.auth.service.system.message.channel.sms.dypns;

import cn.hutool.core.util.BooleanUtil;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.channel.MessageSender;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.springframework.stereotype.Component;

import static com.auth.service.system.message.exception.MessageResultCode.SMS_CUSTOM_BODY_NOT_SUPPORTED;

/**
 * 阿里云 Dypns 验证码短信渠道发送实现
 *
 * @author Bunny
 */
@Component
public class DypnsVerifyCodeMessageSender implements MessageSender {

	private final DypnsVerifyCodeTemplatePipeline dypnsVerifyCodeTemplatePipeline;

	private final AliyunDypnsVerifyCodeSender aliyunDypnsVerifyCodeSender;

	public DypnsVerifyCodeMessageSender(DypnsVerifyCodeTemplatePipeline dypnsVerifyCodeTemplatePipeline,
			AliyunDypnsVerifyCodeSender aliyunDypnsVerifyCodeSender) {
		this.dypnsVerifyCodeTemplatePipeline = dypnsVerifyCodeTemplatePipeline;
		this.aliyunDypnsVerifyCodeSender = aliyunDypnsVerifyCodeSender;
	}

	@Override
	public MessageChannel channel() {
		return MessageChannel.SMS_ALIYUN_DYPNS;
	}

	@Override
	public void sendByTemplate(TemplateMessageCommand command) {
		if (BooleanUtil.isTrue(command.getCustomBody())) {
			throw new SystemBusinessException(SMS_CUSTOM_BODY_NOT_SUPPORTED);
		}

		DypnsVerifyCodeSendPayload prepared = dypnsVerifyCodeTemplatePipeline.prepare(command);
		for (String phone : command.getTargets()) {
			aliyunDypnsVerifyCodeSender.sendVerifyCode(phone, prepared.providerTemplateCode(),
					prepared.templateParam());
		}
	}

}
