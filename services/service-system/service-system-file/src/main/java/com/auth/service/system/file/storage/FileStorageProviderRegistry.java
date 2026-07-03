package com.auth.service.system.file.storage;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.auth.service.system.file.exception.FileException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.module.file.api.enums.StoragePlatformEnum;

/**
 * 文件存储策略注册表
 *
 * @author Bunny
 */
@Component
public class FileStorageProviderRegistry {

	private final Map<StoragePlatformEnum, FileStorageProvider> providerByPlatform;

	public FileStorageProviderRegistry(List<FileStorageProvider> providers) {
		Map<StoragePlatformEnum, FileStorageProvider> mutableProviderByPlatform = new EnumMap<>(
				StoragePlatformEnum.class);

		for (FileStorageProvider provider : providers) {
			StoragePlatformEnum platform = provider.platform();
			FileStorageProvider previousProvider = mutableProviderByPlatform.put(platform, provider);
			if (previousProvider != null) {
				throw new FileException(FileUploadResultCode.FILE_STORAGE_PLATFORM_DUPLICATED, platform.name());
			}
		}

		this.providerByPlatform = Collections.unmodifiableMap(mutableProviderByPlatform);
	}

	/**
	 * 根据平台解析策略实现
	 * @param platform 存储平台
	 * @return 策略实现
	 */
	public FileStorageProvider resolve(StoragePlatformEnum platform) {
		FileStorageProvider provider = providerByPlatform.get(platform);
		if (provider == null) {
			throw new FileException(FileUploadResultCode.FILE_STORAGE_PLATFORM_UNSUPPORTED, platform.name());
		}

		return provider;
	}

}
