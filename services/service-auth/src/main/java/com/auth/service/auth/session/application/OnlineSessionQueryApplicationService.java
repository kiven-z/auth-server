package com.auth.service.auth.session.application;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.session.application.assembler.OnlineSessionVoAssembler;
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
		redisUserSessionIndexRepository.cleanupStaleGlobalSessions(PRE_QUERY_CLEANUP_BATCH);

		Set<Long> allowedUserIds = resolveAllowedUserIds(query.getUsername());
		if (allowedUserIds != null && allowedUserIds.isEmpty()) {
			return emptyPage(query);
		}

		int pageIndex = Objects.requireNonNullElse(query.getPageIndex(), 1);
		int pageSize = Objects.requireNonNullElse(query.getPageSize(), 30);
		OnlineSessionPageSlice slice = redisUserSessionIndexRepository.pageGlobalSessions(pageIndex, pageSize,
				allowedUserIds, query.getIpAddress());
		List<OnlineSessionPageVO> records = assemblePage(slice);
		return PageResponse.of((long) pageIndex, (long) pageSize, slice.total(), records);
	}

	/**
	 * 解析用户名筛选对应的用户 ID 集合
	 * @param username 用户名关键字
	 * @return {@code null} 表示不过滤；空集合表示无匹配用户
	 */
	private Set<Long> resolveAllowedUserIds(String username) {
		if (CharSequenceUtil.isBlank(username)) {
			return null;
		}
		return Set.copyOf(onlineSessionUserLookupPort.findUserIdsByUsernameLike(username));
	}

	/**
	 * 组装分页 VO 列表
	 * @param slice 会话分页切片
	 * @return VO 列表
	 */
	private List<OnlineSessionPageVO> assemblePage(OnlineSessionPageSlice slice) {
		Set<Long> userIds = slice.sessions()
			.stream()
			.map(UserSessionIndex::getUserId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		Map<Long, UserOnlineBrief> briefMap = onlineSessionUserLookupPort.findBriefByIds(userIds);
		return slice.sessions()
			.stream()
			.map(session -> OnlineSessionVoAssembler.toPageVo(session, briefMap.get(session.getUserId())))
			.toList();
	}

	/**
	 * 构建空分页结果
	 * @param query 查询条件
	 * @return 空分页
	 */
	private PageResponse<OnlineSessionPageVO> emptyPage(OnlineSessionPageQuery query) {
		long pageIndex = Objects.requireNonNullElse(query.getPageIndex(), 1);
		long pageSize = Objects.requireNonNullElse(query.getPageSize(), 30);
		return PageResponse.of(pageIndex, pageSize, 0L, List.of());
	}

}
