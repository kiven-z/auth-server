package com.auth.service.system.log.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.log.convert.LogOperationConverter;
import com.auth.service.system.log.mapper.LogOperationMapper;
import com.auth.service.system.log.model.entity.LogOperationEntity;
import com.auth.service.system.log.model.po.LogOperationPageRowPO;
import com.auth.service.system.log.model.query.LogOperationQuery;
import com.auth.service.system.log.model.vo.LogOperationDetailVO;
import com.auth.service.system.log.model.vo.LogOperationPageVO;
import com.auth.service.system.log.service.LogOperationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 审计管道与内部上报写入；对外提供分页、详情与删除
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
		auditUserDisplayService.enrichAuditUsernames(voPage);
		enrichOperatorUsernames(voPage.getRecords());
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
		auditUserDisplayService.enrichAuditUsernames(List.of(vo));
		enrichOperatorUsername(vo);
		return vo;
	}

	/**
	 * 为分页行填充操作用户名（{@code userId} → {@code username}）
	 * @param records 分页 VO 列表
	 */
	private void enrichOperatorUsernames(List<LogOperationPageVO> records) {
		if (CollUtil.isEmpty(records)) {
			return;
		}
		List<Long> userIds = records.stream()
			.map(LogOperationPageVO::getUserId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();
		Map<Long, String> usernameMap = auditUserDisplayService.mapUsernamesByIds(userIds);
		for (LogOperationPageVO vo : records) {
			if (vo.getUserId() != null) {
				vo.setUsername(usernameMap.get(vo.getUserId()));
			}
		}
	}

	/**
	 * 为详情填充操作用户名
	 * @param vo 详情 VO
	 */
	private void enrichOperatorUsername(LogOperationDetailVO vo) {
		if (vo == null || vo.getUserId() == null) {
			return;
		}
		Map<Long, String> usernameMap = auditUserDisplayService.mapUsernamesByIds(List.of(vo.getUserId()));
		vo.setUsername(usernameMap.get(vo.getUserId()));
	}

}
