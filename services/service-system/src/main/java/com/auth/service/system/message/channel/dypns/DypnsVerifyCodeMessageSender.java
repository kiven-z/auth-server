package com.auth.service.system.message.channel.dypns;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.sender.modules.sms.dypns.DypnsVerifyCodeSendPayload;
import com.auth.module.message.sender.modules.sms.dypns.DypnsVerifyCodeSender;
import com.auth.module.message.sender.spi.MessageSender;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.*;

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
		List<String> targets = command.getTargets();
		if (CollUtil.isEmpty(targets)) {
			throw new SystemBusinessException(SMS_TARGET_EMPTY);
		}

		DypnsVerifyCodeSendPayload prepared = dypnsVerifyCodeTemplatePipeline.prepare(command);
		DypnsVerifyCodeSender sender = dypnsVerifyCodeSender.getIfAvailable();
		if (sender == null) {
			throw new SystemBusinessException(SMS_SENDER_NOT_ENABLED);
		}

		for (String phone : targets) {
			sendOne(sender, phone, prepared);
		}
	}

	/**
	 * 发送单条 Dypns 验证码短信
	 * @param sender Dypns 发送器
	 * @param phone 手机号
	 * @param prepared 准备好的发送参数
	 */
	private void sendOne(DypnsVerifyCodeSender sender, String phone, DypnsVerifyCodeSendPayload prepared) {
		try {
			sender.sendVerifyCode(phone, prepared.providerTemplateCode(), prepared.templateParam());
		}
		catch (IllegalStateException e) {
			throw new SystemBusinessException(SMS_SEND_FAILED, e.getMessage());
		}
	}

}
