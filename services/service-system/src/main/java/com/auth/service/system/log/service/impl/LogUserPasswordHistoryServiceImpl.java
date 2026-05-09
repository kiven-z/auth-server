package com.auth.service.system.log.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.log.mapper.LogUserPasswordHistoryMapper;
import com.auth.service.system.log.model.entity.LogUserPasswordHistoryEntity;
import com.auth.service.system.log.model.query.LogUserPasswordHistoryQuery;
import com.auth.service.system.log.model.vo.LogUserPasswordHistoryDetailVO;
import com.auth.service.system.log.model.vo.LogUserPasswordHistoryPageVO;
import com.auth.service.system.log.service.LogUserPasswordHistoryService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.auth.service.system.common.exception.SystemResultCodeEnum.DATA_NOT_EXIST;

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
	 * 分页查询
	 * @param pageParams 分页
	 * @param query 条件
	 * @return 分页数据
	 */
	@Override
	public PageResponse<LogUserPasswordHistoryPageVO> getPage(Page<LogUserPasswordHistoryEntity> pageParams,
			LogUserPasswordHistoryQuery query) {
		IPage<LogUserPasswordHistoryPageVO> page = baseMapper.selectListByPage(pageParams, query);
		auditUserDisplayService.enrichAuditUsernames(page);
		return PageResponse.of(page);
	}

	/**
	 * 详情
	 * @param id 主键
	 * @return 详情
	 */
	@Override
	public LogUserPasswordHistoryDetailVO getDetail(Long id) {
		LogUserPasswordHistoryDetailVO vo = baseMapper.selectDetailById(id);
		if (vo == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		return vo;
	}

}
