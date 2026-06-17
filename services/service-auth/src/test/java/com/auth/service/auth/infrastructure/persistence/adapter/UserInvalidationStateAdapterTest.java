package com.auth.service.auth.infrastructure.persistence.adapter;

import com.auth.service.auth.config.AuthorizationInvalidationProperties;
import com.auth.service.auth.domain.authorization.model.UserInvalidationState;
import com.auth.service.auth.infrastructure.persistence.mapper.UserMapper;
import com.auth.service.auth.infrastructure.persistence.mapper.UserPermVersionMapper;
import com.auth.service.auth.infrastructure.persistence.projection.UserInvalidationStateProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * {@link UserInvalidationStateAdapter} 单元测试
 */
@DisplayName("UserInvalidationStateAdapter 用户失效状态")
@ExtendWith(MockitoExtension.class)
class UserInvalidationStateAdapterTest {

	@Mock
	private UserMapper userMapper;

	@Mock
	private UserPermVersionMapper userPermVersionMapper;

	private UserInvalidationStateAdapter adapter;

	@BeforeEach
	void setUp() {
		AuthorizationInvalidationProperties invalidationProperties = new AuthorizationInvalidationProperties();
		invalidationProperties.setBatchSize(500);
		adapter = new UserInvalidationStateAdapter(userMapper, userPermVersionMapper, invalidationProperties);
	}

	@Test
	@DisplayName("用户 ID 超过分片大小时应分批加载失效状态")
	void loadByUserIds_largeInput_shouldQueryInChunks() {
		List<Long> userIds = IntStream.rangeClosed(1, 1200).mapToObj(Long::valueOf).toList();
		when(userMapper.selectInvalidationStatesByUserIds(anyList())).thenAnswer(invocation -> {
			List<Long> chunk = invocation.getArgument(0);
			UserInvalidationStateProjection projection = new UserInvalidationStateProjection();
			projection.setUserId(chunk.get(0));
			projection.setStatus(1);
			projection.setIsDeleted(0);
			return List.of(projection);
		});

		List<UserInvalidationState> states = adapter.loadByUserIds(userIds);

		assertEquals(3, states.size());
		verify(userMapper, times(3)).selectInvalidationStatesByUserIds(anyList());
	}

}
