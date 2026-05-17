package com.auth.service.system.message.service;

import com.auth.module.message.email.EmailSendDTO;

/**
 * 通用邮件发送服务
 * <p>
 * 基于 sys_email_template + FreeMarker 实现
 * </p>
 *
 * @author Bunny
 */
public interface EmailSendService {

	/**
	 * 按模板发送邮件
	 * <p>
	 * 1. 根据 templateCode 查询模板； 2. 校验 require_fields 与 variables； 3. 使用 FreeMarker 渲染
	 * subject 与 content； 4. 构建 MimeMessage 并发送（支持附件）
	 * </p>
	 * @param dto 邮件发送请求
	 */
	void sendByTemplate(EmailSendDTO dto);

}
