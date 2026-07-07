package com.auth.service.system.file.support;

import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link FileRecordReferenceChecker} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileRecordReferenceChecker 可见性校验")
@ExtendWith(MockitoExtension.class)
class FileRecordReferenceCheckerTest {

	@Mock
	private FileRecordMapper fileRecordMapper;

	@InjectMocks
	private FileRecordReferenceChecker fileRecordReferenceChecker;

	@Test
	@DisplayName("getExistingBatch：按期望删除状态查询并返回记录")
	void getExistingBatchReturnsWhenDeletedStateMatched() {
		// 验证查询会透传 expectedDeleted 参数到 Mapper。
		FileRecordEntity deleted = new FileRecordEntity();
		deleted.setId(1102L);
		deleted.setIsDeleted(true);
		when(fileRecordMapper.selectIsDeletedByIds(List.of(1102L), true)).thenReturn(List.of(deleted));

		FileRecordEntity result = fileRecordReferenceChecker.getExistingBatch(List.of(1102L), true).get(0);

		assertThat(result).isSameAs(deleted);
		verify(fileRecordMapper).selectIsDeletedByIds(List.of(1102L), true);
	}

	@Test
	@DisplayName("getExistingBatch：删除状态不匹配时抛出 FILE_RECORD_NOT_FOUND")
	void getExistingBatchThrowsWhenDeletedStateMismatch() {
		// 验证删除状态不匹配会被识别为记录不存在。
		List<Long> queryIds = List.of(1101L);
		when(fileRecordMapper.selectIsDeletedByIds(queryIds, true)).thenReturn(List.of());

		assertThatThrownBy(() -> fileRecordReferenceChecker.getExistingBatch(queryIds, true))
			.isInstanceOf(FileStorageException.class)
			.extracting(exception -> ((FileStorageException) exception).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
	}

	@Test
	@DisplayName("getExistingBatch：空 ids 直接返回空列表")
	void getExistingBatchReturnsEmptyWhenIdsEmpty() {
		// 验证空输入不会访问数据库。
		List<FileRecordEntity> result = fileRecordReferenceChecker.getExistingBatch(List.of(), true);

		assertThat(result).isEmpty();
		verifyNoInteractions(fileRecordMapper);
	}

	@Test
	@DisplayName("requireOwnerBatch：用户为空时抛出 JWT_INVALID")
	void requireOwnerBatchThrowsWhenOwnerUserIdMissing() {
		// 验证用户上下文缺失时不会放行归属校验。
		FileRecordEntity entity = new FileRecordEntity();
		entity.setId(1003L);
		entity.setCreateUser(1L);

		List<FileRecordEntity> ownerCheckRecords = List.of(entity);
		assertThatThrownBy(() -> fileRecordReferenceChecker.requireOwnerBatch(ownerCheckRecords, null))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.JWT_INVALID);
	}

	@Test
	@DisplayName("requireOwnerBatch：访问他人记录时抛出 FILE_ACCESS_DENIED")
	void requireOwnerBatchThrowsWhenOwnerMismatch() {
		// 验证当前用户与创建人不一致时拒绝访问。
		FileRecordEntity entity = new FileRecordEntity();
		entity.setId(1004L);
		entity.setCreateUser(2L);

		List<FileRecordEntity> mismatchRecords = List.of(entity);
		Long currentUserId = 1L;
		assertThatThrownBy(() -> fileRecordReferenceChecker.requireOwnerBatch(mismatchRecords, currentUserId))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_ACCESS_DENIED);
	}

	@Test
	@DisplayName("requireOwnerBatch：创建人与当前用户一致时通过")
	void requireOwnerBatchPassesWhenOwnerMatched() {
		// 验证本人访问本人文件时允许通过校验。
		FileRecordEntity entity = new FileRecordEntity();
		entity.setId(1006L);
		entity.setCreateUser(9L);

		List<FileRecordEntity> matchedRecords = List.of(entity);
		Long matchedOwnerUserId = 9L;
		assertThatCode(() -> fileRecordReferenceChecker.requireOwnerBatch(matchedRecords, matchedOwnerUserId))
			.doesNotThrowAnyException();
	}

}
