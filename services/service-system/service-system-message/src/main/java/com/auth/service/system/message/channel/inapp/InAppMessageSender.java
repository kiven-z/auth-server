package com.auth.service.system.message.channel.inapp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.NumberUtil;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.channel.MessageSender;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.inapp.InAppChannelOptions;
import com.auth.service.system.message.convert.InAppEntityAssembler;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.InAppMessageMapper;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.entity.InAppMessageRecipientEntity;
import com.auth.service.system.message.model.enums.InAppContentType;
import com.auth.service.system.message.model.enums.InAppMessageStatus;
import com.auth.service.system.message.model.enums.MessageSendSourceType;
import com.auth.service.system.message.model.value.inapp.RenderedInAppMessage;
import com.auth.service.system.message.service.InAppMessageRecipientWriteService;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_TARGET_INVALID;

/**
 * 站内信渠道发送实现：渲染正文写入任务主表，再展开收件箱
 *
 * @author Bunny
 */
@Component
public class InAppMessageSender implements MessageSender {

	private final InAppTemplatePipeline inAppTemplatePipeline;

	private final InAppMessageMapper inAppMessageMapper;

	private final InAppMessageRecipientWriteService inAppMessageRecipientWriteService;

	public InAppMessageSender(InAppTemplatePipeline inAppTemplatePipeline, InAppMessageMapper inAppMessageMapper,
			InAppMessageRecipientWriteService inAppMessageRecipientWriteService) {
		this.inAppTemplatePipeline = inAppTemplatePipeline;
		this.inAppMessageMapper = inAppMessageMapper;
		this.inAppMessageRecipientWriteService = inAppMessageRecipientWriteService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageChannel channel() {
		return MessageChannel.IN_APP;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendByTemplate(TemplateMessageCommand command) {
		List<Long> userIds = CollUtil.emptyIfNull(command.getTargets())
			.stream()
			.filter(CharSequenceUtil::isNotBlank)
			.map(String::trim)
			.distinct()
			.map(target -> {
				if (NumberUtil.isLong(target)) {
					return Long.valueOf(target);
				}
				throw new MessageException(IN_APP_TARGET_INVALID, target);
			})
			.toList();
		if (userIds.isEmpty()) {
			return;
		}

		RenderedInAppMessage rendered = resolveContent(command);
		InAppMessageEntity task = InAppEntityAssembler.toDirectTask(rendered, userIds);
		inAppMessageMapper.insert(task);

		List<InAppMessageRecipientEntity> rows = InAppEntityAssembler.toRecipientEntities(task.getId(), userIds);
		int inserted = inAppMessageRecipientWriteService.insertBatch(rows);
		InAppMessageStatus finalStatus = inserted > 0 ? InAppMessageStatus.SUCCESS : InAppMessageStatus.FAILED;
		inAppMessageMapper.finishTask(task.getId(), finalStatus.name(), inserted);
	}

	/**
	 * 解析发送定稿：自定义正文或模板渲染，并补齐来源与渠道选项
	 * @param command 发送命令
	 * @return 定稿快照
	 */
	private RenderedInAppMessage resolveContent(TemplateMessageCommand command) {
		InAppChannelOptions options = command.getOptions() instanceof InAppChannelOptions inApp ? inApp
				: new InAppChannelOptions();

		if (!Boolean.TRUE.equals(command.getCustomBody())) {
			return inAppTemplatePipeline.render(command.getTemplateCode(), command.getVariables())
				.toBuilder()
				.bizType(options.getBizType())
				.linkUrl(options.getLinkUrl())
				.sourceType(MessageSendSourceType.TEMPLATE)
				.build();
		}
		if (CharSequenceUtil.isBlank(command.getBody())) {
			throw new MessageException(PARAM_REQUIRED, "站内信正文");
		}
		if (CharSequenceUtil.isBlank(options.getTitle())) {
			throw new MessageException(PARAM_REQUIRED, "站内信标题");
		}
		return RenderedInAppMessage.builder()
			.title(options.getTitle())
			.content(command.getBody())
			.contentType(InAppContentType.from(options.getContentType()))
			.sceneCode(command.getTemplateCode())
			.bizType(options.getBizType())
			.linkUrl(options.getLinkUrl())
			.sourceType(MessageSendSourceType.SYSTEM)
			.build();
	}

}
