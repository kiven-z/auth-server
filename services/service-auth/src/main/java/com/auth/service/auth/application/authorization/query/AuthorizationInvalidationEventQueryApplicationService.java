package com.auth.service.auth.application.authorization.query;

import com.auth.common.data.model.PageResponse;
import com.auth.service.auth.application.port.invalidation.InvalidationEventQueryPort;
import com.auth.service.auth.convert.AuthorizationInvalidationEventConverter;
import com.auth.service.auth.domain.authorization.model.po.AuthorizationInvalidationEventDetailRowPO;
import com.auth.service.auth.domain.authorization.model.po.AuthorizationInvalidationEventPageRowPO;
import com.auth.service.auth.domain.authorization.model.po.AuthorizationInvalidationEventStatsPO;
import com.auth.service.auth.domain.authorization.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.auth.domain.authorization.model.vo.AuthorizationInvalidationEventDetailVO;
import com.auth.service.auth.domain.authorization.model.vo.AuthorizationInvalidationEventPageVO;
import com.auth.service.auth.domain.authorization.model.vo.AuthorizationInvalidationEventStatsVO;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 授权失效幂等事件只读查询应用服务
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AuthorizationInvalidationEventQueryApplicationService {

	private final InvalidationEventQueryPort invalidationEventQueryPort;

	/**
	 * 分页查询幂等事件
	 * @param query 查询条件
	 * @return 分页结果
	 */
	public PageResponse<AuthorizationInvalidationEventPageVO> getPage(AuthorizationInvalidationEventQuery query) {
		Page<AuthorizationInvalidationEventPageRowPO> pageParams = new Page<>(query.getPageIndex(),
				query.getPageSize());
		IPage<AuthorizationInvalidationEventPageRowPO> page = invalidationEventQueryPort.selectPage(pageParams, query);
		IPage<AuthorizationInvalidationEventPageVO> voPage = page
			.convert(AuthorizationInvalidationEventConverter.INSTANCE::toPageVO);
		return PageResponse.of(voPage);
	}

	/**
	 * 查询幂等事件详情
	 * @param id 主键
	 * @return 详情
	 */
	public AuthorizationInvalidationEventDetailVO getDetail(Long id) {
		AuthorizationInvalidationEventDetailRowPO detailRow = invalidationEventQueryPort.selectDetailById(id);
		if (detailRow == null) {
			throw new AuthBusinessException(AuthResultCode.DATA_NOT_EXIST);
		}
		return AuthorizationInvalidationEventConverter.INSTANCE.toDetailVo(detailRow);
	}

	/**
	 * 查询幂等事件统计
	 * @return 统计摘要
	 */
	public AuthorizationInvalidationEventStatsVO getStats() {
		AuthorizationInvalidationEventStatsPO statsPo = invalidationEventQueryPort.selectEventStats();
		AuthorizationInvalidationEventStatsVO statsVo = new AuthorizationInvalidationEventStatsVO();
		if (statsPo == null) {
			statsVo.setTotalCount(0L);
			statsVo.setProcessingCount(0L);
			statsVo.setCompletedCount(0L);
			return statsVo;
		}

		statsVo.setTotalCount(Objects.requireNonNullElse(statsPo.getTotalCount(), 0L));
		statsVo.setProcessingCount(Objects.requireNonNullElse(statsPo.getProcessingCount(), 0L));
		statsVo.setCompletedCount(Objects.requireNonNullElse(statsPo.getCompletedCount(), 0L));
		return statsVo;
	}

}
