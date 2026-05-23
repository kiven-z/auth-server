package com.auth.service.system.log.convert;

import com.auth.module.security.contract.event.OperationLogPayloadEvent;
import com.auth.service.system.log.model.entity.LogOperationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link LogOperationPayloadConverter} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("LogOperationPayloadConverter 操作日志映射")
class LogOperationPayloadConverterTest {

	@Test
	@DisplayName("未登录：操作用户与审计列为 null")
	void toInsertEntity_nullPrincipal() {
		OperationLogPayloadEvent payload = OperationLogPayloadEvent.builder()
			.operationType("QUERY")
			.module("EXAMPLE:EXAMPLE_DEMO")
			.requestMethod("GET")
			.requestUri("/api/example")
			.ipAddress("127.0.0.1")
			.build();

		LogOperationEntity entity = LogOperationPayloadConverter.INSTANCE.toInsertEntity(payload, null);

		assertThat(entity.getUserId()).isNull();
		assertThat(entity.getCreateUser()).isNull();
		assertThat(entity.getUpdateUser()).isNull();
		assertThat(entity.getOperationType()).isEqualTo("QUERY");
	}

	@Test
	@DisplayName("已登录：操作用户与审计列使用 principalUserId")
	void toInsertEntity_withPrincipal() {
		OperationLogPayloadEvent payload = OperationLogPayloadEvent.builder()
			.operationType("UPDATE")
			.module("SYSTEM:SYS_DEPT")
			.requestMethod("PUT")
			.requestUri("/api/system/dept/1")
			.ipAddress("10.0.0.1")
			.build();

		LogOperationEntity entity = LogOperationPayloadConverter.INSTANCE.toInsertEntity(payload, 100L);

		assertThat(entity.getUserId()).isEqualTo(100L);
		assertThat(entity.getCreateUser()).isEqualTo(100L);
		assertThat(entity.getUpdateUser()).isEqualTo(100L);
	}

}
