package com.auth.module.message.sender.modules.email.transport;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.sender.modules.email.EmailSendDTO;
import com.auth.module.message.sender.modules.email.RenderedEmail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * JavaMail MIME 邮件传输
 *
 * @author Bunny
 */
@Slf4j
public class MimeMailTransport {

	private final JavaMailSender mailSender;

	private final EmailAttachmentResolver attachmentResolver;

	/**
	 * 默认发件人地址 ({@code spring.mail.username})
	 */
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
			throw new EmailTransportException(EmailTransportException.Reason.SEND_FAILED, e.getMessage(), e);
		}
	}

	/**
	 * 发送MIME邮件
	 * @param emailSendDTO 发送请求
	 * @param subject 主题
	 * @param body 内容
	 * @throws MessagingException 邮件发送异常
	 */
	private void sendMime(EmailSendDTO emailSendDTO, String subject, String body) throws MessagingException {
		// 验证发送请求
		validate(emailSendDTO, subject, body);

		// 创建MIME消息
		MimeMessage message = mailSender.createMimeMessage();
		// 创建MIME消息助手
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
		// 设置发件人
		helper.setFrom(defaultFrom);
		// 设置收件人
		helper.setTo(emailSendDTO.getTo().toArray(String[]::new));
		// 设置主题
		helper.setSubject(subject);
		// 设置内容
		helper.setText(body, Boolean.TRUE.equals(emailSendDTO.getHasHtml()));

		if (CharSequenceUtil.isNotBlank(emailSendDTO.getReplyTo())) {
			// 设置回复地址
			helper.setReplyTo(emailSendDTO.getReplyTo());
		}
		if (CollUtil.isNotEmpty(emailSendDTO.getCc())) {
			// 设置抄送地址
			helper.setCc(emailSendDTO.getCc().toArray(String[]::new));
		}
		if (CollUtil.isNotEmpty(emailSendDTO.getBcc())) {
			// 设置密送地址
			helper.setBcc(emailSendDTO.getBcc().toArray(String[]::new));
		}
		// 设置发送日期
		helper.setSentDate(new Date());

		// 解析附件
		List<MultipartFile> attachmentFiles = attachmentResolver.resolve(emailSendDTO.getAttachments());
		// 添加附件
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
		// 验证发送请求
		if (emailSendDTO == null) {
			throw new EmailTransportException(EmailTransportException.Reason.SEND_DTO_NULL);
		}
		// 验证接收者列表
		if (CollUtil.isEmpty(emailSendDTO.getTo())) {
			throw new EmailTransportException(EmailTransportException.Reason.TO_EMPTY);
		}
		// 验证主题
		if (CharSequenceUtil.isBlank(subject)) {
			throw new EmailTransportException(EmailTransportException.Reason.SUBJECT_EMPTY);
		}
		// 验证内容
		if (CharSequenceUtil.isBlank(body)) {
			throw new EmailTransportException(EmailTransportException.Reason.BODY_EMPTY);
		}
	}

	/**
	 * 添加附件
	 * @param helper MIME消息助手
	 * @param attachments 附件列表
	 * @throws MessagingException 邮件发送异常
	 */
	private void addAttachments(MimeMessageHelper helper, List<MultipartFile> attachments) throws MessagingException {
		// 验证附件列表
		if (CollUtil.isEmpty(attachments)) {
			return;
		}
		// 添加附件
		for (MultipartFile file : attachments) {
			if (file == null || file.isEmpty()) {
				continue;
			}

			// 验证附件文件名
			String filename = file.getOriginalFilename();
			if (CharSequenceUtil.isBlank(filename)) {
				throw new EmailTransportException(EmailTransportException.Reason.ATTACHMENT_FILENAME_EMPTY);
			}
			helper.addAttachment(Objects.requireNonNull(filename), Objects.requireNonNull(file));
		}
	}

}
