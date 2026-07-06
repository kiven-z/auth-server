package com.auth.module.file.api.enums;

import com.auth.module.file.api.enums.FileBizType.ResolvedVisibility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

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
	@DisplayName("parse：大小写与空白均可归一化解析")
	void parseParsesCodeIgnoringCaseAndWhitespace() {
		// avatar 采用标准化 code 后可识别
		Optional<FileBizType> avatar = FileBizType.parse("  AvAtAr  ");
		Optional<FileBizType> attachment = FileBizType.parse("ATTACHMENT");

		assertThat(avatar).contains(FileBizType.AVATAR);
		assertThat(attachment).contains(FileBizType.ATTACHMENT);
	}

	@Test
	@DisplayName("parse：空值返回 Optional.empty")
	void parseReturnsEmptyForBlankInput() {
		// 验证空输入不会命中任何业务类型
		assertThat(FileBizType.parse(null)).isEmpty();
		assertThat(FileBizType.parse("")).isEmpty();
		assertThat(FileBizType.parse("   ")).isEmpty();
	}

	@Test
	@DisplayName("parse：非法值返回 Optional.empty")
	void parseReturnsEmptyForUnknownCode() {
		// 验证未知业务类型按宽松策略返回 empty
		assertThat(FileBizType.parse("unknown")).isEmpty();
		assertThat(FileBizType.parse("product_image")).isEmpty();
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
		assertThat(FileBizType.ATTACHMENT.getCode()).isEqualTo("attachment");
		assertThat(FileBizType.ATTACHMENT.getVisibilityRule()).isEqualTo(VisibilityRule.DEFAULT_PRIVATE);
	}

	@Test
	@DisplayName("resolveVisibility：avatar 固定公开且忽略客户端覆盖请求")
	void resolveVisibilityForAvatarUsesPublicVisibilityAndIgnoresOverride() {
		// 验证 avatar 策略固定公开并禁用覆盖
		ResolvedVisibility resolvedWhenRequestPrivate = FileBizType.resolveVisibility("avatar", true);
		ResolvedVisibility resolvedWhenRequestPublic = FileBizType.resolveVisibility("avatar", false);

		assertThat(resolvedWhenRequestPrivate.isPrivate()).isFalse();
		assertThat(resolvedWhenRequestPrivate.bizTypeCode()).isEqualTo("avatar");
		assertThat(resolvedWhenRequestPublic.isPrivate()).isFalse();
		assertThat(resolvedWhenRequestPublic.bizTypeCode()).isEqualTo("avatar");
	}

	@Test
	@DisplayName("resolveVisibility：attachment 默认私有且允许客户端覆盖")
	void resolveVisibilityForAttachmentAllowsClientOverride() {
		// 验证 attachment 策略在传入请求值时可切换可见性
		ResolvedVisibility resolvedDefault = FileBizType.resolveVisibility("attachment", null);
		ResolvedVisibility resolvedPublic = FileBizType.resolveVisibility("attachment", false);
		ResolvedVisibility resolvedPrivate = FileBizType.resolveVisibility("attachment", true);

		assertThat(resolvedDefault.isPrivate()).isTrue();
		assertThat(resolvedPublic.isPrivate()).isFalse();
		assertThat(resolvedPrivate.isPrivate()).isTrue();
		assertThat(resolvedDefault.bizTypeCode()).isEqualTo("attachment");
	}

	@Test
	@DisplayName("resolveVisibility：未知 bizType 抛出 IllegalArgumentException")
	void resolveVisibilityThrowsWhenBizTypeIsUnknown() {
		// 验证严格模式拒绝未知业务类型，避免静默回退
		assertThatThrownBy(() -> FileBizType.resolveVisibility("product_image", false))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("product_image");
	}

	@Test
	@DisplayName("resolveVisibility：空 bizType 抛出 IllegalArgumentException")
	void resolveVisibilityThrowsWhenBizTypeIsBlank() {
		// 验证严格模式拒绝空白输入，避免错误业务类型落库
		assertThatThrownBy(() -> FileBizType.resolveVisibility(" ", false)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("blank");
	}

}
