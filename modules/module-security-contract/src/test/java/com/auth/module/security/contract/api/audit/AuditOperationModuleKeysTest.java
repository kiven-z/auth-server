package com.auth.module.security.contract.api.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
		assertEquals("SYSTEM:SYS_DEPT",
				AuditOperationModuleKeys.toPersistedModuleKey(AuditServiceDomain.SYSTEM, "SYS_DEPT"));
		assertEquals("EXAMPLE:EXAMPLE_DEMO",
				AuditOperationModuleKeys.toPersistedModuleKey(AuditServiceDomain.EXAMPLE, "EXAMPLE_DEMO"));
	}

	@Test
	@DisplayName("bizModuleCode 仅空白时应拒绝")
	void toPersistedModuleKey_shouldRejectBlankBizCode() {
		assertThrows(IllegalArgumentException.class,
				() -> AuditOperationModuleKeys.toPersistedModuleKey(AuditServiceDomain.SYSTEM, "   "));
	}

}
