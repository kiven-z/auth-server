package com.auth.service.system.email.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.email.model.EmailSendDTO;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.helper.PageResultHelper;
import com.auth.service.system.email.mapper.EmailTemplateMapper;
import com.auth.service.system.email.model.EmailTemplateEntity;
import com.auth.service.system.email.model.EmailTemplateRequireFieldRow;
import com.auth.service.system.email.model.form.*;
import com.auth.service.system.email.model.query.EmailTemplateQuery;
import com.auth.service.system.email.model.vo.EmailTemplateDetailVO;
import com.auth.service.system.email.model.vo.EmailTemplatePageResponse;
import com.auth.service.system.email.service.EmailSendService;
import com.auth.service.system.email.service.EmailTemplateService;
import com.auth.service.system.email.support.EmailTemplateRequireFieldsJsonSupport;
import com.auth.service.system.email.utils.MailFreeMarkUtil;
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

	/**
	 * 分页结果辅助：填充创建人/更新人用户名等展示字段
	 */
	private final PageResultHelper pageResultHelper;

	private final EmailSendService emailSendService;

	/**
	 * @param pageResultHelper 分页展示辅助
	 */
	public EmailTemplateServiceImpl(PageResultHelper pageResultHelper, EmailSendService emailSendService) {
		this.pageResultHelper = pageResultHelper;
		this.emailSendService = emailSendService;
	}

	/**
	 * 条件分页查询邮件模板列表
	 * @param pageParams 分页参数
	 * @param query 筛选条件
	 * @return 分页数据（含创建人/更新人用户名）
	 */
	@Override
	public PageResponse<EmailTemplatePageResponse> getEmailTemplatePage(Page<EmailTemplateEntity> pageParams,
			EmailTemplateQuery query) {
		IPage<EmailTemplatePageResponse> page = baseMapper.selectListByPage(pageParams, query);
		pageResultHelper.setUpdateWithCreateUsername(page);
		return PageResponse.of(page);
	}

	/**
	 * 按主键查询模板详情；不存在时返回 {@code null} 将库中 JSON 格式的必填字段解析为列表，并补充创建人/更新人用户名
	 * @param id 模板主键
	 * @return 详情 VO，未找到则为 null
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

		return vo;
	}

	/**
	 * 新增邮件模板，将必填字段列表序列化为 JSON 存入数据库
	 * @param form 表单数据
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
	 * 仅更新元数据字段，不修改 content、require_fields
	 * @param form 元数据（须含主键）
	 */
	@Override
	public void updateMetadata(EmailTemplateMetadataForm form) {
		EmailTemplateEntity exists = getById(form.getId());
		if (exists == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		update(Wrappers.<EmailTemplateEntity>lambdaUpdate()
			.eq(EmailTemplateEntity::getId, form.getId())
			.set(EmailTemplateEntity::getTemplateCode, form.getTemplateCode())
			.set(EmailTemplateEntity::getTemplateName, form.getTemplateName())
			.set(EmailTemplateEntity::getDescription, form.getDescription())
			.set(EmailTemplateEntity::getSubject, form.getSubject())
			.set(EmailTemplateEntity::getPriority, form.getPriority())
			.set(EmailTemplateEntity::getStatus, form.getStatus()));
	}

	/**
	 * 仅更新邮件正文 content
	 * @param id 模板主键
	 * @param form 正文表单
	 */
	@Override
	public void updateContent(Long id, EmailTemplateContentForm form) {
		EmailTemplateEntity exists = getById(id);
		if (exists == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		update(Wrappers.<EmailTemplateEntity>lambdaUpdate()
			.eq(EmailTemplateEntity::getId, id)
			.set(EmailTemplateEntity::getContent, form.getContent()));
	}

	/**
	 * 仅更新模板的必填变量定义（require_fields），其它字段不变
	 * @param id 模板主键
	 * @param form 必填字段列表
	 */
	@Override
	public void updateRequireFields(Long id, EmailTemplateRequireFieldsForm form) {
		EmailTemplateEntity entity = getById(id);
		if (entity == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		entity.setRequireFields(EmailTemplateRequireFieldsJsonSupport.toJson(form.getRequireFields()));
		updateById(entity);
	}

	/**
	 * 批量启用或禁用模板
	 * @param form 主键列表与目标状态
	 */
	@Override
	public void batchUpdateStatus(EmailTemplateStatusForm form) {
		update(Wrappers.<EmailTemplateEntity>lambdaUpdate()
			.in(EmailTemplateEntity::getId, form.getIds())
			.set(EmailTemplateEntity::getStatus, form.getStatus()));
	}

	/**
	 * 服务端预览：使用已保存模板内容与必填字段中的示例值，经 FreeMarker 渲染为 HTML 模板不存在时返回 {@code null}
	 * @param id 模板主键
	 * @return 渲染后的 HTML，未找到模板则为 null
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
	 * 编辑态实时预览：不读库，用传入内容与必填字段生成示例模型后渲染； 渲染异常时返回固定提示文案，避免接口抛错打断前端编辑
	 * @param form 内容与必填字段
	 * @return 渲染后的 HTML 或错误提示字符串
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
	 * 向指定邮箱发送测试邮件：按库中模板与必填字段示例值渲染正文与主题，同步发送 模板不存在、渲染失败或 SMTP 发送失败时抛出业务异常
	 * @param form 模板主键与收件人邮箱
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
