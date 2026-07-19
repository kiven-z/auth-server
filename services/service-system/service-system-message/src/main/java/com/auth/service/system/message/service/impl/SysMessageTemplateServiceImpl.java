package com.auth.service.system.message.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.message.convert.MessageTemplateConverter;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.MessageTemplateMapper;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.enums.InAppContentType;
import com.auth.service.system.message.model.form.MessageTemplateRequireFieldsForm;
import com.auth.service.system.message.model.form.MessageTemplateStatusForm;
import com.auth.service.system.message.model.form.MessageTemplateTestSendForm;
import com.auth.service.system.message.model.po.MessageTemplatePageRowPO;
import com.auth.service.system.message.model.query.MessageTemplateQuery;
import com.auth.service.system.message.model.vo.template.MessageTemplateDetailVO;
import com.auth.service.system.message.model.vo.template.MessageTemplatePageResponse;
import com.auth.service.system.message.model.vo.template.MessageTemplateRequireFieldRow;
import com.auth.service.system.message.service.MessageDispatchService;
import com.auth.service.system.message.service.SysMessageTemplateService;
import com.auth.service.system.message.support.template.FreemarkerTemplateRenderer;
import com.auth.service.system.message.support.template.MessageTemplateRequireFieldsJsonSupport;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_INVALID;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static com.auth.service.system.message.exception.MessageResultCode.MESSAGE_CHANNEL_UNSUPPORTED;

/**
 * 消息模板公共服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class SysMessageTemplateServiceImpl extends ServiceImpl<MessageTemplateMapper, MessageTemplateEntity>
		implements SysMessageTemplateService {

	private final MessageTemplateMapper messageTemplateMapper;

	private final AuditUserDisplayService auditUserDisplayService;

	private final MessageDispatchService messageDispatchService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<MessageTemplatePageResponse> getMessageTemplatePage(MessageTemplateQuery query) {
		Page<MessageTemplateEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<MessageTemplatePageRowPO> page = messageTemplateMapper.selectMessageTemplatePage(pageParams, query);
		IPage<MessageTemplatePageResponse> voPage = page.convert(MessageTemplateConverter.INSTANCE::toPageResponse);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public MessageTemplateDetailVO getMessageTemplateById(Long id, String channel) {
		MessageTemplateEntity entity = requireTemplate(id, channel);
		MessageTemplateDetailVO vo = MessageTemplateConverter.INSTANCE.toDetailVo(entity);

		String contentType = switch (MessageChannel.valueOf(entity.getChannel())) {
			case EMAIL -> "HTML";
			case IN_APP -> InAppContentType.from(entity.getImMessageType()).name();
			default -> InAppContentType.TEXT.name();
		};
		vo.setContentType(contentType);

		try {
			// 详情仍填充 preview；缺 exampleValue / 渲染失败时软降级为空，不拖垮详情
			String previewContent = renderPreviewContent(entity);
			vo.setPreviewContent(previewContent);
		}
		catch (MessageException ignored) {
			vo.setPreviewContent(null);
		}

		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(vo), null, null);
		return vo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<MessageTemplateRequireFieldRow> getRequireFields(Long id, String channel) {
		MessageTemplateEntity entity = requireTemplate(id, channel);
		String requireFields = entity.getRequireFields();

		return MessageTemplateRequireFieldsJsonSupport.parse(requireFields);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void batchDelete(List<Long> ids, String channel) {
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		super.remove(Wrappers.lambdaQuery(MessageTemplateEntity.class)
			.in(MessageTemplateEntity::getId, ids)
			.eq(MessageTemplateEntity::getChannel, channel));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void batchUpdateStatus(MessageTemplateStatusForm form) {
		List<Long> ids = form.getIds();
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		super.update(Wrappers.lambdaUpdate(MessageTemplateEntity.class)
			.in(MessageTemplateEntity::getId, ids)
			.eq(MessageTemplateEntity::getChannel, form.getChannel())
			.set(MessageTemplateEntity::getStatus, form.getStatus()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateRequireFields(MessageTemplateRequireFieldsForm form) {
		List<MessageTemplateRequireFieldRow> requireFields = form.getRequireFields();
		String json = MessageTemplateRequireFieldsJsonSupport.toJson(requireFields);

		MessageTemplateEntity entity = requireTemplate(form.getId(), form.getChannel());
		entity.setRequireFields(json);
		super.updateById(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void testSend(MessageTemplateTestSendForm form) {
		// 解析渠道枚举；非法名称视为不支持
		String formChannel = form.getChannel();
		MessageChannel channel;
		try {
			channel = MessageChannel.valueOf(formChannel);
		}
		catch (IllegalArgumentException ex) {
			throw new MessageException(MESSAGE_CHANNEL_UNSUPPORTED, formChannel);
		}

		// 按渠道校验接收目标格式
		String target = form.getTarget();
		boolean valid = switch (channel) {
			case EMAIL -> Validator.isEmail(target);
			case SMS -> Validator.isMobile(target);
			case DING_TALK, IN_APP -> true;
		};
		if (!valid) {
			throw new MessageException(DATA_INVALID, "send target is invalid");
		}

		MessageTemplateEntity entity = requireTemplate(form.getId(), channel.name());
		String requireFields = entity.getRequireFields();
		List<MessageTemplateRequireFieldRow> rows = MessageTemplateRequireFieldsJsonSupport.parse(requireFields);
		Map<String, Object> variables = MessageTemplateRequireFieldsJsonSupport.toExampleModel(rows);

		// 组装渠道默认扩展参数
		EmailChannelOptions options = channel == MessageChannel.EMAIL
				? EmailChannelOptions.builder().hasHtml(Boolean.TRUE).build() : null;
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(channel)
			.templateCode(entity.getSceneCode())
			.targets(List.of(target))
			.variables(variables)
			.options(options)
			.build();
		messageDispatchService.sendByTemplate(command);
	}

	/**
	 * 按主键加载模板，并校验渠道一致
	 * @param id 模板主键
	 * @param channel 期望渠道
	 * @return 模板实体
	 */
	private MessageTemplateEntity requireTemplate(Long id, String channel) {
		MessageTemplateEntity entity = messageTemplateMapper.selectById(id);
		if (entity == null || !channel.equals(entity.getChannel())) {
			throw new MessageException(DATA_NOT_EXIST);
		}
		return entity;
	}

	/**
	 * 按 require_fields 示例值 FreeMarker 渲染预览正文
	 * @param entity 模板实体
	 * @return 渲染后正文
	 */
	private String renderPreviewContent(MessageTemplateEntity entity) {
		String requireFields = entity.getRequireFields();
		List<MessageTemplateRequireFieldRow> rows = MessageTemplateRequireFieldsJsonSupport.parse(requireFields);
		Map<String, Object> model = MessageTemplateRequireFieldsJsonSupport.toExampleModel(rows);

		String templateName = CharSequenceUtil.blankToDefault(entity.getTemplateName(), "preview");
		String templateContent = CharSequenceUtil.nullToEmpty(entity.getBodyContent());
		return FreemarkerTemplateRenderer.processTemplate(templateName, templateContent, model);
	}

}
