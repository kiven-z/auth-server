package com.auth.service.auth.infrastructure.redis.session.support;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.TestConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link UserSessionIndexRedisCodec} 会话 Hash 编解码单元测试
 */
@DisplayName("UserSessionIndexRedisCodec 会话 Hash 编解码")
class UserSessionIndexRedisCodecTest {

	private final UserSessionIndexRedisCodec codec = new UserSessionIndexRedisCodec();

	@Test
	@DisplayName("asStringHash：将 Object Map 转为字符串 Hash")
	void asStringHash_convertsObjectMap() {
		Map<Object, Object> raw = Map.of("userId", 1L, "ipAddress", "10.0.0.1");

		Map<String, String> hash = codec.asStringHash(raw);

		assertThat(hash).containsEntry("userId", "1").containsEntry("ipAddress", "10.0.0.1");
	}

	@Test
	@DisplayName("toSessionIndex：纯字符串 Hash 正确映射字段")
	void toSessionIndex_mapsPlainStringHash() {
		Map<String, String> hash = Map.of("userId", String.valueOf(TestConstants.USER_ID), "ipAddress", "10.0.0.1",
				"loginAt", "1700000000000", "rememberMe", "true");

		UserSessionIndex session = codec.toSessionIndex(TestConstants.JTI, hash, null);

		assertThat(session.getSessionId()).isEqualTo(TestConstants.JTI);
		assertThat(session.getUserId()).isEqualTo(TestConstants.USER_ID);
		assertThat(session.getIpAddress()).isEqualTo("10.0.0.1");
		assertThat(session.getLoginAt()).isEqualTo(1_700_000_000_000L);
		assertThat(session.getRememberMe()).isTrue();
	}

	@Test
	@DisplayName("toSessionIndex：兼容旧数据中带 JSON 引号的 field/value")
	void toSessionIndex_normalizesLegacyQuotedHash() {
		Map<String, String> legacyHash = new LinkedHashMap<>();
		legacyHash.put("\"userId\"", "\"" + TestConstants.USER_ID + "\"");
		legacyHash.put("\"ipAddress\"", "\"192.168.1.1\"");
		legacyHash.put("\"rememberMe\"", "\"false\"");

		UserSessionIndex session = codec.toSessionIndex(TestConstants.JTI, legacyHash, null);

		assertThat(session.getUserId()).isEqualTo(TestConstants.USER_ID);
		assertThat(session.getIpAddress()).isEqualTo("192.168.1.1");
		assertThat(session.getRememberMe()).isFalse();
	}

	@Test
	@DisplayName("buildRegisterScriptArgs：生成纯字符串 ARGV 供 sessionRedisTemplate 执行")
	void buildRegisterScriptArgs_returnsStringArgv() {
		UserSessionIndex index = new UserSessionIndex();
		index.setUserId(TestConstants.USER_ID);
		index.setSessionId(TestConstants.JTI);
		index.setIpAddress("10.0.0.1");
		index.setLoginAt(1_700_000_000_000L);
		index.setRememberMe(true);

		Object[] args = codec.buildRegisterScriptArgs(index, TestConstants.JTI, 1_700_000_000_000D,
				java.time.Duration.ofHours(1));

		assertThat(args).hasSizeGreaterThanOrEqualTo(6);
		assertThat(args[0]).isEqualTo(TestConstants.JTI);
		assertThat(args[1]).isEqualTo("1700000000000");
		assertThat(args[2]).isEqualTo("3600");
		assertThat(args[3]).isEqualTo(String.valueOf(TestConstants.USER_ID));
		assertThat(args[4]).isInstanceOf(String.class);
	}

}
