package com.auth.module.file.export.delivery;

import lombok.experimental.UtilityClass;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

/**
 * 同步文件下载交付：将字节流包装为 HTTP 附件响应
 *
 * @author Bunny
 */
@UtilityClass
public class SyncFileDelivery {

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
