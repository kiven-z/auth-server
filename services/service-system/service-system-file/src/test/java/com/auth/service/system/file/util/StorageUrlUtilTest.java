package com.auth.service.system.file.util;

import com.auth.service.system.file.utils.StorageUrlUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link StorageUrlUtil} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("StorageUrlUtil 地址拼接")
class StorageUrlUtilTest {

	@Test
	@DisplayName("concatPublicUrl：自动裁剪斜杠后拼接")
	void concatPublicUrlTrimsSlashBeforeJoin() {
		// 验证公共地址与对象路径拼接时不会出现重复斜杠。
		String url = StorageUrlUtil.concatPublicUrl("https://cdn.example.com/", "/avatar/a.png");

		assertThat(url).isEqualTo("https://cdn.example.com/avatar/a.png");
	}

	@Test
	@DisplayName("normalizeHttpBaseUrl：endpoint 无协议时补全 https")
	void normalizeHttpBaseUrlAddsHttpsWhenProtocolMissing() {
		// 验证未显式声明协议的 endpoint 会统一补全 https，保持 URL 生成行为稳定。
		String baseUrl = StorageUrlUtil.normalizeHttpBaseUrl("oss-cn-hangzhou.aliyuncs.com/");

		assertThat(baseUrl).isEqualTo("https://oss-cn-hangzhou.aliyuncs.com");
	}

}
