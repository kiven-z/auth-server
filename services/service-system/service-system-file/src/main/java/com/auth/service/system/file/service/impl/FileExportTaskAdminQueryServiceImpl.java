package com.auth.service.system.file.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.file.convert.FileExportTaskAdminQueryConverter;
import com.auth.service.system.file.mapper.FileExportAttemptMapper;
import com.auth.service.system.file.mapper.FileExportTaskMapper;
import com.auth.service.system.file.model.entity.FileExportAttemptEntity;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.po.FileExportTaskAdminPageRowPO;
import com.auth.service.system.file.model.query.FileExportTaskAdminPageQuery;
import com.auth.service.system.file.model.vo.FileExportAttemptVO;
import com.auth.service.system.file.model.vo.FileExportTaskAdminDetailVO;
import com.auth.service.system.file.model.vo.FileExportTaskAdminPageVO;
import com.auth.service.system.file.service.FileExportTaskAdminQueryService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 文件异步导出任务管理端查询服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class FileExportTaskAdminQueryServiceImpl implements FileExportTaskAdminQueryService {

	private final FileExportTaskMapper fileExportTaskMapper;

	private final FileExportAttemptMapper fileExportAttemptMapper;

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<FileExportTaskAdminPageVO> getPage(FileExportTaskAdminPageQuery query) {
		Page<FileExportTaskEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<FileExportTaskAdminPageRowPO> page = fileExportTaskMapper.selectPageAdmin(pageParams, query);
		IPage<FileExportTaskAdminPageVO> voPage = page.convert(FileExportTaskAdminQueryConverter.INSTANCE::toPageVo);
		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public FileExportTaskAdminDetailVO getDetail(Long id) {
		FileExportTaskEntity entity = requireTask(id);
		FileExportTaskAdminDetailVO detail = FileExportTaskAdminQueryConverter.INSTANCE.toDetailVo(entity);
		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail), null, null);
		return detail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<FileExportAttemptVO> listAttempts(Long taskId) {
		requireTask(taskId);

		var wrapper = Wrappers.<FileExportAttemptEntity>lambdaQuery()
			.eq(FileExportAttemptEntity::getTaskId, taskId)
			.orderByAsc(FileExportAttemptEntity::getAttemptNo);

		List<FileExportAttemptEntity> attempts = fileExportAttemptMapper.selectList(wrapper);
		return attempts.stream().map(FileExportTaskAdminQueryConverter.INSTANCE::toAttemptVo).toList();
	}

	/**
	 * 按主键加载任务（不存在则抛业务异常）
	 * @param taskId 任务ID
	 * @return 任务实体
	 */
	private FileExportTaskEntity requireTask(Long taskId) {
		FileExportTaskEntity entity = fileExportTaskMapper.selectById(taskId);
		if (entity == null) {
			log.warn("Export task not found: id={}", taskId);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE, "exportTask");
		}
		return entity;
	}

}
