package com.auth.module.file.api.enums;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link StoragePlatformEnum} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("StoragePlatformEnum 存储平台解析")
class StoragePlatformEnumTest {

	@Test
	@DisplayName("fromNullable：空值返回 null")
	void fromNullableReturnsNullForBlank() {
		assertThat(StoragePlatformEnum.fromNullable(null)).isNull();
		assertThat(StoragePlatformEnum.fromNullable("")).isNull();
		assertThat(StoragePlatformEnum.fromNullable("  ")).isNull();
	}

	@Test
	@DisplayName("fromNullable：合法平台名解析成功")
	void fromNullableParsesValidPlatform() {
		assertThat(StoragePlatformEnum.fromNullable("minio")).isEqualTo(StoragePlatformEnum.MINIO);
		assertThat(StoragePlatformEnum.fromNullable("ALIYUN_OSS")).isEqualTo(StoragePlatformEnum.ALIYUN_OSS);
	}

	@Test
	@DisplayName("fromNullable：未知平台抛出 IllegalArgumentException")
	void fromNullableThrowsForUnknownPlatform() {
		assertThatThrownBy(() -> StoragePlatformEnum.fromNullable("unknown"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("unknown");
	}

}
