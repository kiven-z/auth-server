package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.service.system.admin.convert.LogLoginLogConverter;
import com.auth.service.system.admin.mapper.LogLoginLogMapper;
import com.auth.service.system.admin.model.po.loglogin.LogLoginLogPageRowPO;
import com.auth.service.system.admin.model.query.LogLoginLogQuery;
import com.auth.service.system.admin.model.vo.loglogin.LogLoginLogDetailVO;
import com.auth.service.system.admin.model.vo.loglogin.LogLoginLogPageVO;
import com.auth.service.system.admin.service.LogLoginService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 登录日志 Service 实现
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
		IPage<LogLoginLogPageVO> voPage = page.convert(LogLoginLogConverter.INSTANCE::toPageVO);

		// 解析用户名
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
