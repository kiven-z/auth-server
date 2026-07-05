package com.auth.service.system.file.utils;

import cn.hutool.core.text.CharSequenceUtil;
import com.aliyun.oss.OSSException;
import io.minio.errors.ErrorResponseException;
import lombok.experimental.UtilityClass;

/**
 * 对象存储删除幂等判定工具。
 *
 * @author Bunny
 */
@UtilityClass
public class StorageDeleteSupport {

	/**
	 * MinIO / S3 兼容：存储桶不存在。
	 */
	private static final String MINIO_ERROR_CODE_NO_SUCH_BUCKET = "NoSuchBucket";

	/**
	 * MinIO / S3 兼容：对象不存在。
	 */
	private static final String MINIO_ERROR_CODE_NO_SUCH_KEY = "NoSuchKey";

	/**
	 * OSS：存储桶不存在。
	 */
	private static final String OSS_ERROR_CODE_NO_SUCH_BUCKET = "NoSuchBucket";

	/**
	 * OSS：对象不存在。
	 */
	private static final String OSS_ERROR_CODE_NO_SUCH_KEY = "NoSuchKey";

	/**
	 * MinIO 常见 bucket 不存在消息片段。
	 */
	private static final String MESSAGE_BUCKET_NOT_EXIST = "The specified bucket does not exist";

	/**
	 * MinIO / OSS 常见 object 不存在消息片段。
	 */
	private static final String MESSAGE_KEY_NOT_EXIST = "The specified key does not exist";

	/**
	 * 判断 MinIO 删除异常是否表示对象已不存在（可幂等视为成功）。
	 * @param exception 删除过程抛出的异常
	 * @return {@code true} 表示 bucket 或 object 已不存在
	 */
	public static boolean isMinioObjectNotFound(Exception exception) {
		if (exception instanceof ErrorResponseException errorResponseException) {
			String errorCode = errorResponseException.errorResponse().code();
			return MINIO_ERROR_CODE_NO_SUCH_BUCKET.equals(errorCode) || MINIO_ERROR_CODE_NO_SUCH_KEY.equals(errorCode);
		}
		return containsNotFoundMessage(exception);
	}

	/**
	 * 判断 OSS 删除异常是否表示对象已不存在（可幂等视为成功）。
	 * @param exception 删除过程抛出的异常
	 * @return {@code true} 表示 bucket 或 object 已不存在
	 */
	public static boolean isOssObjectNotFound(Exception exception) {
		if (exception instanceof OSSException ossException) {
			String errorCode = ossException.getErrorCode();
			return OSS_ERROR_CODE_NO_SUCH_BUCKET.equals(errorCode) || OSS_ERROR_CODE_NO_SUCH_KEY.equals(errorCode);
		}
		return containsNotFoundMessage(exception);
	}

	/**
	 * 兜底：通过异常消息识别 bucket / object 不存在。
	 * @param exception 删除过程抛出的异常
	 * @return {@code true} 表示消息表明资源已不存在
	 */
	private static boolean containsNotFoundMessage(Exception exception) {
		String message = exception.getMessage();
		if (CharSequenceUtil.isBlank(message)) {
			return false;
		}
		return message.contains(MESSAGE_BUCKET_NOT_EXIST) || message.contains(MESSAGE_KEY_NOT_EXIST);
	}

}
