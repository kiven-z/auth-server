package com.auth.service.system.message.channel.sms;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.sms.aliyun.SmsVerifyCodeSender;
import com.auth.module.message.spi.MessageSender;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.sms.model.PreparedSms;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static com.auth.service.system.common.exception.SystemResultCodeEnum.SMS_SEND_FAILED;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.SMS_TARGET_EMPTY;

/**
 * 短信渠道发送实现
 *
 * @author Bunny
 */
@ConditionalOnBean(SmsVerifyCodeSender.class)
@ConditionalOnProperty(prefix = "auth.message.sms", name = "enabled", havingValue = "true")
@Component
public class SmsMessageSender implements MessageSender {

	private final SmsTemplatePipeline smsTemplatePipeline;

	private final SmsVerifyCodeSender smsVerifyCodeSender;

	public SmsMessageSender(SmsTemplatePipeline smsTemplatePipeline, SmsVerifyCodeSender smsVerifyCodeSender) {
		this.smsTemplatePipeline = smsTemplatePipeline;
		this.smsVerifyCodeSender = smsVerifyCodeSender;
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
		if (CollUtil.isEmpty(command.getTargets())) {
			throw new SystemBusinessException(SMS_TARGET_EMPTY);
		}
		PreparedSms prepared = smsTemplatePipeline.prepare(command);
		for (String phone : command.getTargets()) {
			try {
				smsVerifyCodeSender.sendVerifyCode(phone, prepared.providerTemplateCode(), prepared.signName(),
						prepared.templateParam(), prepared.countryCode(), prepared.schemeName());
			}
			catch (IllegalStateException e) {
				throw new SystemBusinessException(SMS_SEND_FAILED, e.getMessage());
			}
		}
	}

}
