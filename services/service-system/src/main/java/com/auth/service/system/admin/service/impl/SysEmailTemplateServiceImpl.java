package com.auth.service.system.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.email.EmailSendDTO;
import com.auth.service.system.admin.convert.EmailTemplateConvert;
import com.auth.service.system.admin.mapper.MessageTemplateMapper;
import com.auth.service.system.admin.model.entity.MessageTemplateEntity;
import com.auth.service.system.admin.model.form.emailtemplate.*;
import com.auth.service.system.admin.model.po.emailtemplate.EmailTemplatePageRowPO;
import com.auth.service.system.admin.model.query.EmailTemplateQuery;
import com.auth.service.system.admin.model.vo.emailtemplate.EmailTemplateDetailVO;
import com.auth.service.system.admin.model.vo.emailtemplate.EmailTemplatePageResponse;
import com.auth.service.system.admin.model.vo.emailtemplate.EmailTemplateRequireFieldRow;
import com.auth.service.system.admin.service.SysEmailTemplateService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.message.api.EmailSendService;
import com.auth.service.system.message.shared.FreeMarkTemplateRenderer;
import com.auth.service.system.message.shared.MessageTemplateRequireFieldsJsonSupport;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.auth.service.system.common.exception.code.common.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 邮件模板服务（数据落库于 sys_message_template，channel=EMAIL）
 *
 * @author Bunny
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysEmailTemplateServiceImpl extends ServiceImpl<MessageTemplateMapper, MessageTemplateEntity>
		implements SysEmailTemplateService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final EmailSendService emailSendService;

	public SysEmailTemplateServiceImpl(AuditUserDisplayService auditUserDisplayService,
			EmailSendService emailSendService) {
		this.auditUserDisplayService = auditUserDisplayService;
		this.emailSendService = emailSendService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<EmailTemplatePageResponse> getEmailTemplatePage(Page<MessageTemplateEntity> pageParams,
			EmailTemplateQuery query) {
		IPage<EmailTemplatePageRowPO> page = baseMapper.selectEmailTemplatePage(pageParams, query);
		IPage<EmailTemplatePageResponse> voPage = page.convert(EmailTemplateConvert.INSTANCE::toPageResponse);
		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
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

		EmailTemplateDetailVO vo = new EmailTemplateDetailVO();
		vo.setTemplateCode(entity.getSceneCode());
		vo.setTemplateName(entity.getTemplateName());
		vo.setDescription(entity.getDescription());
		vo.setSubject(entity.getSubject());
		vo.setContent(entity.getBodyContent());
		vo.setPriority(entity.getPriority());
		vo.setStatus(entity.getStatus());

		List<EmailTemplateRequireFieldRow> requireFields = MessageTemplateRequireFieldsJsonSupport
			.parse(entity.getRequireFields());
		vo.setRequireFields(requireFields);

		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(vo));
		return vo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(EmailTemplateForm form) {
		MessageTemplateEntity entity = BeanUtil.copyProperties(form, MessageTemplateEntity.class, "id", "templateCode",
				"content", "requireFields");
		entity.setSceneCode(form.getTemplateCode());
		entity.setChannel(MessageChannel.EMAIL.name());
		entity.setBodyContent(CharSequenceUtil.nullToDefault(form.getContent(), "Template content is empty"));
		List<EmailTemplateRequireFieldRow> rows = form.getRequireFields() != null ? form.getRequireFields()
				: Collections.emptyList();
		entity.setRequireFields(MessageTemplateRequireFieldsJsonSupport.toJson(rows));
		save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateMetadata(EmailTemplateMetadataForm form) {
		MessageTemplateEntity exists = getById(form.getId());
		if (exists == null || !MessageChannel.EMAIL.name().equals(exists.getChannel())) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		exists.setSceneCode(form.getTemplateCode());
		exists.setTemplateName(form.getTemplateName());
		exists.setDescription(form.getDescription());
		exists.setSubject(form.getSubject());
		exists.setPriority(form.getPriority());
		exists.setStatus(form.getStatus());

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
	public void batchUpdateStatus(EmailTemplateStatusForm form) {
		update(Wrappers.<MessageTemplateEntity>lambdaUpdate()
			.in(MessageTemplateEntity::getId, form.getIds())
			.eq(MessageTemplateEntity::getChannel, MessageChannel.EMAIL.name())
			.set(MessageTemplateEntity::getStatus, form.getStatus()));
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

		List<EmailTemplateRequireFieldRow> rows = MessageTemplateRequireFieldsJsonSupport
			.parse(entity.getRequireFields());
		Map<String, Object> model = MessageTemplateRequireFieldsJsonSupport.toExampleModel(rows);
		return FreeMarkTemplateRenderer.processTemplate(entity.getTemplateName(), entity.getBodyContent(), model);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String render(EmailTemplateRenderForm form) {
		try {
			Map<String, Object> model = MessageTemplateRequireFieldsJsonSupport.toExampleModel(form.getRequireFields());
			return FreeMarkTemplateRenderer.processTemplate("inline-preview", form.getContent(), model);
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

		EmailSendDTO emailSendDTO = new EmailSendDTO();
		emailSendDTO.setHasHtml(Boolean.TRUE);

		String templateCode = entity.getSceneCode();
		emailSendDTO.setTemplateCode(templateCode);

		String email = form.getEmail();
		emailSendDTO.setTo(List.of(email));

		List<EmailTemplateRequireFieldRow> rows = MessageTemplateRequireFieldsJsonSupport
			.parse(entity.getRequireFields());
		Map<String, Object> model = MessageTemplateRequireFieldsJsonSupport.toExampleModel(rows);
		emailSendDTO.setVariables(model);

		emailSendService.sendByTemplate(emailSendDTO);
	}

}
