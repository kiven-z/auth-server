package com.auth.service.system.common.service.impl;

import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.event.SecurityEventType;
import com.auth.service.system.log.mapper.LogAuthorizationAuditMapper;
import com.auth.service.system.log.model.entity.LogAuthorizationAuditEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * {@link SecurityIngestServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SecurityIngestServiceImpl 安全上报落库")
@ExtendWith(MockitoExtension.class)
class SecurityIngestServiceImplTest {

    @Mock
    private LogAuthorizationAuditMapper logAuthorizationAuditMapper;

    @InjectMocks
    private SecurityIngestServiceImpl securityIngestService;

    @Test
    @DisplayName("append(授权审计)：未认证主体时 create_user 落库为 0")
    void appendAuthorizationAuditUsesZeroWhenUserIdNull() {
        SecurityAuthorizationAuditPayloadEvent event = SecurityAuthorizationAuditPayloadEvent.builder()
                .eventType(SecurityEventType.GRANTED)
                .userId(null)
                .decisionReason("UNAUTHENTICATED")
                .build();

        securityIngestService.append(event);

        ArgumentCaptor<LogAuthorizationAuditEntity> captor = ArgumentCaptor.forClass(LogAuthorizationAuditEntity.class);
        verify(logAuthorizationAuditMapper).insert(captor.capture());
        assertThat(captor.getValue().getCreateUser()).isEqualTo(0L);
    }

    @Test
    @DisplayName("append(授权审计)：已认证主体时 create_user 为事件中的 userId")
    void appendAuthorizationAuditUsesEventUserId() {
        SecurityAuthorizationAuditPayloadEvent event = SecurityAuthorizationAuditPayloadEvent.builder()
                .eventType(SecurityEventType.DENIED)
                .userId(42L)
                .decisionReason("ACCESS_DENIED")
                .build();

        securityIngestService.append(event);

        ArgumentCaptor<LogAuthorizationAuditEntity> captor = ArgumentCaptor.forClass(LogAuthorizationAuditEntity.class);
        verify(logAuthorizationAuditMapper).insert(captor.capture());
        assertThat(captor.getValue().getCreateUser()).isEqualTo(42L);
    }

}
