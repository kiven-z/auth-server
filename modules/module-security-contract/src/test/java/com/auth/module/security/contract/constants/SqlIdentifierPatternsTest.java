package com.auth.module.security.contract.constants;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for SQL identifier validation pattern.
 */
class SqlIdentifierPatternsTest {

	private final Pattern safeIdentifier = Pattern.compile(SqlIdentifierPatterns.SAFE_IDENTIFIER);

	@Test
	@DisplayName("SAFE_IDENTIFIER accepts letters, underscore, and trailing word chars")
	void acceptsValidIdentifiers() {
		assertTrue(safeIdentifier.matcher("t").matches());
		assertTrue(safeIdentifier.matcher("_x").matches());
		assertTrue(safeIdentifier.matcher("org_id").matches());
	}

	@Test
	@DisplayName("SAFE_IDENTIFIER rejects empty strings and invalid tokens")
	void rejectsInvalidIdentifiers() {
		assertFalse(safeIdentifier.matcher("").matches());
		assertFalse(safeIdentifier.matcher("1a").matches());
		assertFalse(safeIdentifier.matcher("a-b").matches());
		assertFalse(safeIdentifier.matcher("a.b").matches());
	}

}
