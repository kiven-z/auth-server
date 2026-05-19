package com.auth.service.system.log.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.log.convert.LogOperationConvert;
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

import static com.auth.service.system.common.exception.SystemCommonResultCode.DATA_NOT_EXIST;

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
		IPage<LogOperationPageVO> voPage = page.convert(LogOperationConvert.INSTANCE::toPageVO);
		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LogOperationDetailVO getDetail(Long id) {
		LogOperationEntity entity = getById(id);
		if (entity == null || Boolean.TRUE.equals(entity.getIsDeleted())) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		LogOperationDetailVO vo = BeanUtil.copyProperties(entity, LogOperationDetailVO.class);
		auditUserDisplayService.enrichAuditUsernames(List.of(vo));
		return vo;
	}

}
