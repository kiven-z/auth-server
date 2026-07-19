package com.auth.module.file.api.enums.policy;

import com.auth.module.file.api.policy.AllowedMimeTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AllowedMimeTypes} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("AllowedMimeTypes MIME 常量组")
class AllowedMimeTypesTest {

	@Test
	@DisplayName("RASTER_IMAGE：不含 GIF，RASTER_IMAGE_WITH_GIF 含 GIF")
	void rasterGroupsDifferentiateGif() {
		// 验证头像组与配图组在 GIF 上的差异
		assertThat(AllowedMimeTypes.RASTER_IMAGE).doesNotContain("image/gif");
		assertThat(AllowedMimeTypes.RASTER_IMAGE_WITH_GIF).contains("image/gif");
	}

}
