package com.auth.service.auth.convert;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.model.vo.UserSessionVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link UserSessionConverter} 单元测试
 */
class UserSessionConverterTest {

	@Test
	@DisplayName("会话索引转 VO：字段完整且不含 refreshTokenHash")
	void toVo_shouldMapFieldsWithoutRefreshTokenHash() {
		UserSessionIndex index = new UserSessionIndex();
		index.setUserId(TestConstants.USER_ID);
		index.setSessionId(TestConstants.JTI);
		index.setIpAddress("127.0.0.1");
		index.setRefreshTokenHash("secret-hash");

		UserSessionVO vo = UserSessionConverter.INSTANCE.toVo(index);

		assertEquals(TestConstants.USER_ID, vo.getUserId());
		assertEquals(TestConstants.JTI, vo.getSessionId());
		assertEquals("127.0.0.1", vo.getIpAddress());
	}

	@Test
	@DisplayName("批量转换：空列表返回空")
	void toVoList_shouldHandleEmptyList() {
		assertEquals(0, UserSessionConverter.INSTANCE.toVoList(java.util.List.of()).size());
	}

}
