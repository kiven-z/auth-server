package com.auth.service.auth.service.impl;

import com.auth.common.ip.IpAddressService;
import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.service.auth.mapper.LoginLogMapper;
import com.auth.service.auth.model.enums.AuthLoginLogResult;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * LoginLogServiceImpl 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class LoginLogServiceImplTest {

	@Mock
	private IpAddressService ipAddressService;

	@Mock
	private LoginLogMapper loginLogMapper;

	private LoginLogServiceImpl loginLogService;

	@BeforeEach
	void setUp() throws Exception {
		loginLogService = new LoginLogServiceImpl(ipAddressService);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(loginLogService, loginLogMapper);
	}

	@Test
	@DisplayName("审计写入：无 HttpServletRequest 时仍写入 loginResult 与事件类型")
	void record_LoginLog_shouldInsertEntity_whenRequestNull() {
		loginLogService.recordLoginLog(null, AuthLoginLogType.LOGIN_PASSWORD, AuthLoginLogResult.SUCCESS, 1L, "user",
				null, "jti-1");

		ArgumentCaptor<LoginLogEntity> captor = ArgumentCaptor.forClass(LoginLogEntity.class);
		verify(loginLogMapper).insert(captor.capture());
		LoginLogEntity saved = captor.getValue();
		assertEquals(0, saved.getLoginResult());
		assertEquals(AuthLoginLogType.LOGIN_PASSWORD.name(), saved.getLoginType());
		assertEquals("user", saved.getUsername());
		assertEquals("jti-1", saved.getSessionId());
	}

	@Test
	@DisplayName("审计写入：插入抛异常时不向外抛出")
	void record_LoginLog_shouldSwallowException_whenMapperFails() {
		doThrow(new RuntimeException("db down")).when(loginLogMapper).insert(any(LoginLogEntity.class));

		assertDoesNotThrow(() -> loginLogService.recordLoginLog(null, AuthLoginLogType.REFRESH_TOKEN,
				AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR, null, null, "token.invalid", null));

		verify(loginLogMapper).insert(any(LoginLogEntity.class));
	}

}
