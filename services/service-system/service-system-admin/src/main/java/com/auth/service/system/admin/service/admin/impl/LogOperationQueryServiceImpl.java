package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.admin.log.LogOperationConverter;
import com.auth.service.system.admin.mapper.log.LogOperationMapper;
import com.auth.service.system.admin.model.entity.LogOperationEntity;
import com.auth.service.system.admin.model.po.logoperation.LogOperationPageRowPO;
import com.auth.service.system.admin.model.query.log.LogOperationQuery;
import com.auth.service.system.admin.model.vo.logoperation.LogOperationDetailVO;
import com.auth.service.system.admin.model.vo.logoperation.LogOperationPageVO;
import com.auth.service.system.admin.service.admin.LogOperationQueryService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 操作日志只读查询实现。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class LogOperationQueryServiceImpl extends ServiceImpl<LogOperationMapper, LogOperationEntity>
		implements LogOperationQueryService {

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<LogOperationPageVO> getPage(Page<LogOperationEntity> pageParams, LogOperationQuery query) {
		IPage<LogOperationPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		IPage<LogOperationPageVO> voPage = page.convert(LogOperationConverter.INSTANCE::toPageVO);

		auditUserDisplayService.enrichAuditUsernames(voPage, vo -> Objects.requireNonNullElse(vo.getUserId(), 0L),
				LogOperationPageVO::setUsername);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LogOperationDetailVO getDetail(Long id) {
		LogOperationEntity entity = getById(id);
		if (entity == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		LogOperationDetailVO vo = LogOperationConverter.INSTANCE.toDetailVo(entity);
		auditUserDisplayService.enrichAuditUsernames(List.of(vo),
				detail -> Objects.requireNonNullElse(detail.getUserId(), 0L), LogOperationDetailVO::setUsername);
		return vo;
	}

}
