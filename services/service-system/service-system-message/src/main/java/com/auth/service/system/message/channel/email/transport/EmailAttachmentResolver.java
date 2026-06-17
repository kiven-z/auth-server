package com.auth.service.system.message.channel.email.transport;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.PrimitiveArrayUtil;
import com.auth.module.message.api.model.email.AttachmentDTO;
import com.auth.service.system.common.exception.SystemBusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_FAILED;

/**
 * 将附件声明解析为可随信发送的二进制内容
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
	 * 将附件声明解析为可随信发送的二进制内容
	 * @param attachments 附件声明列表
	 * @return 可随信发送的二进制内容
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
	 * 将附件声明解析为可随信发送的二进制内容
	 * @param attachment 附件声明
	 * @return 可随信发送的二进制内容
	 */
	private byte[] resolveBytes(AttachmentDTO attachment) {
		// 有Base64编码，解析为二进制内容
		if (CharSequenceUtil.isNotBlank(attachment.getBase64())) {
			try {
				return Base64.decode(attachment.getBase64());
			}
			catch (IllegalArgumentException e) {
				throw new SystemBusinessException(OPERATION_FAILED, "Attachment Base64 decode", e.getMessage());
			}
		}

		// 有字节数组，直接返回
		if (attachment.getBytes() != null && attachment.getBytes().length > 0) {
			return attachment.getBytes();
		}

		if (CharSequenceUtil.isBlank(attachment.getUrl())) {
			return new byte[0];
		}

		// 有URL，下载为二进制内容
		try {
			ResponseEntity<byte[]> response = restTemplate.getForEntity(attachment.getUrl(), byte[].class);
			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				return response.getBody();
			}
			throw new SystemBusinessException(OPERATION_FAILED, "Attachment download",
					"HTTP " + response.getStatusCode().value());
		}
		catch (SystemBusinessException e) {
			throw e;
		}
		catch (Exception e) {
			log.error("Failed to download attachment from URL", e);
			throw new SystemBusinessException(OPERATION_FAILED, "Attachment download", e.getMessage());
		}
	}

}
