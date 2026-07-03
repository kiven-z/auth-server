package com.auth.module.file.export.delivery;

import java.nio.charset.StandardCharsets;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import lombok.experimental.UtilityClass;

/**
 * 文件下载交付：将字节流包装为 HTTP 附件响应
 *
 * @author Bunny
 */
@UtilityClass
public class FileDelivery {

	/**
	 * 构建流式附件下载响应
	 * @param body 流式响应体
	 * @param filename 下载文件名
	 * @param contentType MIME 类型
	 * @return HTTP 响应
	 */
	public static ResponseEntity<StreamingResponseBody> deliver(StreamingResponseBody body, String filename,
			String contentType) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType(contentType));
		headers
			.setContentDisposition(ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
		return ResponseEntity.ok().headers(headers).body(body);
	}

	/**
	 * 构建附件下载响应
	 * @param bytes 文件字节
	 * @param filename 下载文件名
	 * @return HTTP 响应
	 */
	public static ResponseEntity<byte[]> deliver(byte[] bytes, String filename) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers
			.setContentDisposition(ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
		return ResponseEntity.ok().headers(headers).body(bytes);
	}

}
