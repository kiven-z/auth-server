package com.auth.service.system.authorization.application.query;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.authorization.outbox.convert.AuthorizationInvalidationOutboxConverter;
import com.auth.service.system.authorization.outbox.model.po.AuthorizationInvalidationOutboxDetailRowPO;
import com.auth.service.system.authorization.outbox.model.po.AuthorizationInvalidationOutboxPageRowPO;
import com.auth.service.system.authorization.outbox.model.query.AuthorizationInvalidationOutboxQuery;
import com.auth.service.system.authorization.outbox.model.vo.AuthorizationInvalidationOutboxDetailVO;
import com.auth.service.system.authorization.outbox.model.vo.AuthorizationInvalidationOutboxPageVO;
import com.auth.service.system.authorization.outbox.port.InvalidationOutboxQueryPort;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 授权失效 Outbox 只读查询应用服务
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AuthorizationInvalidationOutboxQueryApplicationService {

	private final InvalidationOutboxQueryPort invalidationOutboxQueryPort;

	/**
	 * 分页查询 Outbox
	 * @param query 查询条件
	 * @return 分页结果
	 */
	public PageResponse<AuthorizationInvalidationOutboxPageVO> getPage(AuthorizationInvalidationOutboxQuery query) {
		Page<AuthorizationInvalidationOutboxPageRowPO> pageParams = new Page<>(query.getPageIndex(),
				query.getPageSize());
		IPage<AuthorizationInvalidationOutboxPageRowPO> page = invalidationOutboxQueryPort.selectPage(pageParams,
				query);
		IPage<AuthorizationInvalidationOutboxPageVO> voPage = page
			.convert(AuthorizationInvalidationOutboxConverter.INSTANCE::toPageVO);
		return PageResponse.of(voPage);
	}

	/**
	 * 查询 Outbox 详情
	 * @param id 主键
	 * @return 详情
	 */
	public AuthorizationInvalidationOutboxDetailVO getDetail(Long id) {
		AuthorizationInvalidationOutboxDetailRowPO detailRow = invalidationOutboxQueryPort.selectDetailById(id);
		if (detailRow == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		return AuthorizationInvalidationOutboxConverter.INSTANCE.toDetailVo(detailRow);
	}

}
