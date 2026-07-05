package com.auth.service.system.file.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.file.exception.FileException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.SysFileRecordMapper;
import com.auth.service.system.file.model.entity.SysFileRecordEntity;
import com.auth.service.system.file.model.po.SysFileRecordPageRowPO;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.auth.service.system.file.storage.FileStorageProvider;
import com.auth.service.system.file.storage.FileStorageProviderRegistry;
import com.auth.service.system.file.support.FileRecordPageAssembler;
import com.auth.service.system.file.support.FileRecordReferenceChecker;
import com.auth.service.system.file.support.FileUrlSigner;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
	private SysFileRecordMapper sysFileRecordMapper;

	@Mock
	private FileRecordReferenceChecker fileRecordReferenceChecker;

	@Mock
	private FileUrlSigner fileUrlSigner;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private FileStorageProviderRegistry storageProviderRegistry;

	@Mock
	private FileStorageProvider minioProvider;

	private FileRecycleServiceImpl fileRecycleService;

	@BeforeEach
	void setUp() {
		FileRecordPageAssembler fileRecordPageAssembler = new FileRecordPageAssembler(fileUrlSigner,
				auditUserDisplayService);
		fileRecycleService = new FileRecycleServiceImpl(sysFileRecordMapper, fileRecordPageAssembler,
				fileRecordReferenceChecker, storageProviderRegistry);
	}

	@Test
	@DisplayName("getPage：回收站分页走专用 Mapper 查询")
	void getPageUsesRecycleMapperQuery() {
		// 验证回收站分页会调用回收站专用查询。
		FileRecordPageQuery query = new FileRecordPageQuery();
		query.setPageIndex(1);
		query.setPageSize(20);

		SysFileRecordPageRowPO recycled = buildPageRow();
		Page<SysFileRecordPageRowPO> page = new Page<>(1, 20, 1);
		page.setRecords(List.of(recycled));
		when(sysFileRecordMapper.selectRecycleListByPage(any(), eq(query))).thenReturn(page);
		when(fileUrlSigner.sign(recycled.getBucket(), recycled.getObjectKey(), recycled.getContentType(),
				recycled.getStoragePlatform()))
			.thenReturn(recycled.getUrl() + "?expires=210");

		PageResponse<FileRecordPageVO> pageResult = fileRecycleService.getPage(query);

		assertThat(pageResult.getList()).hasSize(1);
		verify(sysFileRecordMapper).selectRecycleListByPage(any(), eq(query));
		verify(sysFileRecordMapper, never()).selectListByPage(any(), any());
	}

	@Test
	@DisplayName("restoreByIds：批量恢复已删除记录")
	void restoreByIdsRestoresDeletedRecords() {
		// 验证恢复流程会校验已删除记录并调用 Mapper 恢复。
		SysFileRecordEntity deleted = buildDeletedEntity(8001L);
		when(fileRecordReferenceChecker.getExistingBatch(List.of(8001L), true)).thenReturn(List.of(deleted));

		fileRecycleService.restoreByIds(List.of(8001L), null, 7001L);

		verify(fileRecordReferenceChecker).getExistingBatch(List.of(8001L), true);
		verify(sysFileRecordMapper).restoreByIds(List.of(8001L), 7001L);
	}

	@Test
	@DisplayName("restoreByIds：归属校验用户与操作用户分离")
	void restoreByIdsUsesOperatorUserIdForUpdateUser() {
		// 验证 update_user 使用操作用户而非归属校验用户。
		SysFileRecordEntity deleted = buildDeletedEntity(8002L);
		when(fileRecordReferenceChecker.getExistingBatch(List.of(8002L), true)).thenReturn(List.of(deleted));

		fileRecycleService.restoreByIds(List.of(8002L), 9001L, 9002L);

		verify(fileRecordReferenceChecker).requireOwnerBatch(List.of(deleted), 9001L);
		verify(sysFileRecordMapper).restoreByIds(List.of(8002L), 9002L);
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
		verify(sysFileRecordMapper, never()).restoreByIds(anyList(), any());
	}

	@Test
	@DisplayName("restoreByIds：存在缺失记录时抛出 FILE_RECORD_NOT_FOUND")
	void restoreByIdsThrowsWhenRecordsMissing() {
		// 验证恢复遇到缺失记录会直接中断。
		List<Long> missingIds = List.of(8101L);
		doThrow(new FileException(FileUploadResultCode.FILE_RECORD_NOT_FOUND, missingIds))
			.when(fileRecordReferenceChecker)
			.getExistingBatch(missingIds, true);

		assertThatThrownBy(() -> fileRecycleService.restoreByIds(missingIds, null, 7001L))
			.isInstanceOf(FileException.class)
			.extracting(ex -> ((FileException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		verify(sysFileRecordMapper, never()).restoreByIds(anyList(), any());
	}

	@Test
	@DisplayName("restoreByIds：个人恢复他人记录时拒绝")
	void restoreByIdsThrowsWhenOwnerMismatch() {
		// 验证个人恢复会进行归属校验。
		SysFileRecordEntity deleted = buildDeletedEntity(8201L);
		when(fileRecordReferenceChecker.getExistingBatch(List.of(8201L), true)).thenReturn(List.of(deleted));
		doThrow(new FileException(FileUploadResultCode.FILE_ACCESS_DENIED)).when(fileRecordReferenceChecker)
			.requireOwnerBatch(List.of(deleted), 9001L);

		List<Long> restoreIds = List.of(8201L);
		assertThatThrownBy(() -> fileRecycleService.restoreByIds(restoreIds, 9001L, 9002L))
			.isInstanceOf(FileException.class)
			.extracting(ex -> ((FileException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_ACCESS_DENIED);
		verify(sysFileRecordMapper, never()).restoreByIds(anyList(), any());
	}

	@Test
	@DisplayName("purgeByIds：彻底删除存储对象与数据库记录")
	void purgeByIdsDeletesStorageAndDatabaseRows() {
		// 验证彻底删除会先删存储对象再物理删除数据库行。
		SysFileRecordEntity deleted = buildDeletedEntity(8301L);
		when(fileRecordReferenceChecker.getExistingBatch(List.of(8301L), true)).thenReturn(List.of(deleted));
		when(storageProviderRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(minioProvider);

		fileRecycleService.purgeByIds(Arrays.asList(8301L, null, 8301L), null);

		verify(minioProvider).delete("public", "a.png");
		verify(sysFileRecordMapper).deletePhysicallyByIds(List.of(8301L));
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
		verify(sysFileRecordMapper, never()).deletePhysicallyByIds(anyList());
	}

	@Test
	@DisplayName("purgeByIds：个人彻底删除他人记录时拒绝")
	void purgeByIdsThrowsWhenOwnerMismatch() {
		// 验证个人彻底删除会进行归属校验。
		SysFileRecordEntity deleted = buildDeletedEntity(8401L);
		when(fileRecordReferenceChecker.getExistingBatch(List.of(8401L), true)).thenReturn(List.of(deleted));
		doThrow(new FileException(FileUploadResultCode.FILE_ACCESS_DENIED)).when(fileRecordReferenceChecker)
			.requireOwnerBatch(List.of(deleted), 9002L);

		List<Long> purgeIds = List.of(8401L);
		assertThatThrownBy(() -> fileRecycleService.purgeByIds(purgeIds, 9002L)).isInstanceOf(FileException.class)
			.extracting(ex -> ((FileException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_ACCESS_DENIED);
		verify(storageProviderRegistry, never()).resolve(any());
		verify(sysFileRecordMapper, never()).deletePhysicallyByIds(anyList());
	}

	private SysFileRecordEntity buildDeletedEntity(Long id) {
		SysFileRecordEntity entity = new SysFileRecordEntity();
		entity.setId(id);
		entity.setStoragePlatform(StoragePlatformEnum.MINIO);
		entity.setBucket("public");
		entity.setObjectKey("a.png");
		entity.setCreateUser(1L);
		entity.setIsDeleted(true);
		return entity;
	}

	private SysFileRecordPageRowPO buildPageRow() {
		SysFileRecordPageRowPO row = new SysFileRecordPageRowPO();
		row.setId(2101L);
		row.setStoragePlatform(StoragePlatformEnum.MINIO);
		row.setContentType("image/png");
		row.setUrl("https://cdn.example.com/a.png");
		row.setOriginalName("a.png");
		row.setBucket("public");
		row.setObjectKey("a.png");
		row.setCreateUser(1L);
		return row;
	}

}
