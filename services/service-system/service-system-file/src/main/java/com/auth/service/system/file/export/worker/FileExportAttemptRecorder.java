package com.auth.service.system.file.export.worker;

import com.auth.service.system.file.mapper.FileExportAttemptMapper;
import com.auth.service.system.file.model.entity.FileExportAttemptEntity;
import com.auth.service.system.file.model.enums.FileExportAttemptResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 导出任务尝试记录写入
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class FileExportAttemptRecorder {

	private final FileExportAttemptMapper fileExportAttemptMapper;

	/**
	 * 写入执行尝试记录
	 * @param taskId 任务ID
	 * @param attemptNo 本次执行序号（由任务 retryCount+1 推导，claim 串行）
	 * @param startedAt 开始时间
	 * @param finishedAt 结束时间
	 * @param result 执行结果
	 * @param processedRows 处理行数
	 * @param errorCode 错误码
	 * @param errorMessage 错误信息
	 */
	public void append(Long taskId, int attemptNo, Instant startedAt, Instant finishedAt,
			FileExportAttemptResult result, long processedRows, String errorCode, String errorMessage) {
		FileExportAttemptEntity attemptEntity = new FileExportAttemptEntity();
		attemptEntity.setTaskId(taskId);
		attemptEntity.setAttemptNo(attemptNo);
		attemptEntity.setWorkerId(FileExportWorkerConstants.WORKER_ID);
		attemptEntity.setResult(result.name());
		attemptEntity.setProcessedRows(processedRows);
		attemptEntity.setErrorCode(errorCode);
		attemptEntity.setErrorMessage(errorMessage);
		attemptEntity.setStartedAt(startedAt);
		attemptEntity.setFinishedAt(finishedAt);
		fileExportAttemptMapper.insert(attemptEntity);
	}

}
