package com.auth.service.system.log.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.log.convert.LogAuthorizationAuditConverter;
import com.auth.service.system.log.mapper.LogAuthorizationAuditMapper;
import com.auth.service.system.log.model.entity.LogAuthorizationAuditEntity;
import com.auth.service.system.log.model.po.LogAuthorizationAuditDetailRowPO;
import com.auth.service.system.log.model.po.LogAuthorizationAuditPageRowPO;
import com.auth.service.system.log.model.query.LogAuthorizationAuditQuery;
import com.auth.service.system.log.model.vo.LogAuthorizationAuditDetailVO;
import com.auth.service.system.log.model.vo.LogAuthorizationAuditPageVO;
import com.auth.service.system.log.service.LogAuthorizationAuditService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 权限决策审计日志 Service 实现
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class LogAuthorizationAuditServiceImpl extends
		ServiceImpl<LogAuthorizationAuditMapper, LogAuthorizationAuditEntity> implements LogAuthorizationAuditService {

	private final AuditUserDisplayService auditUserDisplayService;

	public LogAuthorizationAuditServiceImpl(AuditUserDisplayService auditUserDisplayService) {
		this.auditUserDisplayService = auditUserDisplayService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<LogAuthorizationAuditPageVO> getPage(Page<LogAuthorizationAuditEntity> pageParams,
			LogAuthorizationAuditQuery query) {
		IPage<LogAuthorizationAuditPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		IPage<LogAuthorizationAuditPageVO> voPage = page.convert(LogAuthorizationAuditConverter.INSTANCE::toPageVO);

		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LogAuthorizationAuditDetailVO getDetail(Long id) {
		LogAuthorizationAuditDetailRowPO po = baseMapper.selectDetailById(id);
		if (po == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		LogAuthorizationAuditDetailVO vo = LogAuthorizationAuditConverter.INSTANCE.toDetailVo(po);
		auditUserDisplayService.enrichAuditUsernames(List.of(vo));
		return vo;
	}

}
