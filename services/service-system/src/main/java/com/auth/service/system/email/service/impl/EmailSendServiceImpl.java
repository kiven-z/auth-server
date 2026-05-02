package com.auth.service.system.email.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.PrimitiveArrayUtil;
import com.auth.module.email.model.AttachmentDTO;
import com.auth.module.email.model.EmailSendDTO;
import com.auth.module.email.model.InMemoryMultipartFile;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.email.mapper.EmailTemplateMapper;
import com.auth.service.system.email.model.EmailTemplateEntity;
import com.auth.service.system.email.model.EmailTemplateRequireFieldRow;
import com.auth.service.system.email.service.EmailSendService;
import com.auth.service.system.email.support.EmailTemplateRequireFieldsJsonSupport;
import com.auth.service.system.email.utils.MailFreeMarkUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static com.auth.service.system.common.exception.SystemResultCodeEnum.EMAIL_ATTACHMENT_BASE64_FAILED;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.EMAIL_ATTACHMENT_DOWNLOAD_FAILED;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.EMAIL_ATTACHMENT_FILENAME_EMPTY;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.EMAIL_ATTACHMENT_HTTP_STATUS;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.EMAIL_BODY_EMPTY;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.EMAIL_BODY_RENDER_FAILED;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.EMAIL_SEND_DTO_NULL;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.EMAIL_SUBJECT_EMPTY;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.EMAIL_SUBJECT_RENDER_FAILED;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.EMAIL_TEMPLATE_NOT_FOUND;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.EMAIL_TEMPLATE_VARS_MISSING;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.EMAIL_TO_EMPTY;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.SERVICE_UNAVAILABLE;

/**
 * 通用邮件发送服务实现
 *
 * @author Bunny
 */
@Slf4j
@Service
public class EmailSendServiceImpl implements EmailSendService {

	private final EmailTemplateMapper emailTemplateMapper;

	/**
	 * JavaMail 发送器
	 */
	private final JavaMailSender mailSender;

	/**
	 * 附件 URL 下载使用的 RestTemplate
	 */
	private final RestTemplate restTemplate;

	/**
	 * 默认发件人（spring.mail.username）
	 */
	@Value("${spring.mail.username}")
	private String defaultFrom;

	public EmailSendServiceImpl(EmailTemplateMapper emailTemplateMapper, JavaMailSender mailSender,
			RestTemplate restTemplate) {
		this.emailTemplateMapper = emailTemplateMapper;
		this.mailSender = mailSender;
		this.restTemplate = restTemplate;
	}

	@Override
	public void sendByTemplate(EmailSendDTO dto) {
		// 1. 查询模板（仅根据 templateCode 查询一次，状态和优先级逻辑在 Mapper 中维护）
		String templateCode = dto.getTemplateCode();
		EmailTemplateEntity template = emailTemplateMapper.selectTemplateByTemplateCode(templateCode);
		if (template == null) {
			throw new SystemBusinessException(EMAIL_TEMPLATE_NOT_FOUND, templateCode);
		}

		// 2. 校验必填变量
		Map<String, Object> variables = Optional.ofNullable(dto.getVariables()).orElseGet(HashMap::new);
		validateRequiredVariables(template, variables);

		// 3. 渲染 subject 与 content（FreeMarker）
		String subject = renderSubject(template, variables);
		String html = renderContent(template, variables);

		// 4. 发送邮件（MimeMessageHelper 组装并发送）
		try {
			sendEmailSync(dto, subject, html);
		}
		catch (MessagingException e) {
			log.error("发送邮件失败，templateCode={}，原因：{}", templateCode, e.getMessage(), e);
			throw new SystemBusinessException(SERVICE_UNAVAILABLE, e.getMessage());
		}
	}

	/**
	 * 同步发送邮件（用于需要向调用方反馈发送结果的场景）
	 * @param emailSendDTO 邮件发送请求对象
	 * @param subject 渲染后的邮件主题
	 * @param html 渲染后的邮件正文
	 * @throws MessagingException 邮件发送过程中的异常
	 */
	private void sendEmailSync(EmailSendDTO emailSendDTO, String subject, String html) throws MessagingException {
		validateEmailSendDTO(emailSendDTO, subject, html);

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

		// 设置基础信息：发件人、收件人、主题、内容、抄送、密送、回复地址
		helper.setFrom(defaultFrom);
		helper.setTo(emailSendDTO.getTo().toArray(String[]::new));
		helper.setSubject(subject);
		helper.setText(html, Boolean.TRUE.equals(emailSendDTO.getHasHtml()));

		if (CharSequenceUtil.isNotBlank(emailSendDTO.getReplyTo())) {
			helper.setReplyTo(emailSendDTO.getReplyTo());
		}
		if (CollUtil.isNotEmpty(emailSendDTO.getCc())) {
			helper.setCc(emailSendDTO.getCc().toArray(String[]::new));
		}
		if (CollUtil.isNotEmpty(emailSendDTO.getBcc())) {
			helper.setBcc(emailSendDTO.getBcc().toArray(String[]::new));
		}

		// 发送时间：使用当前时间
		helper.setSentDate(new Date());

		// 处理附件：Base64 / bytes / URL -> MultipartFile
		List<MultipartFile> attachmentFiles = buildAttachmentFiles(emailSendDTO.getAttachments());
		addAttachments(helper, attachmentFiles);

		mailSender.send(message);
	}

