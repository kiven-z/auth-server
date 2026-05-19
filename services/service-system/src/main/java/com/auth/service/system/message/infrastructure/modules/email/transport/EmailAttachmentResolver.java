package com.auth.service.system.message.infrastructure.modules.email.transport;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.PrimitiveArrayUtil;
import com.auth.module.message.api.model.email.AttachmentDTO;
import com.auth.service.system.message.infrastructure.exception.SystemMessageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.auth.service.system.message.infrastructure.exception.SystemEmailResultCode.*;

/**
 * 附件解析器，将附件DTO转换为MultipartFile（Base64 / bytes / URL）
 *
 * @author Bunny
 */
@Slf4j
public class EmailAttachmentResolver {

	private final RestTemplate restTemplate;

	public EmailAttachmentResolver(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	/**
	 * 解析附件DTO列表为MultipartFile列表
	 * @param attachments 附件DTO列表
	 * @return MultipartFile列表
	 */
	public List<MultipartFile> resolve(List<AttachmentDTO> attachments) {
		if (CollUtil.isEmpty(attachments)) {
			return Collections.emptyList();
		}

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
	 * 解析附件字节数组
	 * @param attachment 附件DTO
	 * @return 字节数组
	 */
	private byte[] resolveBytes(AttachmentDTO attachment) {
		if (CharSequenceUtil.isNotBlank(attachment.getBase64())) {
			try {
				return Base64.decode(attachment.getBase64());
			}
			catch (IllegalArgumentException e) {
				throw new SystemMessageException(EMAIL_ATTACHMENT_BASE64_FAILED, e.getMessage());
			}
		}
		if (attachment.getBytes() != null && attachment.getBytes().length > 0) {
			return attachment.getBytes();
		}
		if (CharSequenceUtil.isNotBlank(attachment.getUrl())) {
			try {
				ResponseEntity<byte[]> response = restTemplate.getForEntity(attachment.getUrl(), byte[].class);
				if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
					return response.getBody();
				}
				throw new SystemMessageException(EMAIL_ATTACHMENT_HTTP_STATUS, response.getStatusCode().value());
			}
			catch (SystemMessageException e) {
				throw e;
			}
			catch (Exception e) {
				log.error("Failed to download attachment from URL", e);
				throw new SystemMessageException(EMAIL_ATTACHMENT_DOWNLOAD_FAILED, e.getMessage());
			}
		}
		return new byte[0];
	}

}
