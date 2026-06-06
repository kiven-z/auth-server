package com.auth.service.auth.authorization.application.query;

import com.auth.common.data.model.PageResponse;
import com.auth.service.auth.authorization.admin.convert.AuthorizationInvalidationEventConverter;
import com.auth.service.auth.authorization.admin.model.po.AuthorizationInvalidationEventDetailRowPO;
import com.auth.service.auth.authorization.admin.model.po.AuthorizationInvalidationEventPageRowPO;
import com.auth.service.auth.authorization.admin.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.auth.authorization.admin.model.vo.AuthorizationInvalidationEventDetailVO;
import com.auth.service.auth.authorization.admin.model.vo.AuthorizationInvalidationEventPageVO;
import com.auth.service.auth.authorization.domain.invalidation.port.InvalidationEventQueryPort;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

}
