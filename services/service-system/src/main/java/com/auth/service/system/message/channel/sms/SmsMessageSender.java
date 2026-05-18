package com.auth.service.system.message.channel.sms;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.modules.sms.SmsSender;
import com.auth.module.message.spi.MessageSender;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.sms.model.SmsPreparedMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.*;

/**
 * 短信渠道发送实现（阿里云 Dypns 验证码短信）
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
		List<String> targets = command.getTargets();
		if (CollUtil.isEmpty(targets)) {
			throw new SystemBusinessException(SMS_TARGET_EMPTY);
		}

		SmsPreparedMessage prepared = smsTemplatePipeline.prepare(command);
		SmsSender sender = smsSender.getIfAvailable();
		if (sender == null) {
			throw new SystemBusinessException(SMS_SENDER_NOT_ENABLED);
		}

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
			sender.sendVerifyCode(phone, prepared.signName(), prepared.providerTemplateCode(),
					prepared.templateParam());
		}
		catch (IllegalStateException e) {
			throw new SystemBusinessException(SMS_SEND_FAILED, e.getMessage());
		}
	}

}
