package com.auth.service.system.binarystatus;

import com.auth.service.system.admin.model.form.role.SysRoleCreateForm;
import com.auth.service.system.admin.model.query.EmailTemplateQuery;
import com.auth.service.system.admin.model.query.SysRoleQuery;
import com.auth.service.system.schedule.model.query.SysJobGroupQuery;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 校验分页/表单查询对象中二元 status 与 JSON {@code true}/{@code false} 的对齐。
 */
@DisplayName("二元 Boolean status JSON 绑定")
class BooleanStatusJsonDeserializationTest {

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Test
	@DisplayName("SysJobGroupQuery 支持 status 布尔反序列化")
	void sysJobGroupQueryAcceptsBooleanStatus() throws Exception {
		SysJobGroupQuery query = objectMapper.readValue("{\"status\":true}", SysJobGroupQuery.class);
		assertThat(query.getStatus()).isTrue();

		query = objectMapper.readValue("{\"status\":false}", SysJobGroupQuery.class);
		assertThat(query.getStatus()).isFalse();
	}

	@Test
	@DisplayName("SysRoleQuery 支持 status 布尔反序列化")
	void sysRoleQueryAcceptsBooleanStatus() throws Exception {
		SysRoleQuery query = objectMapper.readValue("{\"status\":true}", SysRoleQuery.class);
		assertThat(query.getStatus()).isTrue();
	}

	@Test
	@DisplayName("SysRoleCreateForm 必填 status 布尔")
	void sysRoleCreateFormAcceptsBooleanStatus() throws Exception {
		SysRoleCreateForm form = objectMapper.readValue(
				"{\"roleCode\":\"R_DEMO\",\"roleName\":\"演示\",\"roleType\":4,\"status\":true}",
				SysRoleCreateForm.class);
		assertThat(form.getStatus()).isTrue();
	}

	@Test
	@DisplayName("EmailTemplateQuery 支持 status 布尔反序列化")
	void emailTemplateQueryAcceptsBooleanStatus() throws Exception {
		EmailTemplateQuery query = objectMapper.readValue("{\"status\":false}", EmailTemplateQuery.class);
		assertThat(query.getStatus()).isFalse();
	}

}
