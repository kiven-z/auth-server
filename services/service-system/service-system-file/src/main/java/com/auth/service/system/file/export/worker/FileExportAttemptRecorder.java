package com.auth.service.system.file.export.worker;

import com.auth.service.system.file.mapper.FileExportAttemptMapper;
import com.auth.service.system.file.model.entity.FileExportAttemptEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
	 * @param command 落库入参
	 */
	public void append(FileExportAttemptAppendCommand command) {
		FileExportAttemptEntity attemptEntity = new FileExportAttemptEntity();
		attemptEntity.setTaskId(command.getTaskId());
		attemptEntity.setAttemptNo(command.getAttemptNo());
		attemptEntity.setWorkerId(command.getWorkerId());
		attemptEntity.setResult(command.getResult().name());
		attemptEntity.setProcessedRows(command.getProcessedRows());
		attemptEntity.setErrorCode(command.getErrorCode());
		attemptEntity.setErrorMessage(command.getErrorMessage());
		attemptEntity.setStartedAt(command.getStartedAt());
		attemptEntity.setFinishedAt(command.getFinishedAt());
		fileExportAttemptMapper.insert(attemptEntity);
	}

}
