package com.auth.service.system.log.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.log.convert.LogLoginLogConvert;
import com.auth.service.system.log.mapper.LogLoginLogMapper;
import com.auth.service.system.log.model.entity.LogLoginLogEntity;
import com.auth.service.system.log.model.po.LogLoginLogDetailRowPO;
import com.auth.service.system.log.model.po.LogLoginLogPageRowPO;
import com.auth.service.system.log.model.query.LogLoginLogQuery;
import com.auth.service.system.log.model.vo.LogLoginLogDetailVO;
import com.auth.service.system.log.model.vo.LogLoginLogPageVO;
import com.auth.service.system.log.service.LogLoginLogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录日志 Service 实现
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class LogLoginLogServiceImpl extends ServiceImpl<LogLoginLogMapper, LogLoginLogEntity>
		implements LogLoginLogService {

	private final AuditUserDisplayService auditUserDisplayService;

	public LogLoginLogServiceImpl(AuditUserDisplayService auditUserDisplayService) {
		this.auditUserDisplayService = auditUserDisplayService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<LogLoginLogPageVO> getPage(Page<LogLoginLogEntity> pageParams, LogLoginLogQuery query) {
		IPage<LogLoginLogPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		// PO 转 VO（PageResponse.of 复用 records）
		IPage<LogLoginLogPageVO> voPage = page.convert(LogLoginLogConvert.INSTANCE::toPageVO);
		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LogLoginLogDetailVO getDetail(Long id) {
		LogLoginLogDetailRowPO po = baseMapper.selectDetailById(id);
		return BeanUtil.copyProperties(po, LogLoginLogDetailVO.class);
	}

}
