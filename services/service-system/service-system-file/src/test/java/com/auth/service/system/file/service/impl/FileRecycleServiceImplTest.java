package com.auth.service.system.file.service.impl;

import com.auth.module.file.api.constants.FileDeleteSourceConstants;
import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.storage.core.StoragePlatformFacade;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import com.auth.service.system.file.support.FileRecordReferenceChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link FileRecycleServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileRecycleServiceImpl 回收站")
@ExtendWith(MockitoExtension.class)
class FileRecycleServiceImplTest {

	@Mock
	private FileRecordMapper fileRecordMapper;

	@Mock
	private FileRecordReferenceChecker fileRecordReferenceChecker;

	@Mock
	private StoragePlatformFacadeRegistry facadeRegistry;

	@Mock
	private StoragePlatformFacade storagePlatformFacade;

	@Mock
	private FileStorageProvider minioProvider;

	private FileRecycleServiceImpl fileRecycleService;

	@BeforeEach
	void setUp() {
		fileRecycleService = new FileRecycleServiceImpl(fileRecordMapper, fileRecordReferenceChecker, facadeRegistry);
	}

	@Test
	@DisplayName("restoreByIds：批量恢复已删除私有记录")
	void restoreByIdsRestoresDeletedRecords() {
		// 验证恢复流程会校验已删除私有记录并调用 Mapper 恢复。
		FileRecordEntity deleted = buildDeletedEntity(8001L);
		when(fileRecordReferenceChecker.getExistingBatch(List.of(8001L), true)).thenReturn(List.of(deleted));

		fileRecycleService.restoreByIds(List.of(8001L), null, 7001L);

		verify(fileRecordReferenceChecker).getExistingBatch(List.of(8001L), true);
		verify(fileRecordMapper).restoreByIds(List.of(8001L), 7001L);
	}

	@Test
	@DisplayName("restoreByIds：归属校验用户与操作用户分离")
	void restoreByIdsUsesOperatorUserIdForUpdateUser() {
		// 验证 update_user 使用操作用户而非归属校验用户。
		FileRecordEntity deleted = buildDeletedEntity(8002L);
		when(fileRecordReferenceChecker.getExistingBatch(List.of(8002L), true)).thenReturn(List.of(deleted));

		fileRecycleService.restoreByIds(List.of(8002L), 9001L, 9002L);

		verify(fileRecordReferenceChecker).requireOwnerBatch(List.of(deleted), 9001L);
		verify(fileRecordMapper).restoreByIds(List.of(8002L), 9002L);
	}

