package com.auth.service.system.message.channel.email;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.email.EmailSendDTO;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.email.model.RenderedEmail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.auth.service.system.common.exception.code.common.SystemCommonResultCode.SERVICE_UNAVAILABLE;
import static com.auth.service.system.common.exception.code.message.SystemEmailResultCode.*;

/**
 * JavaMail MIME 邮件发送
 *
 * @author Bunny
 */
@Slf4j
@Component
public class MimeMailTransport {

	private final JavaMailSender mailSender;

	private final EmailAttachmentResolver attachmentResolver;

	/**
	 * 默认发件人（spring.mail.username）
	 */
	@Value("${spring.mail.username}")
	private String defaultFrom;

	public MimeMailTransport(JavaMailSender mailSender, EmailAttachmentResolver attachmentResolver) {
		this.mailSender = mailSender;
		this.attachmentResolver = attachmentResolver;
	}

	/**
	 * 同步发送邮件
	 * @param emailSendDTO 邮件发送请求
	 * @param rendered 渲染后的主题与正文
	 */
	public void send(EmailSendDTO emailSendDTO, RenderedEmail rendered) {
		try {
			sendMime(emailSendDTO, rendered.subject(), rendered.body());
		}
		catch (MessagingException e) {
			log.error("Failed to send email, templateCode={}, reason={}", emailSendDTO.getTemplateCode(),
					e.getMessage(), e);
			throw new SystemBusinessException(SERVICE_UNAVAILABLE, e.getMessage());
		}
	}

	/**
	 * 发送 MIME 邮件
	 * @param emailSendDTO 邮件发送请求
	 * @param subject 主题
	 * @param body 正文
	 * @throws MessagingException 邮件发送异常
	 */
	private void sendMime(EmailSendDTO emailSendDTO, String subject, String body) throws MessagingException {
		// 验证邮件发送请求
		validate(emailSendDTO, subject, body);

		// 创建 MIME 消息
		MimeMessage message = mailSender.createMimeMessage();
		// 创建 MIME 消息辅助器
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
		// 设置发件人
		helper.setFrom(defaultFrom);
		// 设置收件人
		helper.setTo(emailSendDTO.getTo().toArray(String[]::new));
		// 设置主题
		helper.setSubject(subject);
		// 设置正文
		helper.setText(body, Boolean.TRUE.equals(emailSendDTO.getHasHtml()));

		// 设置回复地址
		if (CharSequenceUtil.isNotBlank(emailSendDTO.getReplyTo())) {
			helper.setReplyTo(emailSendDTO.getReplyTo());
		}
		// 设置抄送地址
		if (CollUtil.isNotEmpty(emailSendDTO.getCc())) {
			helper.setCc(emailSendDTO.getCc().toArray(String[]::new));
		}
		// 设置密送地址
		if (CollUtil.isNotEmpty(emailSendDTO.getBcc())) {
			helper.setBcc(emailSendDTO.getBcc().toArray(String[]::new));
		}
		helper.setSentDate(new Date());

		// 解析附件
		List<MultipartFile> attachmentFiles = attachmentResolver.resolve(emailSendDTO.getAttachments());
		addAttachments(helper, attachmentFiles);

		mailSender.send(message);
	}

	/**
	 * 验证邮件发送请求
	 * @param emailSendDTO 邮件发送请求
	 * @param subject 主题
	 * @param body 正文
	 */
	private void validate(EmailSendDTO emailSendDTO, String subject, String body) {
		if (emailSendDTO == null) {
			throw new SystemBusinessException(EMAIL_SEND_DTO_NULL);
		}
		if (CollUtil.isEmpty(emailSendDTO.getTo())) {
			throw new SystemBusinessException(EMAIL_TO_EMPTY);
		}
		if (CharSequenceUtil.isBlank(subject)) {
			throw new SystemBusinessException(EMAIL_SUBJECT_EMPTY);
		}
		if (CharSequenceUtil.isBlank(body)) {
			throw new SystemBusinessException(EMAIL_BODY_EMPTY);
		}
	}

	/**
	 * 添加附件
	 * @param helper 邮件辅助器
	 * @param attachments 附件列表
	 * @throws MessagingException 邮件发送异常
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
			helper.addAttachment(Objects.requireNonNull(filename), Objects.requireNonNull(file));
		}
	}

}
