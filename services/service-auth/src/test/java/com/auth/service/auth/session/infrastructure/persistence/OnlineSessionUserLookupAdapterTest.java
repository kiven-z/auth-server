package com.auth.service.auth.session.infrastructure.persistence;

import com.auth.service.auth.TestConstants;
import com.auth.service.auth.authentication.infrastructure.persistence.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * {@link OnlineSessionUserLookupAdapter} 单元测试
 */
@DisplayName("OnlineSessionUserLookupAdapter 在线用户展示查询")
@ExtendWith(MockitoExtension.class)
class OnlineSessionUserLookupAdapterTest {

	@Mock
	private UserMapper userMapper;

	@InjectMocks
	private OnlineSessionUserLookupAdapter onlineSessionUserLookupAdapter;

	@Test
	@DisplayName("findBriefByIds：空集合不访问数据库")
	void findBriefByIds_shouldSkipDbWhenEmpty() {
		assertTrue(onlineSessionUserLookupAdapter.findBriefByIds(List.of()).isEmpty());
		verifyNoInteractions(userMapper);
	}

	@Test
	@DisplayName("findUserIdsByUsernameLike：委托 Mapper 模糊查询")
	void findUserIdsByUsernameLike_shouldDelegateToMapper() {
		when(userMapper.selectIdsByUsernameLike("ali")).thenReturn(List.of(TestConstants.USER_ID));

		List<Long> ids = onlineSessionUserLookupAdapter.findUserIdsByUsernameLike("ali");

		assertEquals(List.of(TestConstants.USER_ID), ids);
		verify(userMapper).selectIdsByUsernameLike("ali");
	}

}
