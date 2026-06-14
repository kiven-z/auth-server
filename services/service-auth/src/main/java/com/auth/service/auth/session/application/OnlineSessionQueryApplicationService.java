package com.auth.service.auth.session.application;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.session.convert.OnlineSessionPageConverter;
import com.auth.service.auth.session.domain.model.OnlineSessionPageSlice;
import com.auth.service.auth.session.domain.model.UserOnlineBrief;
import com.auth.service.auth.session.domain.model.query.OnlineSessionPageQuery;
import com.auth.service.auth.session.domain.model.vo.OnlineSessionPageVO;
import com.auth.service.auth.session.domain.port.OnlineSessionUserLookupPort;
import com.auth.service.auth.session.infrastructure.redis.RedisUserSessionIndexRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局在线会话只读查询应用服务
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class OnlineSessionQueryApplicationService {

	/**
	 * 列表查询前预清理的僵尸索引批次大小
	 */
	private static final int PRE_QUERY_CLEANUP_BATCH = 100;

	private final RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	private final OnlineSessionUserLookupPort onlineSessionUserLookupPort;

	/**
	 * 分页查询全局在线会话
	 * @param query 查询条件
	 * @return 分页结果
	 */
	public PageResponse<OnlineSessionPageVO> getPage(OnlineSessionPageQuery query) {
		// 清理僵尸索引
		redisUserSessionIndexRepository.cleanupStaleGlobalSessions(PRE_QUERY_CLEANUP_BATCH);

		// 根据用户名筛选用户ID
		Set<Long> allowedUserIds = null;
		if (CharSequenceUtil.isNotBlank(query.getUsername())) {
			allowedUserIds = Set.copyOf(onlineSessionUserLookupPort.findUserIdsByUsernameLike(query.getUsername()));
		}

		// 规范化页码和分页大小
		int pageIndex = Objects.requireNonNullElse(query.getPageIndex(), 1);
		int pageSize = Objects.requireNonNullElse(query.getPageSize(), 30);
		if (CollUtil.isEmpty(allowedUserIds)) {
			return PageResponse.of((long) pageIndex, (long) pageSize, 0L, List.of());
		}

		// 分页查询全局在线会话
		OnlineSessionPageSlice slice = redisUserSessionIndexRepository.pageGlobalSessions(pageIndex, pageSize,
				allowedUserIds, query.getIpAddress());
		Set<Long> userIds = slice.sessions()
			.stream()
			.map(UserSessionIndex::getUserId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		// 查询用户在线简要信息
		Map<Long, UserOnlineBrief> briefMap = onlineSessionUserLookupPort.findBriefByIds(userIds);
		List<OnlineSessionPageVO> records = slice.sessions()
			.stream()
			.map(session -> OnlineSessionPageConverter.INSTANCE.toPageVo(session, briefMap.get(session.getUserId())))
			.toList();
		return PageResponse.of((long) pageIndex, (long) pageSize, slice.total(), records);
	}

}
