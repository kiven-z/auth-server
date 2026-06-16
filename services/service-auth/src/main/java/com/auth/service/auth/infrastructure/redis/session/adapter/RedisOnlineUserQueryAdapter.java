package com.auth.service.auth.infrastructure.redis.session.adapter;

import com.auth.service.auth.application.port.session.OnlineUserQueryPort;
import com.auth.service.auth.domain.session.OnlineUserPageSlice;
import com.auth.service.auth.infrastructure.redis.session.RedisOnlineUserIndexReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * 在线用户索引查询 Port 适配器
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Repository
public class RedisOnlineUserQueryAdapter implements OnlineUserQueryPort {

	private final RedisOnlineUserIndexReader redisOnlineUserIndexReader;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OnlineUserPageSlice pageOnlineUsers(int pageIndex, int pageSize, Set<Long> allowedUserIds) {
		return redisOnlineUserIndexReader.pageOnlineUsers(pageIndex, pageSize, allowedUserIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long countOnlineUsers() {
		return redisOnlineUserIndexReader.countOnlineUsers();
	}

}
