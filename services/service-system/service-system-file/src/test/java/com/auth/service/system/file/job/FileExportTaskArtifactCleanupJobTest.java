package com.auth.service.system.file.job;

import com.auth.common.core.constants.BatchSizes;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.FileExportTaskProperties;
import com.auth.service.system.file.mapper.FileExportTaskMapper;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.model.po.FileExportTaskExpireRowPO;
import com.auth.service.system.file.storage.core.StoragePlatformFacade;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link FileExportTaskArtifactCleanupJob} 单元测试
 *
 * @author Bunny
 */
@DisplayName("FileExportTaskArtifactCleanupJob 过期清理")
@ExtendWith(MockitoExtension.class)
class FileExportTaskArtifactCleanupJobTest {

	@Mock
	private FileExportTaskMapper fileExportTaskMapper;

	@Mock
	private FileRecordMapper fileRecordMapper;

	@Mock
	private StoragePlatformFacadeRegistry facadeRegistry;

	@Mock
	private StoragePlatformFacade facade;

	@Mock
	private FileStorageProvider provider;

	@Test
	@DisplayName("purgeExpiredArtifacts：清理对象并标记任务过期")
	void purgeExpiredArtifactsMarksTaskExpired() {
		FileExportTaskProperties properties = new FileExportTaskProperties();
		properties.setCleanupMaxRounds(1);

		FileExportTaskArtifactCleanupJob job = new FileExportTaskArtifactCleanupJob(properties, fileExportTaskMapper,
				fileRecordMapper, facadeRegistry);

		FileExportTaskExpireRowPO task = new FileExportTaskExpireRowPO();
		task.setId(101L);
		task.setFileRecordId(201L);
		when(fileExportTaskMapper.selectExpiredSuccessTasks(any(), eq(BatchSizes.SIZE_500))).thenReturn(List.of(task));

		FileRecordEntity fileRecord = new FileRecordEntity();
		fileRecord.setId(201L);
		fileRecord.setStoragePlatform(StoragePlatformEnum.MINIO);
		fileRecord.setBucket("private");
		fileRecord.setObjectKey("exports/101.xlsx");
		when(fileRecordMapper.selectIsDeletedByIds(List.of(201L), null, null, false, null))
			.thenReturn(List.of(fileRecord));
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(facade);
		when(facade.provider()).thenReturn(provider);

		job.purgeExpiredArtifacts();

		verify(provider).delete("private", "exports/101.xlsx");
		verify(fileRecordMapper).deletePhysicallyByIds(List.of(201L), false);
		verify(fileExportTaskMapper).markExpiredTasks(eq(List.of(task)), any());
	}

}
