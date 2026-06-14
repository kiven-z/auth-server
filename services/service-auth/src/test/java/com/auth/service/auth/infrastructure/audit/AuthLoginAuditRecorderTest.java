package com.auth.service.auth.infrastructure.audit;

import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.service.auth.domain.authentication.enums.AuthLoginLogResult;
import com.auth.service.auth.domain.authentication.enums.AuthLoginLogType;
import com.auth.service.auth.domain.authentication.model.LoginAuditSnapshot;
import com.auth.service.auth.infrastructure.persistence.LoginLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * {@link AuthLoginAuditRecorder} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class AuthLoginAuditRecorderTest {

	@Mock
	private LoginLogMapper loginLogMapper;

	private AuthLoginAuditRecorder loginAuditRecorder;

	@BeforeEach
	void setUp() throws Exception {
		loginAuditRecorder = new AuthLoginAuditRecorder();
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(loginAuditRecorder, loginLogMapper);
	}

	@Test
	@DisplayName("审计写入：空快照仍写入 loginResult 与事件类型")
	void recordLoginLog_shouldInsertEntity_whenSnapshotEmpty() {
		loginAuditRecorder.recordLoginLog(LoginAuditSnapshot.empty(), AuthLoginLogType.LOGIN_PASSWORD,
				AuthLoginLogResult.SUCCESS, 1L, "user", null, "jti-1");

		ArgumentCaptor<LoginLogEntity> captor = ArgumentCaptor.forClass(LoginLogEntity.class);
		verify(loginLogMapper).insert(captor.capture());
		LoginLogEntity saved = captor.getValue();
		assertEquals(0, saved.getLoginResult());
		assertEquals(AuthLoginLogType.LOGIN_PASSWORD.name(), saved.getLoginType());
		assertEquals("user", saved.getUsername());
		assertEquals("jti-1", saved.getSessionId());
		assertEquals(1L, saved.getCreateUser());
		assertEquals(1L, saved.getUpdateUser());
	}

	@Test
	@DisplayName("审计写入：未识别用户时 user_id 与审计字段均为 null")
	void recordLoginLog_shouldLeaveAuditFieldsNull_whenUserIdNull() {
		loginAuditRecorder.recordLoginLog(LoginAuditSnapshot.empty(), AuthLoginLogType.LOGIN_PASSWORD,
				AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR, null, "13800138000", "user.not.found", null);

		ArgumentCaptor<LoginLogEntity> captor = ArgumentCaptor.forClass(LoginLogEntity.class);
		verify(loginLogMapper).insert(captor.capture());
		LoginLogEntity saved = captor.getValue();
		assertNull(saved.getUserId());
		assertNull(saved.getCreateUser());
		assertNull(saved.getUpdateUser());
		assertEquals("13800138000", saved.getUsername());
	}

	@Test
	@DisplayName("审计写入：插入抛异常时不向外抛出")
	void recordLoginLog_shouldSwallowException_whenMapperFails() {
		doThrow(new RuntimeException("db down")).when(loginLogMapper).insert(any(LoginLogEntity.class));

		assertDoesNotThrow(
				() -> loginAuditRecorder.recordLoginLog(LoginAuditSnapshot.empty(), AuthLoginLogType.REFRESH_TOKEN,
						AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR, null, null, "token.invalid", null));

		verify(loginLogMapper).insert(any(LoginLogEntity.class));
	}

	@Test
	@DisplayName("审计写入：显式快照应落库 IP 与 User-Agent 等字段")
	void recordLoginLog_shouldPersistSnapshotFields_whenSnapshotProvided() {
		LoginAuditSnapshot snapshot = new LoginAuditSnapshot("203.0.113.1", "TestRegion", "TestUA", "Desktop", "Linux",
				"Firefox");
		loginAuditRecorder.recordLoginLog(snapshot, AuthLoginLogType.LOGIN_EMAIL, AuthLoginLogResult.SUCCESS, 2L,
				"e@x.com", null, null);

		ArgumentCaptor<LoginLogEntity> captor = ArgumentCaptor.forClass(LoginLogEntity.class);
		verify(loginLogMapper).insert(captor.capture());
		LoginLogEntity saved = captor.getValue();
		assertEquals("203.0.113.1", saved.getLoginIp());
		assertEquals("TestRegion", saved.getLoginRegion());
		assertEquals("TestUA", saved.getUserAgent());
		assertEquals("Desktop", saved.getDeviceType());
		assertEquals("Linux", saved.getOsType());
		assertEquals("Firefox", saved.getBrowserType());
		assertEquals(AuthLoginLogType.LOGIN_EMAIL.name(), saved.getLoginType());
	}

}
