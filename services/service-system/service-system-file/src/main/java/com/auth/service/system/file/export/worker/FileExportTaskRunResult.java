package com.auth.service.system.file.export.worker;

import com.auth.module.file.export.task.FileExportTaskArtifact;
import com.auth.service.system.file.model.enums.FileExportAttemptResult;
import lombok.Builder;
import lombok.Value;

/**
 * 单次导出任务执行结果（供 attempt 落库与临时文件清理）
 *
 * @author Bunny
 */
@Value
@Builder
public class FileExportTaskRunResult {

	FileExportAttemptResult attemptResult;

	long processedRows;

	/** 待清理临时产物；成功上传后仍需删除本地文件 */
	FileExportTaskArtifact artifact;

}
