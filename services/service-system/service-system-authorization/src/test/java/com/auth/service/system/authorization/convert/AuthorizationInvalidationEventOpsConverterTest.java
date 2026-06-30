package com.auth.service.system.authorization.convert;

import java.time.LocalDateTime;

import com.auth.service.system.authorization.feign.dto.AuthorizationInvalidationEventInnerQuery;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationEventOpsQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AuthorizationInvalidationEventOpsConverter} 单元测试
 */
@DisplayName("AuthorizationInvalidationEventOpsConverter 幂等事件运维映射")
class AuthorizationInvalidationEventOpsConverterTest {

	@Test
	@DisplayName("运维查询应映射为 Feign 分页查询")
	void toInnerQuery_shouldMapQueryFields() {
		LocalDateTime processedAtStart = LocalDateTime.of(2026, 6, 1, 0, 0);
		LocalDateTime processedAtEnd = LocalDateTime.of(2026, 6, 19, 23, 59);
		AuthorizationInvalidationEventOpsQuery query = new AuthorizationInvalidationEventOpsQuery();
		query.setPageIndex(2);
		query.setPageSize(20);
		query.setEventId("evt-1");
		query.setChangeKind("ROLE");
		query.setProcessing(Boolean.TRUE);
		query.setProcessedAtStart(processedAtStart);
		query.setProcessedAtEnd(processedAtEnd);

		AuthorizationInvalidationEventInnerQuery innerQuery = AuthorizationInvalidationEventOpsConverter.INSTANCE
			.toInnerQuery(query);

		assertThat(innerQuery.getPageIndex()).isEqualTo(2);
		assertThat(innerQuery.getPageSize()).isEqualTo(20);
		assertThat(innerQuery.getEventId()).isEqualTo("evt-1");
		assertThat(innerQuery.getChangeKind()).isEqualTo("ROLE");
		assertThat(innerQuery.getProcessing()).isTrue();
		assertThat(innerQuery.getProcessedAtStart()).isEqualTo(processedAtStart);
		assertThat(innerQuery.getProcessedAtEnd()).isEqualTo(processedAtEnd);
	}

}
