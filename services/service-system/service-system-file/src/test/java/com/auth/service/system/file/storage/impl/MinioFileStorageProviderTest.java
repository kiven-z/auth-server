package com.auth.service.system.file.storage.impl;

import com.auth.service.system.file.config.FileUploadProperties;
import com.auth.service.system.file.exception.FileException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link MinioFileStorageProvider} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("MinioFileStorageProvider 存储删除")
@ExtendWith(MockitoExtension.class)
class MinioFileStorageProviderTest {

	private static final String BUCKET = "test-bucket";

	private static final String OBJECT_KEY = "path/to/object.png";

	@Mock
	private MinioClient minioClient;

	private MinioFileStorageProvider provider;

	@BeforeEach
	void setUp() throws Exception {
		FileUploadProperties properties = new FileUploadProperties();
		FileUploadProperties.MinioConfig minioConfig = properties.getMinio();
		minioConfig.setEndpoint("http://127.0.0.1:9000");
		minioConfig.setAccessKey("access-key");
		minioConfig.setSecretKey("secret-key");
		minioConfig.setBucket(BUCKET);
		provider = new MinioFileStorageProvider(properties);
		injectMinioClient(minioClient);
	}

	@Test
	@DisplayName("delete：bucket 不存在时不抛异常")
	void delete_succeedsWhenBucketNotFound() throws Exception {
		// 验证存储端 bucket 已不存在时，删除按幂等成功处理。
		ErrorResponseException notFound = new ErrorResponseException(new ErrorResponse("NoSuchBucket",
				"The specified bucket does not exist", BUCKET, null, null, "request-id", "host-id"), null,
				"HTTP 404 Not Found");
		doThrow(notFound).when(minioClient).removeObject(any());

		assertThatCode(() -> provider.delete(BUCKET, OBJECT_KEY)).doesNotThrowAnyException();
		verify(minioClient).removeObject(any());
	}

	@Test
	@DisplayName("presignGetUrl：调用 MinIO SDK 返回预签名地址")
	void presignGetUrl_returnsSignedUrlFromSdk() throws Exception {
		// 验证读取预签名地址会直接委派给 MinIO SDK。
		when(minioClient.getPresignedObjectUrl(any()))
			.thenReturn("http://127.0.0.1:9000/test-bucket/path/to/object.png?X-Amz-Signature=abc");

		String signedUrl = provider.presignGetUrl(BUCKET, OBJECT_KEY, 300);

		assertThat(signedUrl).contains("X-Amz-Signature=abc");
		verify(minioClient).getPresignedObjectUrl(any());
	}

	@Test
	@DisplayName("delete：其他错误仍抛出 FileException")
	void delete_throwsWhenOtherErrorOccurs() throws Exception {
		// 验证非 not-found 错误仍会阻断 DB 侧 purge。
		ErrorResponseException accessDenied = new ErrorResponseException(
				new ErrorResponse("AccessDenied", "Access Denied", BUCKET, OBJECT_KEY, null, "request-id", "host-id"),
				null, "HTTP 403 Forbidden");
		doThrow(accessDenied).when(minioClient).removeObject(any());

		assertThatThrownBy(() -> provider.delete(BUCKET, OBJECT_KEY)).isInstanceOf(FileException.class)
			.extracting(ex -> ((FileException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_UPLOAD_FAILED);
	}

	/**
	 * 向 provider 注入 mock MinIO 客户端，避免测试依赖真实 MinIO 实例。
	 * @param client mock 客户端
	 */
	private void injectMinioClient(MinioClient client) throws Exception {
		Field field = MinioFileStorageProvider.class.getDeclaredField("minioClientRef");
		field.setAccessible(true);
		@SuppressWarnings("unchecked")
		AtomicReference<MinioClient> clientRef = (AtomicReference<MinioClient>) field.get(provider);
		clientRef.set(client);
	}

}
