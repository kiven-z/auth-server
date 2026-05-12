package com.auth.common.data.model;

import com.auth.common.data.model.enums.DatabaseAuditMetaFieldEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseAuditMetaFieldEnumTest {

	private static final String CREATE_TIME = "createTime";

	@Test
	void getColumnName() {
		DatabaseAuditMetaFieldEnum createTime = DatabaseAuditMetaFieldEnum.CREATE_TIME;
		boolean condition = createTime.getColumnName().equals(CREATE_TIME);
		assertTrue(condition);
	}

}