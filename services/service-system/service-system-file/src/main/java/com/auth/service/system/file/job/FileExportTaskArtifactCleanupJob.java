package com.auth.service.system.file.job;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.constants.BatchSizes;
import com.auth.service.system.file.config.properties.FileExportTaskProperties;
import com.auth.service.system.file.mapper.FileExportTaskMapper;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.model.po.FileExportTaskExpireRowPO;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文件异步导出产物过期清理
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class FileExportTaskArtifactCleanupJob {

	private final FileExportTaskProperties properties;

	private final FileExportTaskMapper fileExportTaskMapper;

	private final FileRecordMapper fileRecordMapper;

	private final StoragePlatformFacadeRegistry facadeRegistry;

	/**
	 * 清理过期导出产物并标记任务已过期
	 */
	@Scheduled(cron = "${auth.file.export-task.cleanup-cron:0 10 4 * * ?}")
	public void purgeExpiredArtifacts() {
		int batchSize = BatchSizes.SIZE_500;
		int total = 0;
		for (int round = 0; round < properties.getCleanupMaxRounds(); round++) {
			int purged = purgeBatch(Instant.now(), batchSize);
			total += purged;
			if (purged < batchSize) {
				break;
			}
		}
		if (total > 0) {
			log.info("Purged expired export artifacts: count={}", total);
		}
	}

	/**
	 * 分批清理过期导出产物
	 * @param now 当前时间
	 * @param batchSize 批量大小
	 * @return 清理数量
	 */
	private int purgeBatch(Instant now, int batchSize) {
		List<FileExportTaskExpireRowPO> tasks = fileExportTaskMapper.selectExpiredSuccessTasks(now, batchSize);
		if (CollUtil.isEmpty(tasks)) {
			return 0;
		}

		List<Long> fileRecordIds = tasks.stream()
			.map(FileExportTaskExpireRowPO::getFileRecordId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();
		Map<Long, FileRecordEntity> fileRecordById = loadFileRecordMap(fileRecordIds);

		List<FileExportTaskExpireRowPO> removableTasks = new ArrayList<>();
		List<Long> removableFileRecordIds = new ArrayList<>();
		for (FileExportTaskExpireRowPO task : tasks) {
			Long fileRecordId = task.getFileRecordId();
			FileRecordEntity fileRecord = fileRecordById.get(fileRecordId);
			if (!removeArtifactIfPresent(fileRecord)) {
				continue;
			}
			removableTasks.add(task);
			if (fileRecord != null) {
				removableFileRecordIds.add(fileRecordId);
			}
		}
		if (CollUtil.isEmpty(removableTasks)) {
			return 0;
		}

		if (CollUtil.isNotEmpty(removableFileRecordIds)) {
			fileRecordMapper.deletePhysicallyByIds(removableFileRecordIds.stream().distinct().toList(), false);
		}
		fileExportTaskMapper.markExpiredTasks(removableTasks, now);
		return removableTasks.size();
	}

	/**
	 * 加载文件记录映射
	 * @param fileRecordIds 文件记录ID列表
	 * @return 文件记录映射
	 */
	private Map<Long, FileRecordEntity> loadFileRecordMap(List<Long> fileRecordIds) {
		if (CollUtil.isEmpty(fileRecordIds)) {
			return Map.of();
		}
		List<FileRecordEntity> fileRecords = fileRecordMapper.selectIsDeletedByIds(fileRecordIds, null, null, false,
				null);
		return fileRecords.stream().collect(Collectors.toMap(FileRecordEntity::getId, item -> item));
	}

	/**
	 * 删除对象存储中的导出产物
	 * @param fileRecord 文件记录
	 * @return 删除成功或记录不存在时返回 true
	 */
	private boolean removeArtifactIfPresent(FileRecordEntity fileRecord) {
		if (fileRecord == null) {
			return true;
		}
		try {
			FileStorageProvider provider = facadeRegistry.resolve(fileRecord.getStoragePlatform()).provider();
			provider.delete(fileRecord.getBucket(), fileRecord.getObjectKey());
			return true;
		}
		catch (Exception exception) {
			log.warn("Skip expired export artifact cleanup due to storage failure: fileRecordId={}", fileRecord.getId(),
					exception);
			return false;
		}
	}

}
