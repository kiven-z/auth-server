package com.auth.service.system.file.support;

import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.exception.FileException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.SysFileRecordMapper;
import com.auth.service.system.file.model.entity.SysFileRecordEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link FileRecordReferenceChecker} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileRecordReferenceChecker 可见性校验")
@ExtendWith(MockitoExtension.class)
class FileRecordReferenceCheckerTest {

	@Mock
	private SysFileRecordMapper sysFileRecordMapper;

	@InjectMocks
	private FileRecordReferenceChecker fileRecordReferenceChecker;

	@Test
	@DisplayName("getExistingActiveBatch：已删除记录在 @TableLogic 下查询返回空")
	void getExistingActiveBatchThrowsWhenRecordDeleted() {
		// 验证逻辑删除记录在详情查询阶段会被识别为不存在。
		when(sysFileRecordMapper.selectList(any())).thenReturn(List.of());

		List<Long> recordIds = List.of(1001L);
		assertThatThrownBy(() -> fileRecordReferenceChecker.getExistingActiveBatch(recordIds))
			.isInstanceOf(FileException.class)
			.extracting(exception -> ((FileException) exception).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
	}

	@Test
	@DisplayName("getExistingActiveBatch：未删除记录可正常返回")
	void getExistingActiveBatchReturnsWhenRecordVisible() {
		// 验证可见记录会按原样返回给后续流程。
		SysFileRecordEntity active = new SysFileRecordEntity();
		active.setId(1002L);
		when(sysFileRecordMapper.selectList(any())).thenReturn(List.of(active));

		SysFileRecordEntity result = fileRecordReferenceChecker.getExistingActiveBatch(List.of(1002L)).get(0);

		assertThat(result).isSameAs(active);
	}

	@Test
	@DisplayName("getExistingActiveBatch：包含缺失记录时抛出 FILE_RECORD_NOT_FOUND")
	void getExistingActiveBatchThrowsWhenAnyRecordMissing() {
		// 验证批量加载时只要有一个记录不存在就会整体拒绝。
		SysFileRecordEntity active = new SysFileRecordEntity();
		active.setId(2001L);
		when(sysFileRecordMapper.selectList(any())).thenReturn(List.of(active));

		List<Long> mixedRecordIds = List.of(2001L, 2002L);
		assertThatThrownBy(() -> fileRecordReferenceChecker.getExistingActiveBatch(mixedRecordIds))
			.isInstanceOf(FileException.class)
			.extracting(exception -> ((FileException) exception).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
	}

	@Test
	@DisplayName("requireOwnerBatch：用户为空时抛出 JWT_INVALID")
	void requireOwnerBatchThrowsWhenOwnerUserIdMissing() {
		// 验证用户上下文缺失时不会放行归属校验。
		SysFileRecordEntity entity = new SysFileRecordEntity();
		entity.setId(1003L);
		entity.setCreateUser(1L);

		List<SysFileRecordEntity> ownerCheckRecords = List.of(entity);
		assertThatThrownBy(() -> fileRecordReferenceChecker.requireOwnerBatch(ownerCheckRecords, null))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.JWT_INVALID);
	}

	@Test
	@DisplayName("requireOwnerBatch：访问他人记录时抛出 FILE_ACCESS_DENIED")
	void requireOwnerBatchThrowsWhenOwnerMismatch() {
		// 验证当前用户与创建人不一致时拒绝访问。
		SysFileRecordEntity entity = new SysFileRecordEntity();
		entity.setId(1004L);
		entity.setCreateUser(2L);

		List<SysFileRecordEntity> mismatchRecords = List.of(entity);
		Long currentUserId = 1L;
		assertThatThrownBy(() -> fileRecordReferenceChecker.requireOwnerBatch(mismatchRecords, currentUserId))
			.isInstanceOf(FileException.class)
			.extracting(ex -> ((FileException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_ACCESS_DENIED);
	}

	@Test
	@DisplayName("requireOwnerBatch：批量包含非本人记录时抛出 FILE_ACCESS_DENIED")
	void requireOwnerBatchThrowsWhenAnyOwnerMismatch() {
		// 验证批量归属校验场景下只要存在一条越权记录就拒绝。
		SysFileRecordEntity selfOwned = new SysFileRecordEntity();
		selfOwned.setId(3001L);
		selfOwned.setCreateUser(1L);
		SysFileRecordEntity otherOwned = new SysFileRecordEntity();
		otherOwned.setId(3002L);
		otherOwned.setCreateUser(2L);

		List<SysFileRecordEntity> batchRecords = List.of(selfOwned, otherOwned);
		Long currentUserId = 1L;
		assertThatThrownBy(() -> fileRecordReferenceChecker.requireOwnerBatch(batchRecords, currentUserId))
			.isInstanceOf(FileException.class)
			.extracting(ex -> ((FileException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_ACCESS_DENIED);
	}

	@Test
	@DisplayName("requireOwnerBatch：记录无创建人时抛出 FILE_ACCESS_DENIED")
	void requireOwnerBatchThrowsWhenRecordOwnerMissing() {
		// 验证缺失创建人的历史数据不会被个人接口越权访问。
		SysFileRecordEntity entity = new SysFileRecordEntity();
		entity.setId(1005L);

		List<SysFileRecordEntity> ownerMissingRecords = List.of(entity);
		Long currentUserId = 1L;
		assertThatThrownBy(() -> fileRecordReferenceChecker.requireOwnerBatch(ownerMissingRecords, currentUserId))
			.isInstanceOf(FileException.class)
			.extracting(ex -> ((FileException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_ACCESS_DENIED);
	}

	@Test
	@DisplayName("requireOwnerBatch：创建人与当前用户一致时通过")
	void requireOwnerBatchPassesWhenOwnerMatched() {
		// 验证本人访问本人文件时允许通过校验。
		SysFileRecordEntity entity = new SysFileRecordEntity();
		entity.setId(1006L);
		entity.setCreateUser(9L);

		List<SysFileRecordEntity> matchedRecords = List.of(entity);
		Long matchedOwnerUserId = 9L;
		assertThatCode(() -> fileRecordReferenceChecker.requireOwnerBatch(matchedRecords, matchedOwnerUserId))
			.doesNotThrowAnyException();
	}

}
