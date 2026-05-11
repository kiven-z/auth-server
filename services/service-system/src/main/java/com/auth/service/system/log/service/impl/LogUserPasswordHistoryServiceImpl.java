package com.auth.service.system.log.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.log.convert.LogUserPasswordHistoryConvert;
import com.auth.service.system.log.mapper.LogUserPasswordHistoryMapper;
import com.auth.service.system.log.model.entity.LogUserPasswordHistoryEntity;
import com.auth.service.system.log.model.po.LogUserPasswordHistoryDetailRowPO;
import com.auth.service.system.log.model.po.LogUserPasswordHistoryPageRowPO;
import com.auth.service.system.log.model.query.LogUserPasswordHistoryQuery;
import com.auth.service.system.log.model.vo.LogUserPasswordHistoryDetailVO;
import com.auth.service.system.log.model.vo.LogUserPasswordHistoryPageVO;
import com.auth.service.system.log.service.LogUserPasswordHistoryService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 密码历史日志 Service 实现
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class LogUserPasswordHistoryServiceImpl
		extends ServiceImpl<LogUserPasswordHistoryMapper, LogUserPasswordHistoryEntity>
		implements LogUserPasswordHistoryService {

	private final AuditUserDisplayService auditUserDisplayService;

	public LogUserPasswordHistoryServiceImpl(AuditUserDisplayService auditUserDisplayService) {
		this.auditUserDisplayService = auditUserDisplayService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<LogUserPasswordHistoryPageVO> getPage(Page<LogUserPasswordHistoryEntity> pageParams,
			LogUserPasswordHistoryQuery query) {
		IPage<LogUserPasswordHistoryPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		// PO 转 VO
		IPage<LogUserPasswordHistoryPageVO> voPage = page.convert(LogUserPasswordHistoryConvert.INSTANCE::toPageVO);
		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LogUserPasswordHistoryDetailVO getDetail(Long id) {
		LogUserPasswordHistoryDetailRowPO po = baseMapper.selectDetailById(id);
		return BeanUtil.copyProperties(po, LogUserPasswordHistoryDetailVO.class);
	}

}
