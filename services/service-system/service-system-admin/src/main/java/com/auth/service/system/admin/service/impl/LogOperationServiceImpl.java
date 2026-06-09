package com.auth.service.system.admin.service.impl;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.LogOperationConverter;
import com.auth.service.system.admin.mapper.LogOperationMapper;
import com.auth.service.system.admin.model.entity.LogOperationEntity;
import com.auth.service.system.admin.model.po.logoperation.LogOperationPageRowPO;
import com.auth.service.system.admin.model.query.LogOperationQuery;
import com.auth.service.system.admin.model.vo.logoperation.LogOperationDetailVO;
import com.auth.service.system.admin.model.vo.logoperation.LogOperationPageVO;
import com.auth.service.system.admin.service.LogOperationService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.collection.CollUtil;

/**
 * 审计管道与内部上报写入
 *
 * @author Bunny
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class LogOperationServiceImpl extends ServiceImpl<LogOperationMapper, LogOperationEntity>
		implements LogOperationService {

	private final AuditUserDisplayService auditUserDisplayService;

	public LogOperationServiceImpl(AuditUserDisplayService auditUserDisplayService) {
		this.auditUserDisplayService = auditUserDisplayService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<LogOperationPageVO> getPage(Page<LogOperationEntity> pageParams, LogOperationQuery query) {
		IPage<LogOperationPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		IPage<LogOperationPageVO> voPage = page.convert(LogOperationConverter.INSTANCE::toPageVO);

		// 填充创建人/更新人用户名
		auditUserDisplayService.enrichAuditUsernames(voPage);

		// 填充操作用户名
		List<LogOperationPageVO> records = voPage.getRecords();
		if (CollUtil.isEmpty(records)) {
			return PageResponse.of(voPage);
		}
		List<Long> userIds = records.stream().map(LogOperationPageVO::getUserId).toList();
		Map<Long, String> usernameMap = auditUserDisplayService.mapUsernamesByIds(userIds);
		for (LogOperationPageVO record : records) {
			if (record.getUserId() != null) {
				record.setUsername(usernameMap.get(record.getUserId()));
			}
		}
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

		// 填充创建人/更新人用户名
		auditUserDisplayService.enrichAuditUsernames(List.of(vo));

		// 填充操作用户名（注意用 userId，不是 id）
		if (vo.getUserId() != null) {
			Map<Long, String> usernameMap = auditUserDisplayService.mapUsernamesByIds(List.of(vo.getUserId()));
			vo.setUsername(usernameMap.get(vo.getUserId()));
		}
		return vo;
	}

}
