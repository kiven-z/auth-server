package com.auth.service.system.message.channel.email.transport;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_FAILED;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.email.EmailSendDTO;
import com.auth.service.system.message.channel.email.RenderedEmail;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * JavaMail MIME 邮件传输
 *
 * @author Bunny
 */
@Slf4j
@Component
public class MimeMailTransport {

	private final JavaMailSender mailSender;

	private final EmailAttachmentResolver attachmentResolver;

	@Value("${spring.mail.username}")
	private String defaultFrom;

	public MimeMailTransport(JavaMailSender mailSender, EmailAttachmentResolver attachmentResolver) {
		this.mailSender = mailSender;
		this.attachmentResolver = attachmentResolver;
	}

	/**
	 * 同步发送邮件
	 * @param emailSendDTO 发送请求
	 * @param rendered 渲染后的主题和内容
	 */
	public void send(EmailSendDTO emailSendDTO, RenderedEmail rendered) {
		try {
			sendMime(emailSendDTO, rendered.subject(), rendered.body());
		}
		catch (MessagingException e) {
			log.error("Failed to send email, templateCode={}, reason={}", emailSendDTO.getTemplateCode(),
					e.getMessage(), e);
			throw new SystemBusinessException(OPERATION_FAILED, "Email send", e.getMessage());
		}
	}

	/**
	 * 发送 MIME 邮件
	 * @param emailSendDTO 发送请求
	 * @param subject 主题
	 * @param body 内容
	 * @throws MessagingException 邮件发送异常
	 */
	private void sendMime(EmailSendDTO emailSendDTO, String subject, String body) throws MessagingException {
		validate(emailSendDTO, subject, body);

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
		helper.setFrom(defaultFrom);
		helper.setTo(emailSendDTO.getTo().toArray(String[]::new));
		helper.setSubject(subject);
		helper.setText(body, BooleanUtil.isTrue(emailSendDTO.getHasHtml()));

		if (CharSequenceUtil.isNotBlank(emailSendDTO.getReplyTo())) {
			helper.setReplyTo(emailSendDTO.getReplyTo());
		}
		if (CollUtil.isNotEmpty(emailSendDTO.getCc())) {
			helper.setCc(emailSendDTO.getCc().toArray(String[]::new));
		}
		if (CollUtil.isNotEmpty(emailSendDTO.getBcc())) {
			helper.setBcc(emailSendDTO.getBcc().toArray(String[]::new));
		}
		helper.setSentDate(new Date());

		List<MultipartFile> attachmentFiles = attachmentResolver.resolve(emailSendDTO.getAttachments());
		addAttachments(helper, attachmentFiles);

		mailSender.send(message);
	}

	/**
	 * 验证发送请求
	 * @param emailSendDTO 发送请求
	 * @param subject 主题
	 * @param body 内容
	 */
	private void validate(EmailSendDTO emailSendDTO, String subject, String body) {
		if (emailSendDTO == null) {
			throw new SystemBusinessException(PARAM_REQUIRED, "Email request");
		}
		if (CollUtil.isEmpty(emailSendDTO.getTo())) {
			throw new SystemBusinessException(PARAM_REQUIRED, "Recipients");
		}
		if (CharSequenceUtil.isBlank(subject)) {
			throw new SystemBusinessException(PARAM_REQUIRED, "Email subject");
		}
		if (CharSequenceUtil.isBlank(body)) {
			throw new SystemBusinessException(PARAM_REQUIRED, "Email body");
		}
	}

	/**
	 * 添加附件
	 * @param helper 邮件助手
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
				throw new SystemBusinessException(PARAM_REQUIRED, "Attachment filename");
			}
			helper.addAttachment(Objects.requireNonNull(filename), Objects.requireNonNull(file));
		}
	}

}
