package com.auth.module.file.api.enums;

import com.auth.module.file.api.model.enums.FileBizType;
import com.auth.module.file.api.model.enums.VisibilityRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link FileBizType} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileBizType 业务类型解析")
class FileBizTypeTest {

	@Test
	@DisplayName("require：大小写与空白均可归一化解析")
	void requireParsesCodeIgnoringCaseAndWhitespace() {
		// 验证严格解析支持大小写与首尾空白归一化
		assertThat(FileBizType.require("  AvAtAr  ")).isEqualTo(FileBizType.AVATAR);
		assertThat(FileBizType.require("MESSAGE-IMAGE")).isEqualTo(FileBizType.MESSAGE_IMAGE);
		assertThat(FileBizType.require("ATTACHMENT")).isEqualTo(FileBizType.ATTACHMENT);
	}

	@Test
	@DisplayName("require：非法值抛出 IllegalArgumentException")
	void requireThrowsForUnknownCode() {
		// 验证严格解析会拒绝未知业务类型
		assertThatThrownBy(() -> FileBizType.require("unknown")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("unknown");
		assertThatThrownBy(() -> FileBizType.require("product_image")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("product_image");
	}

	@Test
	@DisplayName("require：空值抛出 IllegalArgumentException")
	void requireThrowsForBlankCode() {
		// 验证严格解析会拒绝空白输入
		assertThatThrownBy(() -> FileBizType.require(" ")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("blank");
	}

	@Test
	@DisplayName("策略字段：枚举项暴露可见性规则")
	void exposesPolicyMetadataOnEnumConstants() {
		// 验证策略元数据已内聚到枚举，避免布尔组合歧义
		assertThat(FileBizType.AVATAR.getCode()).isEqualTo("avatar");
		assertThat(FileBizType.AVATAR.getVisibilityRule()).isEqualTo(VisibilityRule.FORCE_PUBLIC);
		assertThat(FileBizType.MESSAGE_IMAGE.getCode()).isEqualTo("message-image");
		assertThat(FileBizType.MESSAGE_IMAGE.getVisibilityRule()).isEqualTo(VisibilityRule.FORCE_PUBLIC);
		assertThat(FileBizType.ATTACHMENT.getCode()).isEqualTo("attachment");
		assertThat(FileBizType.ATTACHMENT.getVisibilityRule()).isEqualTo(VisibilityRule.DEFAULT_PRIVATE);
	}

	@Test
	@DisplayName("可见性策略：avatar/message-image 固定公开，attachment 默认私有")
	void visibilityPolicyMatchesBizType() {
		// 验证各业务类型的可见性策略与 VisibilityRule 组合一致
		assertThat(FileBizType.AVATAR.getVisibilityRule().resolve(true)).isFalse();
		assertThat(FileBizType.AVATAR.getVisibilityRule().resolve(false)).isFalse();
		assertThat(FileBizType.MESSAGE_IMAGE.getVisibilityRule().resolve(true)).isFalse();
		assertThat(FileBizType.MESSAGE_IMAGE.getVisibilityRule().resolve(false)).isFalse();
		assertThat(FileBizType.ATTACHMENT.getVisibilityRule().resolve(null)).isTrue();
		assertThat(FileBizType.ATTACHMENT.getVisibilityRule().resolve(false)).isFalse();
		assertThat(FileBizType.ATTACHMENT.getVisibilityRule().resolve(true)).isTrue();
	}

}
