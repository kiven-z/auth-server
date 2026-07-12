package com.auth.service.system.message.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.message.convert.EmailTemplateConverter;
import com.auth.service.system.message.mapper.MessageTemplateMapper;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.form.*;
import com.auth.service.system.message.model.vo.MessageTemplateRequireFieldRow;
import com.auth.service.system.message.model.vo.email.EmailTemplateDetailVO;
import com.auth.service.system.message.service.MessageDispatchService;
import com.auth.service.system.message.service.SysEmailTemplateService;
import com.auth.service.system.message.support.template.FreemarkerTemplateRenderer;
import com.auth.service.system.message.support.template.MessageTemplateRequireFieldsJsonSupport;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 邮件模板服务（数据落库于 message_template，channel=EMAIL）
 *
 * @author Bunny
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysEmailTemplateServiceImpl extends ServiceImpl<MessageTemplateMapper, MessageTemplateEntity>
		implements SysEmailTemplateService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final MessageDispatchService messageDispatchService;

	public SysEmailTemplateServiceImpl(AuditUserDisplayService auditUserDisplayService,
			MessageDispatchService messageDispatchService) {
		this.auditUserDisplayService = auditUserDisplayService;
		this.messageDispatchService = messageDispatchService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EmailTemplateDetailVO getEmailTemplateById(Long id) {
		MessageTemplateEntity entity = getById(id);
		if (entity == null || !MessageChannel.EMAIL.name().equals(entity.getChannel())) {
			return null;
		}

		EmailTemplateDetailVO vo = EmailTemplateConverter.INSTANCE.toDetailVo(entity);
		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(vo), null, null);
		return vo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(EmailTemplateForm form) {
		MessageTemplateEntity entity = EmailTemplateConverter.INSTANCE.toEntity(form);
		entity.setChannel(MessageChannel.EMAIL.name());
		entity.setBodyContent(CharSequenceUtil.nullToDefault(entity.getBodyContent(), "Template content is empty"));
		entity.setRequireFields(MessageTemplateRequireFieldsJsonSupport.toJson(Collections.emptyList()));

		save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(EmailTemplateForm form) {
		MessageTemplateEntity exists = getById(form.getId());
		if (exists == null || !MessageChannel.EMAIL.name().equals(exists.getChannel())) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		EmailTemplateConverter.INSTANCE.updateEntity(form, exists);
		exists.setBodyContent(CharSequenceUtil.nullToDefault(form.getContent(), exists.getBodyContent()));
		updateById(exists);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateContent(Long id, EmailTemplateContentForm form) {
		MessageTemplateEntity exists = getById(id);
		if (exists == null || !MessageChannel.EMAIL.name().equals(exists.getChannel())) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		exists.setBodyContent(form.getContent());
		updateById(exists);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateRequireFields(Long id, EmailTemplateRequireFieldsForm form) {
		MessageTemplateEntity entity = getById(id);
		if (entity == null || !MessageChannel.EMAIL.name().equals(entity.getChannel())) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		String json = MessageTemplateRequireFieldsJsonSupport.toJson(form.getRequireFields());
		entity.setRequireFields(json);
		updateById(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String previewRenderedHtml(Long id) {
		MessageTemplateEntity entity = getById(id);
		if (entity == null || !MessageChannel.EMAIL.name().equals(entity.getChannel())) {
			return null;
		}

		List<MessageTemplateRequireFieldRow> rows = MessageTemplateRequireFieldsJsonSupport
			.parse(entity.getRequireFields());
		Map<String, Object> model = MessageTemplateRequireFieldsJsonSupport.toExampleModel(rows);
		return FreemarkerTemplateRenderer.processTemplate(entity.getTemplateName(), entity.getBodyContent(), model);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String render(EmailTemplateRenderForm form) {
		try {
			Map<String, Object> model = MessageTemplateRequireFieldsJsonSupport.toExampleModel(form.getRequireFields());
			return FreemarkerTemplateRenderer.processTemplate("inline-preview", form.getContent(), model);
		}
		catch (Exception e) {
			return "预览渲染失败，请检查变量声明与正文语法";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Async
	public void testSend(EmailTemplateTestSendForm form) {
		Long id = form.getId();
		MessageTemplateEntity entity = getById(id);
		if (entity == null || !MessageChannel.EMAIL.name().equals(entity.getChannel())) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		List<MessageTemplateRequireFieldRow> rows = MessageTemplateRequireFieldsJsonSupport
			.parse(entity.getRequireFields());
		Map<String, Object> model = MessageTemplateRequireFieldsJsonSupport.toExampleModel(rows);

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode(entity.getSceneCode())
			.targets(List.of(form.getEmail()))
			.variables(model)
			.options(EmailChannelOptions.builder().hasHtml(Boolean.TRUE).build())
			.build();
		messageDispatchService.sendByTemplate(command);
	}

}
