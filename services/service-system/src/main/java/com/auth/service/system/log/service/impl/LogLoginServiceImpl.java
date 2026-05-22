package com.auth.service.system.log.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.log.convert.LogLoginLogConverter;
import com.auth.service.system.log.mapper.LogLoginLogMapper;
import com.auth.service.system.log.model.po.LogLoginLogPageRowPO;
import com.auth.service.system.log.model.query.LogLoginLogQuery;
import com.auth.service.system.log.model.vo.LogLoginLogDetailVO;
import com.auth.service.system.log.model.vo.LogLoginLogPageVO;
import com.auth.service.system.log.service.LogLoginService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * ???? Service ??
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class LogLoginServiceImpl extends ServiceImpl<LogLoginLogMapper, LoginLogEntity> implements LogLoginService {

	private final AuditUserDisplayService auditUserDisplayService;

	public LogLoginServiceImpl(AuditUserDisplayService auditUserDisplayService) {
		this.auditUserDisplayService = auditUserDisplayService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<LogLoginLogPageVO> getPage(Page<LoginLogEntity> pageParams, LogLoginLogQuery query) {
		IPage<LogLoginLogPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		// PO ? VO?PageResponse.of ?? records?
		IPage<LogLoginLogPageVO> voPage = page.convert(LogLoginLogConverter.INSTANCE::toPageVO);
		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LogLoginLogDetailVO getDetail(Long id) {
		LoginLogEntity entity = getById(id);
		if (entity == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		return LogLoginLogConverter.INSTANCE.toDetailVo(entity);
	}

}
