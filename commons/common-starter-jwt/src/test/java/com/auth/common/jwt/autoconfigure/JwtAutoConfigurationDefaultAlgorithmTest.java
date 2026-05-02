package com.auth.common.jwt.autoconfigure;

import com.auth.common.jwt.api.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtAutoConfigurationDefaultAlgorithmTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(JwtAutoConfiguration.class));

	@Test
	@DisplayName("未显式配置 algorithm 时默认走 HS256 并创建 JwtTokenProvider")
	void defaultAlgorithmCreatesProvider() {
		this.contextRunner
			.withPropertyValues("auth.common.jwt.issuer=auto-iss",
					"auth.common.jwt.secret=0123456789abcdef0123456789abcdef0123456789abcdef0123456789ab",
					"auth.common.jwt.access-expired=3600", "auth.common.jwt.refresh-expired=7200")
			.run(ctx -> {
				JwtTokenProvider provider = ctx.getBean(JwtTokenProvider.class);
				assertNotNull(provider);
			});
	}

}
