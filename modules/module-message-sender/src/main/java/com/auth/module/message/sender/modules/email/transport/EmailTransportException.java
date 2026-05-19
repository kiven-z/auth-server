package com.auth.module.message.sender.modules.email.transport;

import lombok.Getter;

/**
 * Email 传输层失败（验证、附件解析、MIME发送）
 *
 * @author Bunny
 */
@Getter
public class EmailTransportException extends RuntimeException {

	private final Reason reason;

	public EmailTransportException(Reason reason) {
		super(reason.defaultEnglishMessage());
		this.reason = reason;
	}

	public EmailTransportException(Reason reason, String message) {
		super(message);
		this.reason = reason;
	}

	public EmailTransportException(Reason reason, String message, Throwable cause) {
		super(message, cause);
		this.reason = reason;
	}

	/**
	 * Transport failure reason.
	 */
	public enum Reason {

		/**
		 * 发送邮件DTO为空
		 */
		SEND_DTO_NULL("EmailSendDTO is null"),
		/**
		 * 接收者列表为空
		 */
		TO_EMPTY("Recipient list is empty"),
		/**
		 * 主题为空
		 */
		SUBJECT_EMPTY("Subject is blank"),
		/**
		 * 内容为空
		 */
		BODY_EMPTY("Body is blank"),
		/**
		 * 附件文件名为空
		 */
		ATTACHMENT_FILENAME_EMPTY("Attachment filename is blank"),
		/**
		 * 附件Base64解码失败
		 */
		ATTACHMENT_BASE64_FAILED("Attachment Base64 decode failed"),
		/**
		 * 附件URL返回非2xx HTTP状态
		 */
		ATTACHMENT_HTTP_STATUS("Failed to download attachment from URL, HTTP status: {0}"),
		/**
		 * 附件URL下载失败
		 */
		ATTACHMENT_DOWNLOAD_FAILED("Failed to download attachment from URL"),
		/**
		 * MIME邮件发送失败
		 */
		SEND_FAILED("Email send failed");

		private final String englishTemplate;

		Reason(String englishTemplate) {
			this.englishTemplate = englishTemplate;
		}

		/**
		 * 默认英文消息，不包含占位符
		 * @return 英文消息
		 */
		public String defaultEnglishMessage() {
			return englishTemplate;
		}

		/**
		 * 格式化英文消息，包含占位符 ({0}, {1}, ...)
		 * @param args 格式化参数
		 * @return 格式化后的英文消息
		 */
		public String formatEnglishMessage(Object... args) {
			if (args == null || args.length == 0) {
				return englishTemplate;
			}
			String message = englishTemplate;
			for (int i = 0; i < args.length; i++) {
				message = message.replace("{" + i + "}", String.valueOf(args[i]));
			}
			return message;
		}

		/**
		 * 是否映射到HTTP 400 (客户端/验证)
		 * @return 是否与验证相关
		 */
		public boolean isValidationFailure() {
			return this == SEND_DTO_NULL || this == TO_EMPTY || this == SUBJECT_EMPTY || this == BODY_EMPTY
					|| this == ATTACHMENT_FILENAME_EMPTY || this == ATTACHMENT_BASE64_FAILED;
		}

	}

}
