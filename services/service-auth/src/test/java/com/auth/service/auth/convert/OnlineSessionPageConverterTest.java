package com.auth.service.auth.convert;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.domain.session.model.UserOnlineBrief;
import com.auth.service.auth.domain.session.model.vo.OnlineSessionPageVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link OnlineSessionPageConverter} 在线会话分页行映射单元测试
 */
@DisplayName("OnlineSessionPageConverter 在线会话分页行映射")
class OnlineSessionPageConverterTest {

	@Test
	@DisplayName("会话索引与用户快照合并为分页行")
	void toPageVo_mapsSessionAndUserBrief() {
		UserSessionIndex session = sampleSession();
		UserOnlineBrief userBrief = new UserOnlineBrief(TestConstants.USER_ID, "alice", "Alice");

		OnlineSessionPageVO vo = OnlineSessionPageConverter.INSTANCE.toPageVo(session, userBrief);

		assertThat(vo.getUserId()).isEqualTo(TestConstants.USER_ID);
		assertThat(vo.getSessionId()).isEqualTo(TestConstants.JTI);
		assertThat(vo.getIpAddress()).isEqualTo("192.168.1.1");
		assertThat(vo.getIpRegion()).isEqualTo("内网");
		assertThat(vo.getDeviceType()).isEqualTo("desktop");
		assertThat(vo.getBrowserType()).isEqualTo("Chrome");
		assertThat(vo.getOsType()).isEqualTo("Linux");
		assertThat(vo.getLoginAt()).isEqualTo(1_700_000_000_000L);
		assertThat(vo.getRememberMe()).isTrue();
		assertThat(vo.getUsername()).isEqualTo("alice");
		assertThat(vo.getNickname()).isEqualTo("Alice");
	}

	@Test
	@DisplayName("用户快照为 null 时仅映射会话字段")
	void toPageVo_mapsSessionOnlyWhenUserBriefMissing() {
		UserSessionIndex session = sampleSession();

		OnlineSessionPageVO vo = OnlineSessionPageConverter.INSTANCE.toPageVo(session, null);

		assertThat(vo.getUserId()).isEqualTo(TestConstants.USER_ID);
		assertThat(vo.getSessionId()).isEqualTo(TestConstants.JTI);
		assertThat(vo.getUsername()).isNull();
		assertThat(vo.getNickname()).isNull();
	}

	/**
	 * 构造样例会话索引
	 * @return 会话索引
	 */
	private UserSessionIndex sampleSession() {
		UserSessionIndex session = new UserSessionIndex();
		session.setUserId(TestConstants.USER_ID);
		session.setSessionId(TestConstants.JTI);
		session.setIpAddress("192.168.1.1");
		session.setIpRegion("内网");
		session.setDeviceType("desktop");
		session.setBrowserType("Chrome");
		session.setOsType("Linux");
		session.setLoginAt(1_700_000_000_000L);
		session.setRememberMe(true);
		return session;
	}

}
