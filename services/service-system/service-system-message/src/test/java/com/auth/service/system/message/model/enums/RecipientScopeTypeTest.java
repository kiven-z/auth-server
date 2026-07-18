package com.auth.service.system.message.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link RecipientScopeType} 单元测试
 *
 * @author Bunny
 */
@DisplayName("RecipientScopeType 范围与投递模式")
class RecipientScopeTypeTest {

	@Test
	@DisplayName("ALL 为读扩散，其余为写扩散")
	void deliveryMode_shouldMapPullOnlyForAll() {
		// 扩展新范围时在此声明投递模式即可
		assertThat(RecipientScopeType.ALL.deliveryMode()).isEqualTo(DeliveryMode.PULL);
		assertThat(RecipientScopeType.USER.deliveryMode()).isEqualTo(DeliveryMode.PUSH);
		assertThat(RecipientScopeType.POST.deliveryMode()).isEqualTo(DeliveryMode.PUSH);
		assertThat(RecipientScopeType.DEPT.deliveryMode()).isEqualTo(DeliveryMode.PUSH);
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
