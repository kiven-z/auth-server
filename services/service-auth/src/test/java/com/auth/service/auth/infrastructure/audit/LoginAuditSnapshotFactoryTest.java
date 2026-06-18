package com.auth.service.auth.infrastructure.audit;

import com.auth.common.ip.IpAddressService;
import com.auth.common.ip.IpInfo;
import com.auth.common.web.model.entity.UserAgent;
import com.auth.service.auth.domain.authentication.model.LoginAuditSnapshot;
import com.auth.service.auth.infrastructure.audit.adapter.LoginAuditAdapter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

/**
 * {@link LoginAuditAdapter} 快照构建单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class LoginAuditSnapshotAdapterTest {

	@Mock
	private IpAddressService ipAddressService;

	@Test
	@DisplayName("请求为 null 时应返回空快照")
	void buildSnapshot_shouldReturnEmpty_whenRequestNull() {
		LoginAuditAdapter adapter = new LoginAuditAdapter(ipAddressService);
		LoginAuditSnapshot snapshot = adapter.buildSnapshot(null);
		assertEquals(LoginAuditSnapshot.empty(), snapshot);
	}

	@Test
	@DisplayName("有效请求时应解析 IP 与 UA 相关字段")
	void buildSnapshot_shouldPopulateFields_whenRequestPresent() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
		when(ipAddressService.resolveIpInfo(same(request)))
			.thenReturn(IpInfo.builder().ipAddr("192.0.2.1").ipRegion("Test-Region").build());

		UserAgent ua = mock(UserAgent.class);
		when(ua.getDeviceType()).thenReturn("PC");
		when(ua.getOs()).thenReturn("Linux");
		when(ua.getBrowser()).thenReturn("Firefox");

		LoginAuditAdapter adapter = new LoginAuditAdapter(ipAddressService);
		try (MockedStatic<UserAgent> userAgentMock = mockStatic(UserAgent.class)) {
			userAgentMock.when(() -> UserAgent.getUserAgent(request)).thenReturn(ua);
			LoginAuditSnapshot snapshot = adapter.buildSnapshot(request);
			assertEquals("192.0.2.1", snapshot.loginIp());
			assertEquals("Test-Region", snapshot.loginRegion());
			assertEquals("Mozilla/5.0", snapshot.userAgent());
			assertEquals("PC", snapshot.deviceType());
			assertEquals("Linux", snapshot.osType());
			assertEquals("Firefox", snapshot.browserType());
		}
	}

	@Test
	@DisplayName("空快照各 accessor 应为 null")
	void empty_shouldHaveNullFields() {
		LoginAuditSnapshot empty = LoginAuditSnapshot.empty();
		assertNull(empty.loginIp());
		assertNull(empty.userAgent());
	}

}
