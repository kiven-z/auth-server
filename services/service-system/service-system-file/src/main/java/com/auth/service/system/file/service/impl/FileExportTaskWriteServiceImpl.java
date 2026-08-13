package com.auth.service.system.file.service.impl;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.FileExportTaskMapper;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.model.enums.FileExportTaskStatus;
import com.auth.service.system.file.model.vo.FileExportTaskDownloadLinkVO;
import com.auth.service.system.file.service.FileExportTaskWriteService;
import com.auth.service.system.file.support.FileUrlSigner;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 文件异步导出任务写服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class FileExportTaskWriteServiceImpl extends ServiceImpl<FileExportTaskMapper, FileExportTaskEntity>
		implements FileExportTaskWriteService {

	private final FileRecordMapper fileRecordMapper;

	private final FileUrlSigner fileUrlSigner;

	private final FileUploadProperties fileUploadProperties;

	private static boolean isRetryableStatus(String status) {
		return FileExportTaskStatus.FAILED.name().equals(status)
				|| FileExportTaskStatus.CANCELLED.name().equals(status);
	}

	private static boolean isRetryExhausted(FileExportTaskEntity entity) {
		Integer retryCount = entity.getRetryCount();
		Integer maxRetry = entity.getMaxRetry();
		if (retryCount == null || maxRetry == null) {
			return false;
		}
		return retryCount >= maxRetry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void cancelTask(Long taskId, Long createdBy) {
		cancelTaskInternal(taskId, createdBy);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void forceCancelTask(Long taskId) {
		cancelTaskInternal(taskId, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void retryTask(Long taskId, Long createdBy) {
		retryTaskInternal(taskId, createdBy, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void forceRetryTask(Long taskId) {
		retryTaskInternal(taskId, null, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public FileExportTaskDownloadLinkVO getDownloadLink(Long taskId, Long createdBy) {
		FileExportTaskEntity task = findTask(taskId, createdBy);
		if (task == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE, "exportTask");
		}
		if (!FileExportTaskStatus.SUCCESS.name().equals(task.getStatus())) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID, "taskStatus");
		}
		if (task.getExpireAt() != null && Instant.now().isAfter(task.getExpireAt())) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE, "artifactExpired");
		}
		if (task.getFileRecordId() == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE, "fileRecord");
		}

		FileRecordEntity fileRecord = fileRecordMapper.selectDetailById(task.getFileRecordId(), null, null, false,
				null);
		if (fileRecord == null) {
			throw new FileStorageException(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		}
		StoragePlatformEnum platform = fileRecord.getStoragePlatform();
		String url = fileUrlSigner.signDownload(fileRecord.getBucket(), fileRecord.getObjectKey(), platform);

		FileExportTaskDownloadLinkVO result = new FileExportTaskDownloadLinkVO();
		result.setUrl(url);
		result.setFileName(fileRecord.getOriginalName());
		result.setExpireSeconds(fileUploadProperties.getExpireSeconds());
		return result;
	}

	/**
	 * 取消任务（createdBy 为空表示不校验所有权）
	 * @param taskId 任务ID
	 * @param createdBy 创建人
	 */
	private void cancelTaskInternal(Long taskId, Long createdBy) {
		Instant now = Instant.now();
		int pendingRows = baseMapper.cancelPendingTask(taskId, createdBy, now);
		if (pendingRows > 0) {
			return;
		}
		int runningRows = baseMapper.requestCancelRunningTask(taskId, createdBy);
		if (runningRows > 0) {
			return;
		}
		FileExportTaskEntity entity = findTask(taskId, createdBy);
		if (entity == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE, "exportTask");
		}
		String status = entity.getStatus();
		if (FileExportTaskStatus.CANCELLING.name().equals(status)
				|| FileExportTaskStatus.CANCELLED.name().equals(status)) {
			return;
		}
		throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID, "taskStatus");
	}

	/**
	 * 重试任务（createdBy 为空表示不校验所有权；force 为 true 时不受 max_retry 约束）
	 * @param taskId 任务ID
	 * @param createdBy 创建人
	 * @param force 是否强制重试
	 */
	private void retryTaskInternal(Long taskId, Long createdBy, boolean force) {
		Instant now = Instant.now();
		int rows = force ? baseMapper.forceRetryTask(taskId, createdBy, now)
				: baseMapper.retryTask(taskId, createdBy, now);
		if (rows > 0) {
			return;
		}
		FileExportTaskEntity entity = findTask(taskId, createdBy);
		if (entity == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE, "exportTask");
		}
		if (!force && isRetryableStatus(entity.getStatus()) && isRetryExhausted(entity)) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID, "retryLimit");
		}
		throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID, "taskStatus");
	}

	/**
	 * 查询任务
	 * @param taskId 任务ID
	 * @param createdBy 创建人（为空则仅按主键查）
	 * @return 任务实体
	 */
	private FileExportTaskEntity findTask(Long taskId, Long createdBy) {
		if (createdBy == null) {
			return this.getById(taskId);
		}
		return this.lambdaQuery()
			.eq(FileExportTaskEntity::getId, taskId)
			.eq(FileExportTaskEntity::getCreatedBy, createdBy)
			.last("LIMIT 1")
			.one();
	}

}
