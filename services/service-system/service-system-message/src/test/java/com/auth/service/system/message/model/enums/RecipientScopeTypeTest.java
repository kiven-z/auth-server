package com.auth.service.system.message.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link RecipientScopeType} 单元测试
 *
 * @author Bunny
 */
@DisplayName("RecipientScopeType 范围与读扩散")
class RecipientScopeTypeTest {

	@Test
	@DisplayName("仅 ALL 为读扩散")
	void isPull_shouldBeTrueOnlyForAll() {
		assertThat(RecipientScopeType.ALL.isPull()).isTrue();
		assertThat(RecipientScopeType.USER.isPull()).isFalse();
		assertThat(RecipientScopeType.POST.isPull()).isFalse();
		assertThat(RecipientScopeType.DEPT.isPull()).isFalse();
	}

	@Test
	@DisplayName("from 解析大小写不敏感，非法返回 null")
	void from_shouldParseIgnoreCase() {
		assertThat(RecipientScopeType.from("all")).isEqualTo(RecipientScopeType.ALL);
		assertThat(RecipientScopeType.from("DEPT")).isEqualTo(RecipientScopeType.DEPT);
		assertThat(RecipientScopeType.from(" ")).isNull();
		assertThat(RecipientScopeType.from("FOO")).isNull();
	}

}
