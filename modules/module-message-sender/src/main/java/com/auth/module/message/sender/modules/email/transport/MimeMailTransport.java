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
 * JavaMail MIME email transport.
 *
 * @author Bunny
 */
@Slf4j
public class MimeMailTransport {

	private final JavaMailSender mailSender;

	private final EmailAttachmentResolver attachmentResolver;

	/**
	 * Default from address ({@code spring.mail.username}).
	 */
	@Value("${spring.mail.username}")
	private String defaultFrom;

	public MimeMailTransport(JavaMailSender mailSender, EmailAttachmentResolver attachmentResolver) {
		this.mailSender = mailSender;
		this.attachmentResolver = attachmentResolver;
	}

	/**
	 * Sends email synchronously.
	 * @param emailSendDTO send request
	 * @param rendered rendered subject and body
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

	private void sendMime(EmailSendDTO emailSendDTO, String subject, String body) throws MessagingException {
		validate(emailSendDTO, subject, body);

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
		helper.setFrom(defaultFrom);
		helper.setTo(emailSendDTO.getTo().toArray(String[]::new));
		helper.setSubject(subject);
		helper.setText(body, Boolean.TRUE.equals(emailSendDTO.getHasHtml()));

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

	private void validate(EmailSendDTO emailSendDTO, String subject, String body) {
		if (emailSendDTO == null) {
			throw new EmailTransportException(EmailTransportException.Reason.SEND_DTO_NULL, "EmailSendDTO is null");
		}
		if (CollUtil.isEmpty(emailSendDTO.getTo())) {
			throw new EmailTransportException(EmailTransportException.Reason.TO_EMPTY, "Recipient list is empty");
		}
		if (CharSequenceUtil.isBlank(subject)) {
			throw new EmailTransportException(EmailTransportException.Reason.SUBJECT_EMPTY, "Subject is blank");
		}
		if (CharSequenceUtil.isBlank(body)) {
			throw new EmailTransportException(EmailTransportException.Reason.BODY_EMPTY, "Body is blank");
		}
	}

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
				throw new EmailTransportException(EmailTransportException.Reason.ATTACHMENT_FILENAME_EMPTY,
						"Attachment filename is blank");
			}
			helper.addAttachment(Objects.requireNonNull(filename), Objects.requireNonNull(file));
		}
	}

}
