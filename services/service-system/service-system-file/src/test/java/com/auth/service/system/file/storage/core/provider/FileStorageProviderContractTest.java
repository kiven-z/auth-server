package com.auth.service.system.file.storage.core.provider;

import com.auth.service.system.file.storage.core.s3.S3CompatibleFileStorageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FileStorageProvider} 组合接口契约测试。
 *
 * @author Bunny
 */
@DisplayName("FileStorageProvider 能力组合契约")
class FileStorageProviderContractTest {

	@Test
	@DisplayName("组合接口：同时继承读写能力接口")
	void providerExtendsReadAndWriteOps() {
		// 验证调用方可继续面向 FileStorageProvider，且读写能力边界已拆分。
		assertThat(FileStorageReadOps.class.isAssignableFrom(FileStorageProvider.class)).isTrue();
		assertThat(FileStorageWriteOps.class.isAssignableFrom(FileStorageProvider.class)).isTrue();
	}

	@Test
	@DisplayName("统一实现：S3CompatibleFileStorageProvider 满足组合接口")
	void unifiedProviderImplementsCompositeInterface() {
		// 验证经过统一层后，唯一实现仍面向 FileStorageProvider。
		assertThat(FileStorageProvider.class.isAssignableFrom(S3CompatibleFileStorageProvider.class)).isTrue();
	}

}
