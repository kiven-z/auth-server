package com.auth.service.auth.application.authorization.query;

import com.auth.common.data.model.PageResponse;
import com.auth.service.auth.api.dto.authorization.request.AuthorizationInvalidationEventQuery;
import com.auth.service.auth.api.dto.authorization.response.AuthorizationInvalidationEventDetailVO;
import com.auth.service.auth.api.dto.authorization.response.AuthorizationInvalidationEventPageVO;
import com.auth.service.auth.api.dto.authorization.response.AuthorizationInvalidationEventStatsVO;
import com.auth.service.auth.convert.AuthorizationInvalidationEventConverter;
import com.auth.service.auth.infrastructure.persistence.filter.InvalidationEventPageFilter;
import com.auth.service.auth.infrastructure.persistence.mapper.AuthorizationInvalidationEventMapper;
import com.auth.service.auth.infrastructure.persistence.projection.AuthorizationInvalidationEventDetailRow;
import com.auth.service.auth.infrastructure.persistence.projection.AuthorizationInvalidationEventPageRow;
import com.auth.service.auth.infrastructure.persistence.projection.AuthorizationInvalidationEventStatsRow;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.AuthResultCode;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 授权失效幂等事件 SQL 只读查询服务
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AuthorizationInvalidationEventQueryService {

	private final AuthorizationInvalidationEventMapper invalidationEventMapper;

	private static InvalidationEventPageFilter toFilter(AuthorizationInvalidationEventQuery query) {
		InvalidationEventPageFilter filter = new InvalidationEventPageFilter();
		filter.setEventId(query.getEventId());
		filter.setChangeKind(query.getChangeKind());
		filter.setProcessing(query.getProcessing());
		filter.setProcessedAtStart(query.getProcessedAtStart());
		filter.setProcessedAtEnd(query.getProcessedAtEnd());
		return filter;
	}

	private static AuthorizationInvalidationEventStatsVO toStatsVo(AuthorizationInvalidationEventStatsRow row) {
		if (row == null) {
			AuthorizationInvalidationEventStatsVO statsVo = new AuthorizationInvalidationEventStatsVO();
			statsVo.setTotalCount(0L);
			statsVo.setProcessingCount(0L);
			statsVo.setCompletedCount(0L);
			return statsVo;
		}
		AuthorizationInvalidationEventStatsVO statsVo = AuthorizationInvalidationEventConverter.INSTANCE.toStatsVo(row);
		if (statsVo.getTotalCount() == null) {
			statsVo.setTotalCount(0L);
		}
		if (statsVo.getProcessingCount() == null) {
			statsVo.setProcessingCount(0L);
		}
		if (statsVo.getCompletedCount() == null) {
			statsVo.setCompletedCount(0L);
		}
		return statsVo;
	}

	/**
	 * 分页查询幂等事件
	 * @param query HTTP 查询条件
	 * @return 分页 VO
	 */
	public PageResponse<AuthorizationInvalidationEventPageVO> getPage(AuthorizationInvalidationEventQuery query) {
		Page<AuthorizationInvalidationEventPageRow> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		InvalidationEventPageFilter filter = toFilter(query);

		IPage<AuthorizationInvalidationEventPageRow> page = invalidationEventMapper.selectListByPage(pageParams,
				filter);

		IPage<AuthorizationInvalidationEventPageVO> voPage = page
			.convert(AuthorizationInvalidationEventConverter.INSTANCE::toPageVO);
		return PageResponse.of(voPage);
	}

	/**
	 * 查询幂等事件详情
	 * @param id 主键
	 * @return 详情 VO
	 */
	public AuthorizationInvalidationEventDetailVO getDetail(Long id) {
		AuthorizationInvalidationEventDetailRow row = invalidationEventMapper.selectDetailById(id);
		if (row == null) {
			throw new AuthBusinessException(AuthResultCode.DATA_NOT_EXIST);
		}

		return AuthorizationInvalidationEventConverter.INSTANCE.toDetailVo(row);
	}

	/**
	 * 查询幂等事件统计
	 * @return 统计 VO，无数据时各计数归零
	 */
	public AuthorizationInvalidationEventStatsVO getStats() {
		AuthorizationInvalidationEventStatsRow row = invalidationEventMapper.selectEventStats();
		return toStatsVo(row);
	}

}
