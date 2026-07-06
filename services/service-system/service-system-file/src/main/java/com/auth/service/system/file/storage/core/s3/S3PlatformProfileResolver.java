package com.auth.service.system.file.storage.core.s3;

import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.config.properties.S3PlatformProfile;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * S3 协议配置解析器：优先读取 platforms 显式配置，缺省时委托平台来源适配
 *
 * @author Bunny
 */
@Component
public class S3PlatformProfileResolver {

	private final FileUploadProperties fileUploadProperties;

	private final Map<StoragePlatformEnum, S3PlatformProfileSource> sourceByPlatform;

	public S3PlatformProfileResolver(FileUploadProperties fileUploadProperties,
			List<S3PlatformProfileSource> profileSources) {
		this.fileUploadProperties = fileUploadProperties;
		Map<StoragePlatformEnum, S3PlatformProfileSource> mutableSourceByPlatform = new EnumMap<>(
				StoragePlatformEnum.class);
		for (S3PlatformProfileSource profileSource : profileSources) {
			StoragePlatformEnum platform = profileSource.platform();
			S3PlatformProfileSource previousSource = mutableSourceByPlatform.put(platform, profileSource);
			if (previousSource != null) {
				throw new FileStorageException(FileUploadResultCode.FILE_STORAGE_PLATFORM_DUPLICATED, platform.name());
			}
		}
		this.sourceByPlatform = Collections.unmodifiableMap(mutableSourceByPlatform);
	}

	/**
	 * 依据平台返回 S3 协议配置
	 * @param platform 存储平台
	 * @return S3 协议配置
	 */
	public S3PlatformProfile resolve(StoragePlatformEnum platform) {
		Map<StoragePlatformEnum, S3PlatformProfile> platforms = fileUploadProperties.getPlatforms();
		if (platforms != null) {
			S3PlatformProfile profile = platforms.get(platform);
			if (profile != null) {
				return profile;
			}
		}
		S3PlatformProfileSource profileSource = sourceByPlatform.get(platform);
		if (profileSource == null) {
			throw new FileStorageException(FileUploadResultCode.FILE_STORAGE_PLATFORM_UNSUPPORTED, platform.name());
		}
		return profileSource.resolveFallback(fileUploadProperties);
	}

}
