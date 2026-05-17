package com.auth.service.system.message.email;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.PrimitiveArrayUtil;
import com.auth.module.message.email.AttachmentDTO;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.support.InMemoryMultipartFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.auth.service.system.common.exception.SystemResultCodeEnum.*;

/**
 * 邮件附件解析：Base64 / bytes / URL → {@link MultipartFile}
 *
 * @author Bunny
 */
@Slf4j
@Component
public class EmailAttachmentResolver {

	private final RestTemplate restTemplate;

	public EmailAttachmentResolver(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	/**
	 * 将附件 DTO 列表转换为 MultipartFile 列表
	 * @param attachments 附件 DTO 列表
	 * @return MultipartFile 列表
	 */
	public List<MultipartFile> resolve(List<AttachmentDTO> attachments) {
		if (CollUtil.isEmpty(attachments)) {
			return Collections.emptyList();
		}

		// 创建附件列表
		List<MultipartFile> files = new ArrayList<>(attachments.size());
		for (AttachmentDTO attachment : attachments) {
			if (attachment != null && CharSequenceUtil.isNotBlank(attachment.getFilename())) {
				String filename = attachment.getFilename();
				String contentType = attachment.getContentType();
				byte[] content = resolveBytes(attachment);
				if (PrimitiveArrayUtil.isNotEmpty(content)) {
					files.add(new InMemoryMultipartFile(filename, filename, contentType, content));
				}
			}
		}
		return files;
	}

	/**
	 * 按优先级解析附件二进制：base64 → bytes → url
	 * @param attachment 附件 DTO
	 * @return 附件二进制
	 */
	private byte[] resolveBytes(AttachmentDTO attachment) {
		// 解析 base64 附件
		if (CharSequenceUtil.isNotBlank(attachment.getBase64())) {
			try {
				return Base64.decode(attachment.getBase64());
			}
			catch (IllegalArgumentException e) {
				throw new SystemBusinessException(EMAIL_ATTACHMENT_BASE64_FAILED, e.getMessage());
			}
		}
		// 解析 bytes 附件
		if (attachment.getBytes() != null && attachment.getBytes().length > 0) {
			return attachment.getBytes();
		}
		// 解析 url 附件
		if (CharSequenceUtil.isNotBlank(attachment.getUrl())) {
			try {
				// 下载附件
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
				log.error("Failed to download attachment from URL", e);
				throw new SystemBusinessException(EMAIL_ATTACHMENT_DOWNLOAD_FAILED, e.getMessage());
			}
		}

		return new byte[0];
	}

}
