package com.auth.service.system.file.support;

import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.FileUploadProperties;
import com.auth.service.system.file.exception.FileException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link FileUrlSigner} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileUrlSigner 预览地址签名")
class FileUrlSignerTest {

	@Test
	@DisplayName("sign：追加过期时间参数")
	void signAppendsExpireParam() {
		// 验证签名结果会携带 expires 参数，体现 URL 过期策略。
		FileUploadProperties properties = new FileUploadProperties();
		properties.getUrl().setExpireSeconds(600);
		FileUrlSigner signer = new FileUrlSigner(properties);

		String signedUrl = signer.sign("https://cdn.example.com/avatar.png", "image/png", StoragePlatformEnum.MINIO);

		assertThat(signedUrl).contains("expires=");
	}

	@Test
	@DisplayName("sign：不在白名单的文件类型禁止预览")
	void signThrowsWhenContentTypeNotAllowed() {
		// 验证文件类型不符合白名单时会抛出统一业务异常码。
		FileUploadProperties properties = new FileUploadProperties();
		properties.getPreview().setAllowedContentTypes(java.util.List.of("image/*"));
		FileUrlSigner signer = new FileUrlSigner(properties);

		assertThatThrownBy(
				() -> signer.sign("https://cdn.example.com/a.zip", "application/zip", StoragePlatformEnum.MINIO))
			.isInstanceOf(FileException.class)
			.extracting(exception -> ((FileException) exception).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_PREVIEW_CONTENT_TYPE_NOT_ALLOWED);
	}

}
