package com.auth.service.system.file.storage.platform.minio;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.config.properties.S3PlatformProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MinioS3PlatformProfileSource} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("MinioS3PlatformProfileSource MinIO 配置适配")
class MinioS3PlatformProfileSourceTest {

	@Test
	@DisplayName("resolveFallback：映射 minio 配置为 S3 协议 profile")
	void resolveFallbackMapsMinioProperties() {
		// 验证 MinIO legacy 配置正确映射到 S3 协议字段。
		FileUploadProperties properties = new FileUploadProperties();
		properties.getMinio().setEndpoint("http://127.0.0.1:9000");
		properties.getMinio().setAccessKey("ak");
		properties.getMinio().setSecretKey("sk");
		properties.getMinio().setBucket("bucket-a");
		properties.getMinio().setPublicUrl("https://cdn.example.com");
		MinioS3PlatformProfileSource source = new MinioS3PlatformProfileSource();

		S3PlatformProfile profile = source.resolveFallback(properties);

		assertThat(source.platform()).isEqualTo(StoragePlatformEnum.MINIO);
		assertThat(profile.getEndpoint()).isEqualTo("http://127.0.0.1:9000");
		assertThat(profile.getRegion()).isEqualTo("us-east-1");
		assertThat(profile.getAccessKey()).isEqualTo("ak");
		assertThat(profile.getSecretKey()).isEqualTo("sk");
		assertThat(profile.getBucket()).isEqualTo("bucket-a");
		assertThat(profile.getPublicUrl()).isEqualTo("https://cdn.example.com");
		assertThat(profile.isPathStyleAccess()).isTrue();
	}

}