	@Test
	@DisplayName("restoreByIds：ids 为空时抛出 PARAM_REQUIRED")
	void restoreByIdsThrowsWhenIdsEmpty() {
		// 验证恢复参数为空时按参数校验规则返回异常。
		List<Long> emptyIds = List.of();
		assertThatThrownBy(() -> fileRecycleService.restoreByIds(emptyIds, null, 7001L))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);
		verify(fileRecordReferenceChecker, never()).getExistingBatch(anyList(), anyBoolean());
		verify(fileRecordMapper, never()).restoreByIds(anyList(), any());
	}

	@Test
	@DisplayName("restoreByIds：存在缺失记录时抛出 FILE_RECORD_NOT_FOUND")
	void restoreByIdsThrowsWhenRecordsMissing() {
		// 验证恢复遇到缺失记录会直接中断。
		List<Long> missingIds = List.of(8101L);
		doThrow(new FileStorageException(FileUploadResultCode.FILE_RECORD_NOT_FOUND)).when(fileRecordReferenceChecker)
			.getExistingBatch(missingIds, true);

		assertThatThrownBy(() -> fileRecycleService.restoreByIds(missingIds, null, 7001L))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		verify(fileRecordMapper, never()).restoreByIds(anyList(), any());
	}

	@Test
	@DisplayName("restoreByIds：非私有文件拒绝恢复")
	void restoreByIdsThrowsWhenFileIsNotPrivate() {
		// 验证仅允许恢复逻辑删除的私有文件。
		FileRecordEntity deleted = buildDeletedEntity(8501L);
		deleted.setIsPrivate(false);
		List<Long> restoreIds = List.of(8501L);
		when(fileRecordReferenceChecker.getExistingBatch(restoreIds, true)).thenReturn(List.of(deleted));

		assertThatThrownBy(() -> fileRecycleService.restoreByIds(restoreIds, null, 7001L))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		verify(fileRecordMapper, never()).restoreByIds(anyList(), any());
	}

	@Test
	@DisplayName("restoreByIds：个人恢复 ADMIN_ACTION 记录时拒绝")
	void restoreByIdsThrowsWhenUserRestoresAdminDeletedFile() {
		// 验证用户不能恢复管理员删除的私有文件。
		FileRecordEntity deleted = buildDeletedEntity(8601L);
		deleted.setDeleteSource(FileDeleteSourceConstants.ADMIN_ACTION);
		List<Long> restoreIds = List.of(8601L);
		when(fileRecordReferenceChecker.getExistingBatch(restoreIds, true)).thenReturn(List.of(deleted));

		assertThatThrownBy(() -> fileRecycleService.restoreByIds(restoreIds, 9001L, 9001L))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		verify(fileRecordMapper, never()).restoreByIds(anyList(), any());
	}

	@Test
	@DisplayName("restoreByIds：个人恢复他人记录时拒绝")
	void restoreByIdsThrowsWhenOwnerMismatch() {
		// 验证个人恢复会进行归属校验。
		FileRecordEntity deleted = buildDeletedEntity(8201L);
		when(fileRecordReferenceChecker.getExistingBatch(List.of(8201L), true)).thenReturn(List.of(deleted));
		doThrow(new FileStorageException(FileUploadResultCode.FILE_RECORD_NOT_FOUND)).when(fileRecordReferenceChecker)
			.requireOwnerBatch(List.of(deleted), 9001L);

		List<Long> restoreIds = List.of(8201L);
		assertThatThrownBy(() -> fileRecycleService.restoreByIds(restoreIds, 9001L, 9002L))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		verify(fileRecordMapper, never()).restoreByIds(anyList(), any());
	}

	@Test
	@DisplayName("purgeByIds：彻底删除存储对象与数据库记录")
	void purgeByIdsDeletesStorageAndDatabaseRows() {
		// 验证彻底删除会先删存储对象再物理删除数据库行。
		FileRecordEntity deleted = buildDeletedEntity(8301L);
		when(fileRecordReferenceChecker.getExistingBatch(List.of(8301L), true)).thenReturn(List.of(deleted));
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(minioProvider);

		fileRecycleService.purgeByIds(Arrays.asList(8301L, null, 8301L), null);

		verify(minioProvider).delete("public", "a.png");
		verify(fileRecordMapper).deletePhysicallyByIds(List.of(8301L));
	}

	@Test
	@DisplayName("purgeByIds：ids 为空时抛出 PARAM_REQUIRED")
	void purgeByIdsThrowsWhenIdsEmpty() {
		// 验证彻底删除参数为空时按参数校验规则返回异常。
		List<Long> emptyIds = List.of();
		assertThatThrownBy(() -> fileRecycleService.purgeByIds(emptyIds, null))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);
		verify(fileRecordReferenceChecker, never()).getExistingBatch(anyList(), anyBoolean());
		verify(fileRecordMapper, never()).deletePhysicallyByIds(anyList());
	}

	@Test
	@DisplayName("purgeByIds：个人彻底删除他人记录时拒绝")
	void purgeByIdsThrowsWhenOwnerMismatch() {
		// 验证个人彻底删除会进行归属校验。
		FileRecordEntity deleted = buildDeletedEntity(8401L);
		when(fileRecordReferenceChecker.getExistingBatch(List.of(8401L), true)).thenReturn(List.of(deleted));
		doThrow(new FileStorageException(FileUploadResultCode.FILE_RECORD_NOT_FOUND)).when(fileRecordReferenceChecker)
			.requireOwnerBatch(List.of(deleted), 9002L);

		List<Long> purgeIds = List.of(8401L);
		assertThatThrownBy(() -> fileRecycleService.purgeByIds(purgeIds, 9002L))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		verify(facadeRegistry, never()).resolve(any());
		verify(fileRecordMapper, never()).deletePhysicallyByIds(anyList());
	}

	private FileRecordEntity buildDeletedEntity(Long id) {
		FileRecordEntity entity = new FileRecordEntity();
		entity.setId(id);
		entity.setStoragePlatform(StoragePlatformEnum.MINIO);
		entity.setBucket("public");
		entity.setObjectKey("a.png");
		entity.setCreateUser(1L);
		entity.setIsPrivate(true);
		entity.setDeleteSource(FileDeleteSourceConstants.USER_SELF);
		entity.setIsDeleted(true);
		return entity;
	}

}
