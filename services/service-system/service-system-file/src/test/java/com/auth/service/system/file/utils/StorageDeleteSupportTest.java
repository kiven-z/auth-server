package com.auth.service.system.file.utils;

import com.aliyun.oss.OSSException;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link StorageDeleteSupport} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("StorageDeleteSupport 删除幂等判定")
class StorageDeleteSupportTest {

	private static final String REQUEST_ID = "test-request-id";

	private static final String HOST_ID = "test-host-id";

	@Test
	@DisplayName("isMinioObjectNotFound：NoSuchBucket 错误码返回 true")
	void isMinioObjectNotFound_returnsTrueForNoSuchBucketCode() {
		// 验证 MinIO 标准 NoSuchBucket 响应被识别为已删除。
		ErrorResponseException exception = new ErrorResponseException(new ErrorResponse("NoSuchBucket",
				"The specified bucket does not exist", "bucket", null, null, REQUEST_ID, HOST_ID), null,
				"HTTP 404 Not Found");

		assertThat(StorageDeleteSupport.isMinioObjectNotFound(exception)).isTrue();
	}

	@Test
	@DisplayName("isMinioObjectNotFound：NoSuchKey 错误码返回 true")
	void isMinioObjectNotFound_returnsTrueForNoSuchKeyCode() {
		// 验证 MinIO 标准 NoSuchKey 响应被识别为已删除。
		ErrorResponseException exception = new ErrorResponseException(new ErrorResponse("NoSuchKey",
				"The specified key does not exist", "bucket", "object", null, REQUEST_ID, HOST_ID), null,
				"HTTP 404 Not Found");

		assertThat(StorageDeleteSupport.isMinioObjectNotFound(exception)).isTrue();
	}

	@Test
	@DisplayName("isMinioObjectNotFound：消息包含 bucket 不存在时返回 true")
	void isMinioObjectNotFound_returnsTrueForBucketMissingMessage() {
		// 验证非结构化异常消息仍可被兜底识别。
		Exception exception = new RuntimeException("MINIO upload failed: The specified bucket does not exist");

		assertThat(StorageDeleteSupport.isMinioObjectNotFound(exception)).isTrue();
	}

	@Test
	@DisplayName("isMinioObjectNotFound：其他异常返回 false")
	void isMinioObjectNotFound_returnsFalseForOtherErrors() {
		// 验证非 not-found 异常不会被误判为幂等成功。
		ErrorResponseException exception = new ErrorResponseException(
				new ErrorResponse("AccessDenied", "Access Denied", "bucket", "object", null, REQUEST_ID, HOST_ID), null,
				"HTTP 403 Forbidden");

		assertThat(StorageDeleteSupport.isMinioObjectNotFound(exception)).isFalse();
	}

	@Test
	@DisplayName("isOssObjectNotFound：NoSuchBucket 错误码返回 true")
	void isOssObjectNotFound_returnsTrueForNoSuchBucketCode() {
		// 验证 OSS 标准 NoSuchBucket 响应被识别为已删除。
		OSSException exception = new OSSException("The specified bucket does not exist", "NoSuchBucket", REQUEST_ID,
				HOST_ID, null, null, null);

		assertThat(StorageDeleteSupport.isOssObjectNotFound(exception)).isTrue();
	}

	@Test
	@DisplayName("isOssObjectNotFound：NoSuchKey 错误码返回 true")
	void isOssObjectNotFound_returnsTrueForNoSuchKeyCode() {
		// 验证 OSS 标准 NoSuchKey 响应被识别为已删除。
		OSSException exception = new OSSException("The specified key does not exist", "NoSuchKey", REQUEST_ID, HOST_ID,
				null, null, null);

		assertThat(StorageDeleteSupport.isOssObjectNotFound(exception)).isTrue();
	}

	@Test
	@DisplayName("isOssObjectNotFound：消息包含 key 不存在时返回 true")
	void isOssObjectNotFound_returnsTrueForKeyMissingMessage() {
		// 验证 OSS 非结构化异常消息仍可被兜底识别。
		Exception exception = new RuntimeException("The specified key does not exist");

		assertThat(StorageDeleteSupport.isOssObjectNotFound(exception)).isTrue();
	}

	@Test
	@DisplayName("isOssObjectNotFound：其他异常返回 false")
	void isOssObjectNotFound_returnsFalseForOtherErrors() {
		// 验证 OSS 非 not-found 异常不会被误判为幂等成功。
		OSSException exception = new OSSException("Access Denied", "AccessDenied", REQUEST_ID, HOST_ID, null, null,
				null);

		assertThat(StorageDeleteSupport.isOssObjectNotFound(exception)).isFalse();
	}

}
