package com.auth.service.system.file.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.storage.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件临时访问地址签名器
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class FileUrlSigner {

	/**
	 * MIME 类型通配后缀，如 image/*
	 */
	private static final String CONTENT_TYPE_WILDCARD_SUFFIX = "/*";

	/**
	 * 通配后缀中的星号，用于截取类型前缀
	 */
	private static final String CONTENT_TYPE_WILDCARD_CHAR = "*";

	private final FileUploadProperties fileUploadProperties;

	private final StoragePlatformFacadeRegistry facadeRegistry;

	/**
	 * 生成带过期时间的访问地址
	 * @param bucket 存储桶
	 * @param objectKey 对象键
	 * @param contentType 文件类型
	 * @param platform 存储平台
	 * @return 临时访问地址
	 */
	public String sign(String bucket, String objectKey, String contentType, StoragePlatformEnum platform) {
		if (platform == null) {
			throw new FileStorageException(FileUploadResultCode.FILE_STORAGE_PLATFORM_UNSUPPORTED, "null");
		}
		if (CharSequenceUtil.isBlank(bucket)) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE, "bucket");
		}
		if (CharSequenceUtil.isBlank(objectKey)) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE, "objectKey");
		}

		requirePreviewContentTypeAllowed(contentType);

		FileStorageProvider provider = facadeRegistry.resolve(platform).provider();
		return provider.presignGetUrl(bucket, objectKey, fileUploadProperties.getExpireSeconds());
	}

	/**
	 * 校验当前文件类型是否允许预览
	 * @param contentType 文件类型
	 */
	private void requirePreviewContentTypeAllowed(String contentType) {
		List<String> allowedTypes = fileUploadProperties.getAllowedContentTypes();
		if (CollUtil.isEmpty(allowedTypes)) {
			return;
		}

		boolean matched = allowedTypes.stream()
			.filter(CharSequenceUtil::isNotBlank)
			.map(CharSequenceUtil::trim)
			.anyMatch(pattern -> {
				// 内容类型匹配（支持 image/* 形式）
				if (CharSequenceUtil.isBlank(contentType) || CharSequenceUtil.isBlank(pattern)) {
					return false;
				}
				if (CharSequenceUtil.endWith(pattern, CONTENT_TYPE_WILDCARD_SUFFIX)) {
					String prefix = CharSequenceUtil.removeSuffix(pattern, CONTENT_TYPE_WILDCARD_CHAR);
					return CharSequenceUtil.startWithIgnoreCase(contentType, prefix);
				}
				return CharSequenceUtil.equalsIgnoreCase(contentType, pattern);
			});
		if (!matched) {
			throw new FileStorageException(FileUploadResultCode.FILE_PREVIEW_CONTENT_TYPE_NOT_ALLOWED, contentType);
		}
	}

}
