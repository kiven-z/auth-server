package com.auth.module.security.contract.api.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link AuditOperationModuleKeys} 单元测试
 *
 * @author Bunny
 */
@DisplayName("AuditOperationModuleKeys")
class AuditOperationModuleKeysTest {

	@Test
	@DisplayName("应生成 SERVICE_DOMAIN:BIZ_MODULE 复合键")
	void toPersistedModuleKey_shouldJoinDomainAndBiz() {
		// 与 log_operation.module 落库格式一致，便于按微服务域前缀筛选
		assertEquals("SYSTEM:SYS_DEPT",
				AuditOperationModuleKeys.toPersistedModuleKey(AuditServiceDomain.SYSTEM, AuditBizModule.SYS_DEPT));
		assertEquals("EXAMPLE:EXAMPLE_DEMO",
				AuditOperationModuleKeys.toPersistedModuleKey(AuditServiceDomain.EXAMPLE, AuditBizModule.EXAMPLE_DEMO));
	}

}
