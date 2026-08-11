package com.auth.service.system.file.job;

import com.auth.service.system.file.export.worker.FileExportTaskWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 文件异步导出任务调度
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class FileExportTaskWorkerJob {

	private final FileExportTaskWorker worker;

	/**
	 * 按固定间隔执行任务拉取
	 */
	@Scheduled(fixedDelayString = "${auth.file.export-task.poll-interval-ms:5000}")
	public void run() {
		worker.executeRound();
	}

}
