package com.auth.module.security.starter.autoconfigure;

import com.auth.module.security.autoconfigure.boot.filter.SecurityStarterAutoConfigurationImportFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityStarterAutoConfigurationImportFilterTest {

	@Test
	@DisplayName("测试排除 UserDetailsServiceAutoConfiguration")
	void match_shouldExcludeUserDetailsServiceAutoConfiguration() {
		SecurityStarterAutoConfigurationImportFilter filter = new SecurityStarterAutoConfigurationImportFilter();

		String[] classes = { "com.example.Foo",
				"org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration",
				"com.example.Bar" };

		boolean[] matches = filter.match(classes, null);

		assertNotNull(matches);
		assertTrue(matches[0]);
		assertFalse(matches[1]);
		assertTrue(matches[2]);
	}

	@Test
	@DisplayName("测试空条目应视为匹配为真")
	void match_nullEntries_shouldBeTreatedAsMatchTrue() {
		SecurityStarterAutoConfigurationImportFilter filter = new SecurityStarterAutoConfigurationImportFilter();
		String[] classes = { null };
		boolean[] matches = filter.match(classes, null);
		assertTrue(matches[0]);
	}

}
