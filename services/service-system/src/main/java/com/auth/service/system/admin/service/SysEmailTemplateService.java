package com.auth.service.system.admin.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.EmailTemplateEntity;
import com.auth.service.system.admin.model.form.emailtemplate.*;
import com.auth.service.system.admin.model.query.EmailTemplateQuery;
import com.auth.service.system.admin.model.vo.emailtemplate.EmailTemplateDetailVO;
import com.auth.service.system.admin.model.vo.emailtemplate.EmailTemplatePageResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 邮件模板服务
 *
 * @author Bunny
 */
public interface SysEmailTemplateService extends IService<EmailTemplateEntity> {

	/**
	 * 分页查询邮件模板
	 * @param pageParams 分页参数
	 * @param query 查询参数
	 * @return 邮件模板分页数据
	 */
	PageResponse<EmailTemplatePageResponse> getEmailTemplatePage(Page<EmailTemplateEntity> pageParams,
			EmailTemplateQuery query);

	/**
	 * 邮件模板详情
	 * @param id 模板ID
	 * @return 邮件模板详情
	 */
	EmailTemplateDetailVO getEmailTemplateById(Long id);

	/**
	 * 添加邮件模板
	 * @param form 邮件模板表单
	 */
	void create(EmailTemplateForm form);

	/**
	 * 仅更新元数据（编码、名称、描述、主题、优先级、状态），不修改 content 与 require_fields
	 * @param form 元数据表单
	 */
	void updateMetadata(EmailTemplateMetadataForm form);

	/**
	 * 仅更新邮件正文 content
	 * @param id 模板主键
	 * @param form 正文表单
	 */
	void updateContent(Long id, EmailTemplateContentForm form);

	/**
	 * 仅更新 require_fields
	 * @param id 模板ID
	 * @param form 变量列表表单
	 */
	void updateRequireFields(Long id, EmailTemplateRequireFieldsForm form);

	/**
	 * 批量启用/禁用邮件模板
	 * @param form 批量启用/禁用参数
	 */
	void batchUpdateStatus(EmailTemplateStatusForm form);

	/**
	 * 预览：将数据库模板按 exampleValue 渲染为 HTML
	 * @param id 模板ID
	 * @return 渲染后的 HTML
	 */
	String previewRenderedHtml(Long id);

	/**
	 * 离线渲染（用于编辑态实时预览）
	 * @param form 内容与变量声明
	 * @return 渲染后的 HTML
	 */
	String render(EmailTemplateRenderForm form);

	/**
	 * 测试发送（使用 require_fields 中 exampleValue 构建模型）
	 * @param form 模板ID与收件邮箱
	 */
	void testSend(EmailTemplateTestSendForm form);

}
