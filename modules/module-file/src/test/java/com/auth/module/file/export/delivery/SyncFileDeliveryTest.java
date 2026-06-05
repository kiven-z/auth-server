package com.auth.module.file.export.delivery;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SyncFileDelivery} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SyncFileDelivery 同步文件下载交付")
class SyncFileDeliveryTest {

	@Test
	@DisplayName("中文文件名应输出 RFC 5987 的 filename* 头")
	void deliver_whenFilenameContainsNonAscii_setsUtf8FilenameStar() {
		// 验证前端 resolveFilenameFromContentDisposition 可解析的响应头
		byte[] bytes = new byte[] { 1, 2, 3 };
		String filename = "岗位_20250101120000.xlsx";

		ResponseEntity<byte[]> response = SyncFileDelivery.deliver(bytes, filename);

		assertThat(response.getBody()).isEqualTo(bytes);
		ContentDisposition disposition = response.getHeaders().getContentDisposition();
		assertThat(disposition.getFilename()).isEqualTo(filename);
		assertThat(disposition.toString()).contains("filename*=UTF-8''");
	}

	@Test
	@DisplayName("纯 ASCII 文件名可正常交付")
	void deliver_whenFilenameIsAscii_setsAttachmentHeaders() {
		byte[] bytes = "excel".getBytes(StandardCharsets.UTF_8);
		String filename = "role_20250101120000.xlsx";

		ResponseEntity<byte[]> response = SyncFileDelivery.deliver(bytes, filename);

		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/octet-stream");
		assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo(filename);
	}

}
