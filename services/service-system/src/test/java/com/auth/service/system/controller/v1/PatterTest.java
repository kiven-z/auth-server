package com.auth.service.system.controller.v1;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PatterTest {

	private static final Pattern DANGEROUS_PATTERN = Pattern
		.compile("^\\s*(DROP|DELETE|UPDATE|INSERT|ALTER|CREATE|TRUNCATE)", Pattern.CASE_INSENSITIVE);

	@Test
	void test() {
		String sql = "DROP id,username,nickname,email,phone,password,avatar,status,gender,birthday,introduction,last_login_ip,last_login_region,last_login_time,is_deleted,create_time,update_time,create_user,update_user FROM sys_user WHERE id IN ( 1 ) AND is_deleted=0";
		boolean flag = DANGEROUS_PATTERN.matcher(sql).find();
		assertTrue(flag);
	}

}
