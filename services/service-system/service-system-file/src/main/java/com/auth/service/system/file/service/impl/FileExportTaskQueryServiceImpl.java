package com.auth.service.system.file.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.file.convert.FileExportTaskQueryConverter;
import com.auth.service.system.file.mapper.FileExportTaskMapper;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.po.FileExportTaskPageRowPO;
import com.auth.service.system.file.model.query.FileExportTaskPageQuery;
import com.auth.service.system.file.model.vo.FileExportTaskDetailVO;
import com.auth.service.system.file.model.vo.FileExportTaskPageVO;
import com.auth.service.system.file.service.FileExportTaskQueryService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * 文件异步导出任务查询服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class FileExportTaskQueryServiceImpl extends ServiceImpl<FileExportTaskMapper, FileExportTaskEntity>
		implements FileExportTaskQueryService {

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<FileExportTaskPageVO> getPage(FileExportTaskPageQuery query, Long createdBy) {
		Page<FileExportTaskEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<FileExportTaskPageRowPO> page = baseMapper.selectPageByCreator(pageParams, query, createdBy);
		IPage<FileExportTaskPageVO> voPage = page.convert(FileExportTaskQueryConverter.INSTANCE::toPageVo);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public FileExportTaskDetailVO getDetail(Long id, Long createdBy) {
		FileExportTaskEntity entity = this.lambdaQuery()
			.eq(FileExportTaskEntity::getId, id)
			.eq(FileExportTaskEntity::getCreatedBy, createdBy)
			.last("LIMIT 1")
			.one();

		if (entity == null) {
			log.warn("Export task not found or unavailable: id={}, createdBy={}", id, createdBy);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE, "exportTask");
		}

		FileExportTaskDetailVO detail = FileExportTaskQueryConverter.INSTANCE.toDetailVo(entity);
		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail), null, null);
		return detail;
	}

}
