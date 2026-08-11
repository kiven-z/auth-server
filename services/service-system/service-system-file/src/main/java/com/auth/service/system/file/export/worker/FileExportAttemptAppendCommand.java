package com.auth.service.system.file.export.worker;

import com.auth.service.system.file.model.enums.FileExportAttemptResult;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * 导出任务尝试落库入参
 *
 * @author Bunny
 */
@Value
@Builder
public class FileExportAttemptAppendCommand {

	/**
	 * 任务ID
	 */
	Long taskId;

	/**
	 * 尝试次数
	 */
	int attemptNo;

	/**
	 * 开始时间
	 */
	Instant startedAt;

	/**
	 * 结束时间
	 */
	Instant finishedAt;

	/**
	 * 结果
	 */
	FileExportAttemptResult result;

	/**
	 * 处理行数
	 */
	long processedRows;

	/**
	 * 错误码
	 */
	String errorCode;

	/**
	 * 错误信息
	 */
	String errorMessage;

}
