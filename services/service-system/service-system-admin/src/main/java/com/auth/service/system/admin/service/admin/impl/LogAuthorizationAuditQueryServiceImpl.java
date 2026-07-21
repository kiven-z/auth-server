package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.admin.log.LogAuthorizationAuditConverter;
import com.auth.service.system.admin.mapper.log.LogAuthorizationAuditMapper;
import com.auth.service.system.admin.model.entity.LogAuthorizationAuditEntity;
import com.auth.service.system.admin.model.po.logauthorizationaudit.LogAuthorizationAuditDetailRowPO;
import com.auth.service.system.admin.model.po.logauthorizationaudit.LogAuthorizationAuditPageRowPO;
import com.auth.service.system.admin.model.query.log.LogAuthorizationAuditQuery;
import com.auth.service.system.admin.model.vo.logauthorizationaudit.LogAuthorizationAuditDetailVO;
import com.auth.service.system.admin.model.vo.logauthorizationaudit.LogAuthorizationAuditPageVO;
import com.auth.service.system.admin.service.admin.LogAuthorizationAuditQueryService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 权限决策审计日志只读查询实现。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class LogAuthorizationAuditQueryServiceImpl
		extends ServiceImpl<LogAuthorizationAuditMapper, LogAuthorizationAuditEntity>
		implements LogAuthorizationAuditQueryService {

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<LogAuthorizationAuditPageVO> getPage(Page<LogAuthorizationAuditEntity> pageParams,
			LogAuthorizationAuditQuery query) {
		IPage<LogAuthorizationAuditPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		IPage<LogAuthorizationAuditPageVO> voPage = page.convert(LogAuthorizationAuditConverter.INSTANCE::toPageVO);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public LogAuthorizationAuditDetailVO getDetail(Long id) {
		LogAuthorizationAuditDetailRowPO po = baseMapper.selectDetailById(id);
		if (po == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		LogAuthorizationAuditDetailVO vo = LogAuthorizationAuditConverter.INSTANCE.toDetailVo(po);
		auditUserDisplayService.enrichAuditUsernames(List.of(vo), null, null);
		return vo;
	}

}
