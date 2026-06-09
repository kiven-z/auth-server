package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.LogUserPasswordHistoryConverter;
import com.auth.service.system.admin.mapper.LogUserPasswordHistoryMapper;
import com.auth.service.system.admin.model.entity.LogUserPasswordHistoryEntity;
import com.auth.service.system.admin.model.po.loguserpasswordhistory.LogUserPasswordHistoryDetailRowPO;
import com.auth.service.system.admin.model.po.loguserpasswordhistory.LogUserPasswordHistoryPageRowPO;
import com.auth.service.system.admin.model.query.LogUserPasswordHistoryQuery;
import com.auth.service.system.admin.model.vo.loguserpasswordhistory.LogUserPasswordHistoryDetailVO;
import com.auth.service.system.admin.model.vo.loguserpasswordhistory.LogUserPasswordHistoryPageVO;
import com.auth.service.system.admin.service.LogUserPasswordHistoryService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 密码历史日志查询与物理删除
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
		IPage<LogUserPasswordHistoryPageVO> voPage = page.convert(LogUserPasswordHistoryConverter.INSTANCE::toPageVO);

		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LogUserPasswordHistoryDetailVO getDetail(Long id) {
		LogUserPasswordHistoryDetailRowPO po = baseMapper.selectDetailById(id);
		if (po == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		LogUserPasswordHistoryDetailVO vo = LogUserPasswordHistoryConverter.INSTANCE.toDetailVo(po);

		auditUserDisplayService.enrichAuditUsernames(List.of(vo));
		return vo;
	}

}
