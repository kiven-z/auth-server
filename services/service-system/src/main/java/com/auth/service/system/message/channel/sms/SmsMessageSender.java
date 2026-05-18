package com.auth.service.system.message.channel.sms;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.sms.SmsNotificationSender;
import com.auth.module.message.sms.SmsSendMode;
import com.auth.module.message.sms.aliyun.SmsVerifyCodeSender;
import com.auth.module.message.spi.MessageSender;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.sms.model.PreparedSms;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.SMS_NOTIFICATION_NOT_ENABLED;
import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.SMS_SEND_FAILED;
import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.SMS_TARGET_EMPTY;
import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.SMS_VERIFY_NOT_ENABLED;

/**
 * 短信渠道发送实现：VERIFY 走 Dypnsapi，NOTIFY 走 sms4j（可选模块）
 *
 * @author Bunny
 */
@ConditionalOnProperty(prefix = "auth.message.sms", name = "enabled", havingValue = "true")
@Component
public class SmsMessageSender implements MessageSender {

	private final SmsTemplatePipeline smsTemplatePipeline;

	private final ObjectProvider<SmsVerifyCodeSender> smsVerifyCodeSender;

	private final ObjectProvider<SmsNotificationSender> smsNotificationSender;

	public SmsMessageSender(SmsTemplatePipeline smsTemplatePipeline,
			ObjectProvider<SmsVerifyCodeSender> smsVerifyCodeSender,
			ObjectProvider<SmsNotificationSender> smsNotificationSender) {
		this.smsTemplatePipeline = smsTemplatePipeline;
		this.smsVerifyCodeSender = smsVerifyCodeSender;
		this.smsNotificationSender = smsNotificationSender;
	}

	@Override
	public MessageChannel channel() {
		return MessageChannel.SMS;
	}

	@Override
	public void sendByTemplate(TemplateMessageCommand command) {
		if (CollUtil.isEmpty(command.getTargets())) {
			throw new SystemBusinessException(SMS_TARGET_EMPTY);
		}
		PreparedSms prepared = smsTemplatePipeline.prepare(command);
		for (String phone : command.getTargets()) {
			dispatch(phone, prepared);
		}
	}

	private void dispatch(String phone, PreparedSms prepared) {
		try {
			if (prepared.sendMode() == SmsSendMode.NOTIFY) {
				sendNotify(phone, prepared);
				return;
			}
			sendVerify(phone, prepared);
		}
		catch (IllegalStateException e) {
			throw new SystemBusinessException(SMS_SEND_FAILED, e.getMessage());
		}
	}

	private void sendVerify(String phone, PreparedSms prepared) {
		SmsVerifyCodeSender sender = smsVerifyCodeSender.getIfAvailable();
		if (sender == null) {
			throw new SystemBusinessException(SMS_VERIFY_NOT_ENABLED);
		}
		sender.sendVerifyCode(phone, prepared.providerTemplateCode(), prepared.signName(), prepared.templateParam(),
				prepared.countryCode(), prepared.schemeName());
	}

	private void sendNotify(String phone, PreparedSms prepared) {
		SmsNotificationSender sender = smsNotificationSender.getIfAvailable();
		if (sender == null) {
			throw new SystemBusinessException(SMS_NOTIFICATION_NOT_ENABLED);
		}
		sender.sendTemplateSms(phone, prepared.providerTemplateCode(), prepared.signName(), prepared.templateParam(),
				prepared.smsConfigId());
	}

}
