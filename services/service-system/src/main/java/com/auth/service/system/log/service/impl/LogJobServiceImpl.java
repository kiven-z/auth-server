package com.auth.service.system.log.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.log.convert.SysJobLogConverter;
import com.auth.service.system.log.mapper.LogJobMapper;
import com.auth.service.system.log.model.entity.JobLogEntity;
import com.auth.service.system.log.model.po.SysJobLogPageRowPO;
import com.auth.service.system.log.model.query.LogJobQuery;
import com.auth.service.system.log.model.vo.LogJobDetailVO;
import com.auth.service.system.log.model.vo.LogJobPageResponse;
import com.auth.service.system.log.service.LogJobService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 定时任务日志服务实现
 *
 * @author Bunny
 */
@Service
public class LogJobServiceImpl extends ServiceImpl<LogJobMapper, JobLogEntity> implements LogJobService {

	private final AuditUserDisplayService auditUserDisplayService;

	public LogJobServiceImpl(AuditUserDisplayService auditUserDisplayService) {
		this.auditUserDisplayService = auditUserDisplayService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<LogJobPageResponse> getPage(Page<JobLogEntity> pageParams, LogJobQuery query) {
		IPage<SysJobLogPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		// PO 转 VO
		IPage<LogJobPageResponse> voPage = page.convert(SysJobLogConverter.INSTANCE::toPageResponse);
		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LogJobDetailVO getDetail(Long id) {
		// 查询并验证日志是否存在
		JobLogEntity sysJobLog = getById(id);
		if (sysJobLog == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		return SysJobLogConverter.INSTANCE.toDetailVo(sysJobLog);
	}

}
