package com.auth.service.system.message.channel.sms;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.sms.SmsSender;
import com.auth.module.message.spi.MessageSender;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.sms.model.SmsPreparedMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.*;

/**
 * 短信渠道发送实现（统一经 sms4j 标准模板短信）
 *
 * @author Bunny
 */
@ConditionalOnProperty(prefix = "auth.message.sms", name = "enabled", havingValue = "true")
@Component
public class SmsMessageSender implements MessageSender {

	private final SmsTemplatePipeline smsTemplatePipeline;

	private final ObjectProvider<SmsSender> smsSender;

	public SmsMessageSender(SmsTemplatePipeline smsTemplatePipeline, ObjectProvider<SmsSender> smsSender) {
		this.smsTemplatePipeline = smsTemplatePipeline;
		this.smsSender = smsSender;
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
		// 获取手机号列表
		List<String> targets = command.getTargets();
		if (CollUtil.isEmpty(targets)) {
			throw new SystemBusinessException(SMS_TARGET_EMPTY);
		}

		// 准备短信发送参数
		SmsPreparedMessage prepared = smsTemplatePipeline.prepare(command);
		// 获取短信发送器
		SmsSender sender = smsSender.getIfAvailable();
		// 如果短信发送器不存在，则抛出异常
		if (sender == null) {
			throw new SystemBusinessException(SMS_SMS4J_NOT_ENABLED);
		}

		// 发送短信
		for (String phone : targets) {
			sendOne(sender, phone, prepared);
		}
	}

	/**
	 * 发送单条短信
	 * @param sender 短信发送器
	 * @param phone 手机号
	 * @param prepared 准备好的短信发送参数
	 */
	private void sendOne(SmsSender sender, String phone, SmsPreparedMessage prepared) {
		try {
			// 发送短信
			sender.sendTemplateSms(phone, prepared.providerTemplateCode(), prepared.signName(),
					prepared.templateParam(), prepared.smsConfigId());
		}
		catch (IllegalStateException e) {
			throw new SystemBusinessException(SMS_SEND_FAILED, e.getMessage());
		}
	}

}
