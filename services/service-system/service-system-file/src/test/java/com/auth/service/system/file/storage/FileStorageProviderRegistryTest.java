package com.auth.service.system.file.storage;

import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.file.exception.FileException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.model.value.FileUploadCommand;
import com.auth.service.system.file.model.value.StoredFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link FileStorageProviderRegistry} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileStorageProviderRegistry 存储路由")
class FileStorageProviderRegistryTest {

	@Test
	@DisplayName("resolve：返回匹配平台的 provider")
	void resolveReturnsMatchedProvider() {
		// 验证注册表能够按平台正确路由策略实现。
		FileStorageProvider minioProvider = new StubProvider(StoragePlatformEnum.MINIO);
		FileStorageProvider aliyunProvider = new StubProvider(StoragePlatformEnum.ALIYUN_OSS);
		FileStorageProviderRegistry registry = new FileStorageProviderRegistry(List.of(minioProvider, aliyunProvider));

		assertThat(registry.resolve(StoragePlatformEnum.MINIO)).isSameAs(minioProvider);
		assertThat(registry.resolve(StoragePlatformEnum.ALIYUN_OSS)).isSameAs(aliyunProvider);
	}

	@Test
	@DisplayName("resolve：平台未注册时抛出业务异常")
	void resolveThrowsWhenProviderMissing() {
		// 验证未注册平台会抛出统一业务异常，避免调用方拿到空 provider。
		FileStorageProviderRegistry registry = new FileStorageProviderRegistry(
				List.of(new StubProvider(StoragePlatformEnum.MINIO)));

		assertThatThrownBy(() -> registry.resolve(StoragePlatformEnum.ALIYUN_OSS)).isInstanceOf(FileException.class)
			.extracting(ex -> ((FileException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_STORAGE_PLATFORM_UNSUPPORTED);
	}

	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("registry：构造后使用不可变平台索引")
	void registryMapIsUnmodifiable() throws NoSuchFieldException, IllegalAccessException {
		// 验证注册表内部索引为不可变集合，避免运行期被篡改。
		FileStorageProvider minioProvider = new StubProvider(StoragePlatformEnum.MINIO);
		FileStorageProviderRegistry registry = new FileStorageProviderRegistry(List.of(minioProvider));
		java.lang.reflect.Field field = FileStorageProviderRegistry.class.getDeclaredField("providerByPlatform");
		field.setAccessible(true);
		Map<StoragePlatformEnum, FileStorageProvider> providerMap = (Map<StoragePlatformEnum, FileStorageProvider>) field
			.get(registry);

		StubProvider aliyunStub = new StubProvider(StoragePlatformEnum.ALIYUN_OSS);
		assertThatThrownBy(() -> providerMap.put(StoragePlatformEnum.ALIYUN_OSS, aliyunStub))
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	@DisplayName("registry：同平台重复注册时抛出业务异常")
	void constructorThrowsWhenPlatformDuplicated() {
		// 验证同一平台出现多个实现时，启动阶段即可失败并给出统一错误码。
		FileStorageProvider firstProvider = new StubProvider(StoragePlatformEnum.MINIO);
		FileStorageProvider secondProvider = new StubProvider(StoragePlatformEnum.MINIO);

		List<FileStorageProvider> duplicatedProviders = List.of(firstProvider, secondProvider);
		assertThatThrownBy(() -> new FileStorageProviderRegistry(duplicatedProviders)).isInstanceOf(FileException.class)
			.extracting(ex -> ((FileException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_STORAGE_PLATFORM_DUPLICATED);
	}

	/**
	 * 测试桩策略实现。
	 */
	private record StubProvider(StoragePlatformEnum platform) implements FileStorageProvider {

		@Override
		public StoredFile upload(FileUploadCommand command) {
			throw new UnsupportedOperationException("not used");
		}

		@Override
		public InputStream download(String bucket, String objectKey) {
			throw new UnsupportedOperationException("not used");
		}

		@Override
		public String presignGetUrl(String bucket, String objectKey, int expireSeconds) {
			throw new UnsupportedOperationException("not used");
		}

		@Override
		public void delete(String bucket, String objectKey) {
			throw new UnsupportedOperationException("not used");
		}

	}

}
