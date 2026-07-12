package com.auth.service.system.message.service;

import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.form.*;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 邮件模板服务
 *
 * @author Bunny
 */
public interface SysEmailTemplateService extends IService<MessageTemplateEntity> {

	/**
	 * 添加邮件模板
	 * @param form 邮件模板表单
	 */
	void create(EmailTemplateForm form);

	/**
	 * 更新邮件模板
	 * @param form 邮件模板表单
	 */
	void update(EmailTemplateForm form);

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