	/**
	 * 校验邮件发送请求是否合法
	 * @param emailSendDTO 邮件发送请求对象
	 * @param subject 渲染后的邮件主题
	 * @param html 渲染后的邮件正文
	 */
	private void validateEmailSendDTO(EmailSendDTO emailSendDTO, String subject, String html) {
		if (emailSendDTO == null) {
			throw new SystemBusinessException(EMAIL_SEND_DTO_NULL);
		}
		if (CollUtil.isEmpty(emailSendDTO.getTo())) {
			throw new SystemBusinessException(EMAIL_TO_EMPTY);
		}
		if (CharSequenceUtil.isBlank(subject)) {
			throw new SystemBusinessException(EMAIL_SUBJECT_EMPTY);
		}
		if (CharSequenceUtil.isBlank(html)) {
			throw new SystemBusinessException(EMAIL_BODY_EMPTY);
		}
	}

	/**
	 * 添加附件
	 * @param helper MimeMessageHelper
	 * @param attachments 附件列表
	 * @throws MessagingException 添加附件过程中可能抛出的异常
	 */
	private void addAttachments(MimeMessageHelper helper, List<MultipartFile> attachments) throws MessagingException {
		if (CollUtil.isEmpty(attachments)) {
			return;
		}
		for (MultipartFile file : attachments) {
			if (file == null || file.isEmpty()) {
				continue;
			}
			String filename = file.getOriginalFilename();
			if (CharSequenceUtil.isBlank(filename)) {
				throw new SystemBusinessException(EMAIL_ATTACHMENT_FILENAME_EMPTY);
			}
			helper.addAttachment(filename, file);
		}
	}

	/**
	 * 校验必填变量：sys_email_template.require_fields 中出现的 key 都必须在 variables 中存在
	 * @param template 邮件模板实体
	 * @param variables 调用方传入的变量
	 */
	private void validateRequiredVariables(EmailTemplateEntity template, Map<String, Object> variables) {
		List<EmailTemplateRequireFieldRow> requireFields = EmailTemplateRequireFieldsJsonSupport
			.parse(template.getRequireFields());

		if (requireFields.isEmpty()) {
			return;
		}

		List<String> missingKeys = requireFields.stream()
			.map(EmailTemplateRequireFieldRow::getKey)
			.filter(key -> !variables.containsKey(key))
			.toList();

		if (!missingKeys.isEmpty()) {
			throw new SystemBusinessException(EMAIL_TEMPLATE_VARS_MISSING, String.join(", ", missingKeys));
		}
	}

	/**
	 * 使用 FreeMarker 渲染邮件主题
	 * @param template 邮件模板
	 * @param variables 模板变量
	 * @return 渲染后的主题
	 */
	private String renderSubject(EmailTemplateEntity template, Map<String, Object> variables) {
		try {
			return MailFreeMarkUtil.processTemplate(template.getTemplateName() + "_subject", template.getSubject(),
					variables);
		}
		catch (Exception e) {
			throw new SystemBusinessException(EMAIL_SUBJECT_RENDER_FAILED, e.getMessage());
		}
	}

	/**
	 * 使用 FreeMarker 渲染邮件正文 HTML
	 * @param template 邮件模板
	 * @param variables 模板变量
	 * @return 渲染后的 HTML 正文
	 */
	private String renderContent(EmailTemplateEntity template, Map<String, Object> variables) {
		try {
			return MailFreeMarkUtil.processTemplate(template.getTemplateName(), template.getContent(), variables);
		}
		catch (Exception e) {
			throw new SystemBusinessException(EMAIL_BODY_RENDER_FAILED, e.getMessage());
		}
	}

	/**
	 * 将 JSON 传入的附件列表转换为 Spring MultipartFile 列表
	 * @param attachments 附件 DTO 列表
	 * @return MultipartFile 列表
	 */
	private List<MultipartFile> buildAttachmentFiles(List<AttachmentDTO> attachments) {
		if (CollUtil.isEmpty(attachments)) {
			return Collections.emptyList();
		}

		List<MultipartFile> files = new ArrayList<>(attachments.size());
		for (AttachmentDTO attachment : attachments) {
			if (attachment == null || CharSequenceUtil.isBlank(attachment.getFilename())) {
				continue;
			}

			byte[] content = resolveAttachmentBytes(attachment);
			if (PrimitiveArrayUtil.isEmpty(content)) {
				continue;
			}

			String filename = attachment.getFilename();
			String contentType = attachment.getContentType();
			InMemoryMultipartFile file = new InMemoryMultipartFile(filename, filename, contentType, content);
			files.add(file);
		}
		return files;
	}

	/**
	 * 按优先级解析附件内容：base64 -> bytes -> url
	 * @param attachment 附件 DTO
	 * @return 文件二进制内容
	 */
	private byte[] resolveAttachmentBytes(AttachmentDTO attachment) {
		// 1. Base64
		if (CharSequenceUtil.isNotBlank(attachment.getBase64())) {
			try {
				return Base64.decode(attachment.getBase64());
			}
			catch (IllegalArgumentException e) {
				throw new SystemBusinessException(EMAIL_ATTACHMENT_BASE64_FAILED, e.getMessage());
			}
		}

		// 2. 直接字节数组
		if (attachment.getBytes() != null && attachment.getBytes().length > 0) {
			return attachment.getBytes();
		}

		// 3. URL 下载
		if (CharSequenceUtil.isNotBlank(attachment.getUrl())) {
			try {
				ResponseEntity<byte[]> response = restTemplate.getForEntity(attachment.getUrl(), byte[].class);
				if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
					return response.getBody();
				}
				throw new SystemBusinessException(EMAIL_ATTACHMENT_HTTP_STATUS, response.getStatusCode().value());
			}
			catch (SystemBusinessException e) {
				throw e;
			}
			catch (Exception e) {
				log.error("从 URL 下载附件失败", e);
				throw new SystemBusinessException(EMAIL_ATTACHMENT_DOWNLOAD_FAILED, e.getMessage());
			}
		}

		// 4. 全部为空
		return new byte[0];
	}

}
