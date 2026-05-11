package com.auth.service.system.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.email.model.EmailSendDTO;
import com.auth.service.system.admin.convert.EmailTemplateConvert;
import com.auth.service.system.admin.mapper.EmailTemplateMapper;
import com.auth.service.system.admin.model.entity.EmailTemplateEntity;
import com.auth.service.system.admin.model.form.emailtemplate.*;
import com.auth.service.system.admin.model.po.emailtemplate.EmailTemplatePageRowPO;
import com.auth.service.system.admin.model.query.EmailTemplateQuery;
import com.auth.service.system.admin.model.vo.emailtemplate.EmailTemplateDetailVO;
import com.auth.service.system.admin.model.vo.emailtemplate.EmailTemplatePageResponse;
import com.auth.service.system.admin.model.vo.emailtemplate.EmailTemplateRequireFieldRow;
import com.auth.service.system.admin.service.EmailTemplateService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.message.service.EmailSendService;
import com.auth.service.system.message.support.EmailTemplateRequireFieldsJsonSupport;
import com.auth.service.system.message.utils.MailFreeMarkUtil;
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

import static com.auth.service.system.common.exception.SystemResultCodeEnum.DATA_NOT_EXIST;

/**
 * 邮件模板业务实现：分页查询、详情、增删改、必填变量维护、FreeMarker 预览/渲染及测试发送
 *
 * @author Bunny
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class EmailTemplateServiceImpl extends ServiceImpl<EmailTemplateMapper, EmailTemplateEntity>
		implements EmailTemplateService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final EmailSendService emailSendService;

	public EmailTemplateServiceImpl(AuditUserDisplayService auditUserDisplayService,
			EmailSendService emailSendService) {
		this.auditUserDisplayService = auditUserDisplayService;
		this.emailSendService = emailSendService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<EmailTemplatePageResponse> getEmailTemplatePage(Page<EmailTemplateEntity> pageParams,
			EmailTemplateQuery query) {
		IPage<EmailTemplatePageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		// PO 转 VO
		IPage<EmailTemplatePageResponse> voPage = page.convert(EmailTemplateConvert.INSTANCE::toPageResponse);
		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EmailTemplateDetailVO getEmailTemplateById(Long id) {
		EmailTemplateEntity entity = getById(id);
		if (entity == null) {
			return null;
		}

		EmailTemplateDetailVO vo = new EmailTemplateDetailVO();
		BeanUtil.copyProperties(entity, vo, "requireFields");

		// 设置变量声明列表
		List<EmailTemplateRequireFieldRow> requireFields = EmailTemplateRequireFieldsJsonSupport
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
		EmailTemplateEntity entity = BeanUtil.copyProperties(form, EmailTemplateEntity.class);
		entity.setContent(CharSequenceUtil.nullToDefault(form.getContent(), "Template content is empty"));
		List<EmailTemplateRequireFieldRow> rows = form.getRequireFields() != null ? form.getRequireFields()
				: Collections.emptyList();
		entity.setRequireFields(EmailTemplateRequireFieldsJsonSupport.toJson(rows));
		save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateMetadata(EmailTemplateMetadataForm form) {
		EmailTemplateEntity exists = getById(form.getId());
		if (exists == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		exists.setTemplateCode(form.getTemplateCode());
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
		EmailTemplateEntity exists = getById(id);
		if (exists == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		exists.setContent(form.getContent());
		updateById(exists);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateRequireFields(Long id, EmailTemplateRequireFieldsForm form) {
		EmailTemplateEntity entity = getById(id);
		if (entity == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		String json = EmailTemplateRequireFieldsJsonSupport.toJson(form.getRequireFields());
		entity.setRequireFields(json);
		updateById(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void batchUpdateStatus(EmailTemplateStatusForm form) {
		update(Wrappers.<EmailTemplateEntity>lambdaUpdate()
			.in(EmailTemplateEntity::getId, form.getIds())
			.set(EmailTemplateEntity::getStatus, form.getStatus()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String previewRenderedHtml(Long id) {
		EmailTemplateEntity entity = getById(id);
		if (entity == null) {
			return null;
		}

		List<EmailTemplateRequireFieldRow> rows = EmailTemplateRequireFieldsJsonSupport
			.parse(entity.getRequireFields());
		Map<String, Object> model = EmailTemplateRequireFieldsJsonSupport.toExampleModel(rows);
		return MailFreeMarkUtil.processTemplate(entity.getTemplateName(), entity.getContent(), model);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String render(EmailTemplateRenderForm form) {
		try {
			Map<String, Object> model = EmailTemplateRequireFieldsJsonSupport.toExampleModel(form.getRequireFields());
			return MailFreeMarkUtil.processTemplate("inline-preview", form.getContent(), model);
		}
		catch (Exception e) {
			return "部分内容渲染失败，请检查模板中的变量是否均已填写";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Async
	public void testSend(EmailTemplateTestSendForm form) {
		// 获取并验证模板实体是否存在
		Long id = form.getId();
		EmailTemplateEntity entity = getById(id);
		if (entity == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		// 创建邮件发送实体
		EmailSendDTO emailSendDTO = new EmailSendDTO();
		emailSendDTO.setHasHtml(Boolean.TRUE);

		// 设置模板
		String templateCode = entity.getTemplateCode();
		emailSendDTO.setTemplateCode(templateCode);

		// 设置邮箱
		String email = form.getEmail();
		emailSendDTO.setTo(List.of(email));

		// 解析必填字段并生成示例数据模型
		List<EmailTemplateRequireFieldRow> rows = EmailTemplateRequireFieldsJsonSupport
			.parse(entity.getRequireFields());
		Map<String, Object> model = EmailTemplateRequireFieldsJsonSupport.toExampleModel(rows);
		emailSendDTO.setVariables(model);

		// 调用邮件服务同步发送邮件
		emailSendService.sendByTemplate(emailSendDTO);
	}

}
