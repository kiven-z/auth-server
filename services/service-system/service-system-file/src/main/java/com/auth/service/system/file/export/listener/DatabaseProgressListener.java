package com.auth.service.system.file.export.listener;

import com.auth.module.file.export.task.FileExportTaskProgressListener;
import com.auth.service.system.file.config.properties.FileExportTaskProperties;
import com.auth.service.system.file.mapper.FileExportTaskMapper;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.enums.FileExportTaskStatus;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 数据库进度监听器（写库更新进度与租约续期）
 *
 * @author Bunny
 */
@RequiredArgsConstructor
public class DatabaseProgressListener implements FileExportTaskProgressListener {

	private final FileExportTaskMapper fileExportTaskMapper;

	private final FileExportTaskProperties properties;

	private final Long taskId;

	private final String workerId;

	@Override
	public void onProgress(long processedRows, long totalRows) {
		int progressPercent = totalRows <= 0 ? 0 : (int) Math.min(99, (processedRows * 100 / totalRows));
		fileExportTaskMapper.updateProgress(taskId, workerId, progressPercent, processedRows,
				totalRows <= 0 ? null : totalRows);
		Instant leaseUntil = Instant.now().plus(properties.getLeaseSeconds(), ChronoUnit.SECONDS);
		fileExportTaskMapper.renewLease(taskId, workerId, leaseUntil);
	}

	@Override
	public boolean isCancelRequested() {
		FileExportTaskEntity task = fileExportTaskMapper.selectById(taskId);
		return task != null && FileExportTaskStatus.CANCELLING.name().equals(task.getStatus());
	}

}
