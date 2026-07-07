package com.auth.common.web.validation;

import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for shared validation regular expressions.
 */
class ValidationPatternsTest {

	private final Pattern letterUnderscoreHyphen = Pattern
		.compile(ValidationPatterns.LETTER_UNDERSCORE_HYPHEN_REQUIRE_LETTER);

	private final Pattern upperAlnumUnderscore = Pattern.compile(ValidationPatterns.UPPER_ALNUM_UNDERSCORE);

	@Test
	@DisplayName("LETTER_UNDERSCORE_HYPHEN_REQUIRE_LETTER accepts letters with optional underscore or hyphen")
	void letterUnderscoreHyphenAcceptsValidCodes() {
		// 合法：含字母，且仅含字母、下划线、中划线
		assertTrue(letterUnderscoreHyphen.matcher("ab").matches());
		assertTrue(letterUnderscoreHyphen.matcher("a_b").matches());
		assertTrue(letterUnderscoreHyphen.matcher("a-b").matches());
	}

	@Test
	@DisplayName("LETTER_UNDERSCORE_HYPHEN_REQUIRE_LETTER rejects pure digits or strings without letters")
	void letterUnderscoreHyphenRejectsInvalidCodes() {
		assertFalse(letterUnderscoreHyphen.matcher("123").matches());
		assertFalse(letterUnderscoreHyphen.matcher("___").matches());
		assertFalse(letterUnderscoreHyphen.matcher("a1").matches());
	}

	@Test
	@DisplayName("UPPER_ALNUM_UNDERSCORE accepts uppercase codes only")
	void upperAlnumUnderscoreAcceptsJobStyleCodes() {
		assertTrue(upperAlnumUnderscore.matcher("SYS_JOB").matches());
		assertTrue(upperAlnumUnderscore.matcher("A1").matches());
	}

	@Test
	@DisplayName("UPPER_ALNUM_UNDERSCORE rejects lowercase or special characters")
	void upperAlnumUnderscoreRejectsInvalidCodes() {
		assertFalse(upperAlnumUnderscore.matcher("job").matches());
		assertFalse(upperAlnumUnderscore.matcher("A-B").matches());
	}

}
