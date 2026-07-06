package com.auth.service.system.file.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.file.api.constants.FileDeleteSourceConstants;
import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.module.file.export.delivery.FileDelivery;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.SysFileRecordMapper;
import com.auth.service.system.file.model.entity.SysFileRecordEntity;
import com.auth.service.system.file.model.po.SysFileRecordPageRowPO;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordDetailVO;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.auth.service.system.file.service.SysFileRecordService;
import com.auth.service.system.file.storage.StoragePlatformFacade;
import com.auth.service.system.file.storage.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import com.auth.service.system.file.support.FileRecordPageAssembler;
import com.auth.service.system.file.support.FileRecordReferenceChecker;
import com.auth.service.system.file.support.FileUrlSigner;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link SysFileRecordServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysFileRecordServiceImpl 查询与治理")
@ExtendWith(MockitoExtension.class)
class SysFileRecordServiceImplTest {

	@Mock
	private SysFileRecordMapper sysFileRecordMapper;

	@Mock
	private FileRecordReferenceChecker fileRecordReferenceChecker;

	@Mock
	private FileUrlSigner fileUrlSigner;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private StoragePlatformFacadeRegistry facadeRegistry;

	@Mock
	private StoragePlatformFacade storagePlatformFacade;

	@Mock
	private FileStorageProvider minioProvider;

	private SysFileRecordServiceImpl sysFileRecordService;

	@BeforeEach
	void setUp() throws Exception {
		FileRecordPageAssembler fileRecordPageAssembler = new FileRecordPageAssembler(fileUrlSigner,
				auditUserDisplayService);
		sysFileRecordService = spy(new SysFileRecordServiceImpl(fileRecordReferenceChecker, fileRecordPageAssembler,
				auditUserDisplayService, facadeRegistry, fileUrlSigner));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysFileRecordService, sysFileRecordMapper);
	}

	@Test
	@DisplayName("getDetail：公开文件返回 presign accessUrl")
	void getDetailReturnsPresignAccessUrlWhenFileIsPublic() {
		// 验证公开文件详情也统一返回临时签名访问地址。
		SysFileRecordEntity entity = buildEntity(1001L);
		entity.setIsPrivate(false);
		when(fileRecordReferenceChecker.getExistingBatch(List.of(1001L), false)).thenReturn(List.of(entity));
		when(fileUrlSigner.sign(entity.getBucket(), entity.getObjectKey(), entity.getContentType(),
				entity.getStoragePlatform()))
			.thenReturn("https://minio.example.com/public/a.png?X-Amz-Signature=demo");

		FileRecordDetailVO detail = sysFileRecordService.getDetail(1001L, null);

		verify(auditUserDisplayService)
			.enrichAuditUsernames(org.mockito.ArgumentMatchers.<List<FileRecordDetailVO>>any(), isNull(), isNull());
		assertThat(detail.getId()).isEqualTo(1001L);
		assertThat(detail.getAccessUrl()).isEqualTo("https://minio.example.com/public/a.png?X-Amz-Signature=demo");
	}

	@Test
	@DisplayName("getPage：个人分页携带 ownerUserId 时按本人过滤")
	void getPageFiltersByOwnerWhenOwnerUserIdPresent() {
		// 验证个人分页场景会把 ownerUserId 透传到 Mapper 过滤条件。
		FileRecordPageQuery query = new FileRecordPageQuery();
		query.setPageIndex(1);
		query.setPageSize(20);
		query.setOwnerUserId(9001L);

		SysFileRecordPageRowPO visible = buildPageRow(2001L);
		visible.setIsPrivate(false);
		Page<SysFileRecordPageRowPO> page = new Page<>(1, 20, 1);
		page.setRecords(List.of(visible));
		when(sysFileRecordMapper.selectListByPage(org.mockito.ArgumentMatchers.any(), eq(query))).thenReturn(page);
		when(fileUrlSigner.sign(visible.getBucket(), visible.getObjectKey(), visible.getContentType(),
				visible.getStoragePlatform()))
			.thenReturn("https://minio.example.com/public/a.png?X-Amz-Signature=demo");

		PageResponse<FileRecordPageVO> pageResult = sysFileRecordService.getPage(query);

		assertThat(pageResult.getList()).hasSize(1);
		assertThat(pageResult.getList().get(0).getAccessUrl())
			.isEqualTo("https://minio.example.com/public/a.png?X-Amz-Signature=demo");
		verify(sysFileRecordMapper).selectListByPage(org.mockito.ArgumentMatchers.any(),
				argThat(param -> Long.valueOf(9001L).equals(param.getOwnerUserId())));
		verify(auditUserDisplayService).enrichAuditUsernames(org.mockito.ArgumentMatchers.<Page<FileRecordPageVO>>any(),
				isNull(), isNull());
	}

	@Test
	@DisplayName("getPage：管理分页不传 ownerUserId 仍可查询")
	void getPageKeepsAdminQueryWhenOwnerUserIdMissing() {
		// 验证管理端不传 ownerUserId 时，查询条件保持为空且不被强制注入。
		FileRecordPageQuery query = new FileRecordPageQuery();
		query.setPageIndex(1);
		query.setPageSize(20);

		SysFileRecordPageRowPO visible = buildPageRow(2002L);
		visible.setIsPrivate(true);
		Page<SysFileRecordPageRowPO> page = new Page<>(1, 20, 1);
		page.setRecords(List.of(visible));
		when(sysFileRecordMapper.selectListByPage(org.mockito.ArgumentMatchers.any(), eq(query))).thenReturn(page);
		when(fileUrlSigner.sign(visible.getBucket(), visible.getObjectKey(), visible.getContentType(),
				visible.getStoragePlatform()))
			.thenReturn("https://minio.example.com/private/a.png?X-Amz-Signature=201");

		PageResponse<FileRecordPageVO> pageResult = sysFileRecordService.getPage(query);

		assertThat(pageResult.getList()).hasSize(1);
		assertThat(pageResult.getList().get(0).getAccessUrl())
			.isEqualTo("https://minio.example.com/private/a.png?X-Amz-Signature=201");
		verify(sysFileRecordMapper).selectListByPage(org.mockito.ArgumentMatchers.any(),
				argThat(param -> param.getOwnerUserId() == null));
	}

	@Test
	@DisplayName("getPage：时间范围参数按原样透传到 Mapper")
	void getPagePassesTimeRangeParamsToMapper() {
		// 验证时间范围筛选参数会传递到分页查询条件。
		LocalDateTime startTime = LocalDateTime.of(2026, 7, 1, 0, 0, 0);
		LocalDateTime endTime = LocalDateTime.of(2026, 7, 3, 23, 59, 59);
		FileRecordPageQuery query = new FileRecordPageQuery();
		query.setPageIndex(1);
		query.setPageSize(20);
		query.setStartTime(startTime);
		query.setEndTime(endTime);

		SysFileRecordPageRowPO visible = buildPageRow(2003L);
		visible.setIsPrivate(true);
		Page<SysFileRecordPageRowPO> page = new Page<>(1, 20, 1);
		page.setRecords(List.of(visible));
		when(sysFileRecordMapper.selectListByPage(org.mockito.ArgumentMatchers.any(), eq(query))).thenReturn(page);
		when(fileUrlSigner.sign(visible.getBucket(), visible.getObjectKey(), visible.getContentType(),
				visible.getStoragePlatform()))
			.thenReturn("https://minio.example.com/private/a.png?X-Amz-Signature=202");

		sysFileRecordService.getPage(query);

		verify(sysFileRecordMapper).selectListByPage(org.mockito.ArgumentMatchers.any(),
				argThat(param -> startTime.equals(param.getStartTime()) && endTime.equals(param.getEndTime())));
	}

	@Test
	@DisplayName("getDetail：个人访问他人记录时拒绝")
	void getDetailThrowsWhenOwnerMismatch() {
		// 验证个人详情查询会进行归属校验并拒绝访问他人记录。
		SysFileRecordEntity entity = buildEntity(3001L);
		when(fileRecordReferenceChecker.getExistingBatch(List.of(3001L), false)).thenReturn(List.of(entity));
		doThrow(new FileStorageException(FileUploadResultCode.FILE_ACCESS_DENIED, 3001L))
			.when(fileRecordReferenceChecker)
			.requireOwnerBatch(List.of(entity), 9002L);

		Long detailId = 3001L;
		Long ownerUserId = 9002L;
		assertThatThrownBy(() -> sysFileRecordService.getDetail(detailId, ownerUserId))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_ACCESS_DENIED);
	}

	@Test
	@DisplayName("deleteByIds：个人批删包含他人记录时拒绝")
	void deleteByIdsThrowsWhenAnyOwnerMismatch() {
		// 验证个人批量删除会通过批量 owner 校验拒绝越权记录。
		SysFileRecordEntity first = buildEntity(4001L);
		SysFileRecordEntity second = buildEntity(4002L);
		when(fileRecordReferenceChecker.getExistingBatch(List.of(4001L, 4002L), false))
			.thenReturn(List.of(first, second));
		doThrow(new FileStorageException(FileUploadResultCode.FILE_ACCESS_DENIED, 4002L))
			.when(fileRecordReferenceChecker)
			.requireOwnerBatch(List.of(first, second), 9004L);

		List<Long> deleteIds = List.of(4001L, 4002L);
		Long deleteOwnerUserId = 9004L;
		assertThatThrownBy(() -> sysFileRecordService.deleteByIds(deleteIds, deleteOwnerUserId, deleteOwnerUserId))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_ACCESS_DENIED);
		verify(sysFileRecordMapper, never()).markDeletedByIds(anyList(), any(), any(), anyString());
	}

	@Test
	@DisplayName("deleteByIds：ids 为空时抛出 PARAM_REQUIRED")
	void deleteByIdsThrowsWhenIdsEmpty() {
		// 验证批量删除参数为空时按参数校验规则返回异常。
		List<Long> emptyIds = List.of();
		assertThatThrownBy(() -> sysFileRecordService.deleteByIds(emptyIds, null, null))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);
		verify(sysFileRecordMapper, never()).markDeletedByIds(anyList(), any(), any(), anyString());
		verify(fileRecordReferenceChecker, never()).getExistingBatch(anyList(), anyBoolean());
	}

	@Test
	@DisplayName("deleteByIds：存在缺失记录时抛出 FILE_RECORD_NOT_FOUND")
	void deleteByIdsThrowsWhenRecordsMissing() {
		// 验证批量删除遇到缺失记录会直接中断并抛业务异常。
		doThrow(new FileStorageException(FileUploadResultCode.FILE_RECORD_NOT_FOUND, List.of(4101L, 4102L)))
			.when(fileRecordReferenceChecker)
			.getExistingBatch(List.of(4101L, 4102L), false);

		List<Long> missingIds = List.of(4101L, 4102L);
		assertThatThrownBy(() -> sysFileRecordService.deleteByIds(missingIds, null, null))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		verify(sysFileRecordMapper, never()).markDeletedByIds(anyList(), any(), any(), anyString());
	}

	@Test
	@DisplayName("deleteByIds：私有文件执行逻辑删除并写入 USER_SELF")
	void deleteByIdsMarksPrivateFilesDeletedWithUserSelfSource() {
		// 验证用户删除私有文件时走逻辑删除并标记 USER_SELF。
		SysFileRecordEntity first = buildEntity(5001L);
		first.setIsPrivate(true);
		SysFileRecordEntity second = buildEntity(5002L);
		second.setIsPrivate(true);
		when(fileRecordReferenceChecker.getExistingBatch(List.of(5001L, 5002L), false))
			.thenReturn(List.of(first, second));

		sysFileRecordService.deleteByIds(Arrays.asList(5001L, null, 5002L, 5001L), 7001L, 7001L);

		verify(fileRecordReferenceChecker).getExistingBatch(List.of(5001L, 5002L), false);
		verify(sysFileRecordMapper).markDeletedByIds(List.of(5001L, 5002L), 7001L, 7001L,
				FileDeleteSourceConstants.USER_SELF);
		verify(sysFileRecordMapper, never()).deletePhysicallyActiveByIds(anyList());
		verify(facadeRegistry, never()).resolve(any());
	}

	@Test
	@DisplayName("deleteByIds：管理端删除私有文件写入 ADMIN_ACTION")
	void deleteByIdsMarksPrivateFilesDeletedWithAdminActionSource() {
		// 验证管理端删除私有文件时走逻辑删除并标记 ADMIN_ACTION。
		SysFileRecordEntity entity = buildEntity(5101L);
		entity.setIsPrivate(true);
		when(fileRecordReferenceChecker.getExistingBatch(List.of(5101L), false)).thenReturn(List.of(entity));

		sysFileRecordService.deleteByIds(List.of(5101L), null, 7002L);

		verify(sysFileRecordMapper).markDeletedByIds(List.of(5101L), 7002L, 7002L,
				FileDeleteSourceConstants.ADMIN_ACTION);
	}

	@Test
	@DisplayName("deleteByIds：公开文件物理删除存储对象与数据库记录")
	void deleteByIdsPhysicallyDeletesPublicFiles() {
		// 验证公开文件删除会直接清理存储对象并物理删除数据库行。
		SysFileRecordEntity first = buildEntity(5201L);
		first.setIsPrivate(false);
		SysFileRecordEntity second = buildEntity(5202L);
		second.setIsPrivate(null);
		when(fileRecordReferenceChecker.getExistingBatch(List.of(5201L, 5202L), false))
			.thenReturn(List.of(first, second));
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(minioProvider);

		sysFileRecordService.deleteByIds(List.of(5201L, 5202L), null, 7003L);

		verify(minioProvider, times(2)).delete("public", "a.png");
		verify(sysFileRecordMapper).deletePhysicallyActiveByIds(List.of(5201L, 5202L));
		verify(sysFileRecordMapper, never()).markDeletedByIds(anyList(), any(), any(), anyString());
	}

	@Test
	@DisplayName("deleteByIds：混合公开与私有文件分别走物理删除与逻辑删除")
	void deleteByIdsSplitsPublicAndPrivateDeletionPaths() {
		// 验证同一批次内公开与私有文件会分别走物理删除与逻辑删除。
		SysFileRecordEntity publicFile = buildEntity(5301L);
		publicFile.setIsPrivate(false);
		SysFileRecordEntity privateFile = buildEntity(5302L);
		privateFile.setIsPrivate(true);
		when(fileRecordReferenceChecker.getExistingBatch(List.of(5301L, 5302L), false))
			.thenReturn(List.of(publicFile, privateFile));
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(minioProvider);

		sysFileRecordService.deleteByIds(List.of(5301L, 5302L), 9001L, 9001L);

		verify(minioProvider).delete("public", "a.png");
		verify(sysFileRecordMapper).deletePhysicallyActiveByIds(List.of(5301L));
		verify(sysFileRecordMapper).markDeletedByIds(List.of(5302L), 9001L, 9001L, FileDeleteSourceConstants.USER_SELF);
	}

	@Test
	@DisplayName("batchDownload：为空参数时抛出 PARAM_REQUIRED")
	void batchDownloadThrowsWhenIdsEmpty() {
		// 验证批量下载参数为空时按参数校验规则返回异常。
		List<Long> emptyDownloadIds = List.of();
		assertThatThrownBy(() -> sysFileRecordService.batchDownload(emptyDownloadIds, null))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);
		verify(fileRecordReferenceChecker, never()).getExistingBatch(anyList(), anyBoolean());
	}

	@Test
	@DisplayName("batchDownload：管理端批量下载输出 ZIP 并处理同名文件")
	void batchDownloadBuildsZipForAdminAndResolvesDuplicateNames() throws IOException {
		// 验证管理端批量下载会输出 ZIP，并对同名文件自动重命名。
		SysFileRecordEntity first = buildEntity(6001L);
		first.setOriginalName("report.txt");
		first.setObjectKey("reports/6001.txt");
		SysFileRecordEntity second = buildEntity(6002L);
		second.setOriginalName("report.txt");
		second.setObjectKey("reports/6002.txt");

		when(fileRecordReferenceChecker.getExistingBatch(List.of(6001L, 6002L), false))
			.thenReturn(List.of(first, second));
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(minioProvider);
		when(minioProvider.download("public", "reports/6001.txt"))
			.thenReturn(new ByteArrayInputStream("content-1".getBytes()));
		when(minioProvider.download("public", "reports/6002.txt"))
			.thenReturn(new ByteArrayInputStream("content-2".getBytes()));

		MockHttpServletResponse response = writeBatchDownloadToResponse(List.of(6001L, 6002L), null);

		assertThat(response.getContentType()).isEqualTo("application/zip");
		assertThat(response.getHeader("Content-Disposition")).contains("attachment");

		Map<String, String> zipEntries = readZipEntries(response.getContentAsByteArray());
		assertThat(zipEntries).containsEntry("report.txt", "content-1").containsEntry("report(1).txt", "content-2");
		verify(fileRecordReferenceChecker, never()).requireOwnerBatch(anyList(), anyLong());
	}

	@Test
	@DisplayName("batchDownload：个人端下载会执行 owner 校验")
	void batchDownloadChecksOwnerWhenOwnerUserIdPresent() throws IOException {
		// 验证个人端批量下载会进行归属校验，防止越权读取。
		SysFileRecordEntity entity = buildEntity(7001L);
		when(fileRecordReferenceChecker.getExistingBatch(List.of(7001L), false)).thenReturn(List.of(entity));
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(minioProvider);
		when(minioProvider.download("public", "a.png")).thenReturn(new ByteArrayInputStream("x".getBytes()));

		writeBatchDownloadToResponse(List.of(7001L), 9009L);

		verify(fileRecordReferenceChecker).requireOwnerBatch(List.of(entity), 9009L);
	}

	@Test
	@DisplayName("updatePrivacyByIds：公开文件切换为私有时迁移对象并更新记录")
	void updatePrivacyByIdsMovesPublicToPrivate() {
		// 验证 public → private 会切换 objectKey 前缀、调用 move 并更新 is_private。
		SysFileRecordEntity entity = buildEntity(8001L);
		entity.setIsPrivate(false);
		entity.setObjectKey("public/avatar/20260702/a.png");
		when(fileRecordReferenceChecker.getExistingBatch(List.of(8001L), false)).thenReturn(List.of(entity));
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(minioProvider);
		when(minioProvider.resolvePublicUrl("public", "private/avatar/20260702/a.png"))
			.thenReturn("https://cdn.example.com/private/avatar/20260702/a.png");

		sysFileRecordService.updatePrivacyByIds(List.of(8001L), true, null, 7001L);

		verify(minioProvider).move("public", "public/avatar/20260702/a.png", "private/avatar/20260702/a.png");
		verify(sysFileRecordMapper).updatePrivacyById(8001L, "private/avatar/20260702/a.png", true,
				"https://cdn.example.com/private/avatar/20260702/a.png", 7001L);
	}

	@Test
	@DisplayName("updatePrivacyByIds：私有文件切换为公开时迁移对象并更新记录")
	void updatePrivacyByIdsMovesPrivateToPublic() {
		// 验证 private → public 会切换 objectKey 前缀、调用 move 并更新 is_private。
		SysFileRecordEntity entity = buildEntity(8002L);
		entity.setIsPrivate(true);
		entity.setObjectKey("private/attachment/20260702/a.txt");
		when(fileRecordReferenceChecker.getExistingBatch(List.of(8002L), false)).thenReturn(List.of(entity));
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(minioProvider);
		when(minioProvider.resolvePublicUrl("public", "public/attachment/20260702/a.txt"))
			.thenReturn("https://cdn.example.com/public/attachment/20260702/a.txt");

		sysFileRecordService.updatePrivacyByIds(List.of(8002L), false, null, 7002L);

		verify(minioProvider).move("public", "private/attachment/20260702/a.txt", "public/attachment/20260702/a.txt");
		verify(sysFileRecordMapper).updatePrivacyById(8002L, "public/attachment/20260702/a.txt", false,
				"https://cdn.example.com/public/attachment/20260702/a.txt", 7002L);
	}

	@Test
	@DisplayName("updatePrivacyByIds：目标隐私与当前一致时短路跳过")
	void updatePrivacyByIdsSkipsWhenPrivacyUnchanged() {
		// 验证 is_private 无变化时不调用 move 也不更新数据库。
		SysFileRecordEntity entity = buildEntity(8003L);
		entity.setIsPrivate(true);
		entity.setObjectKey("private/attachment/20260702/a.txt");
		when(fileRecordReferenceChecker.getExistingBatch(List.of(8003L), false)).thenReturn(List.of(entity));

		sysFileRecordService.updatePrivacyByIds(List.of(8003L), true, null, 7003L);

		verify(facadeRegistry, never()).resolve(any());
		verify(sysFileRecordMapper, never()).updatePrivacyById(anyLong(), anyString(), anyBoolean(), anyString(),
				anyLong());
	}

	@Test
	@DisplayName("updatePrivacyByIds：已逻辑删除文件不可切换")
	void updatePrivacyByIdsThrowsWhenFileDeleted() {
		// 验证已逻辑删除文件在存在性校验阶段被拒绝。
		List<Long> privacyIds = List.of(8004L);
		doThrow(new FileStorageException(FileUploadResultCode.FILE_RECORD_NOT_FOUND, privacyIds))
			.when(fileRecordReferenceChecker)
			.getExistingBatch(privacyIds, false);

		assertThatThrownBy(() -> sysFileRecordService.updatePrivacyByIds(privacyIds, true, null, 7004L))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		verify(facadeRegistry, never()).resolve(any());
	}

	private MockHttpServletResponse writeBatchDownloadToResponse(List<Long> ids, Long ownerUserId) throws IOException {
		StreamingResponseBody body = sysFileRecordService.batchDownload(ids, ownerUserId);
		ResponseEntity<StreamingResponseBody> downloadResponse = FileDelivery.deliver(body,
				SysFileRecordService.BATCH_DOWNLOAD_FILE_NAME, SysFileRecordService.BATCH_DOWNLOAD_CONTENT_TYPE);
		MockHttpServletResponse response = new MockHttpServletResponse();
		HttpHeaders headers = downloadResponse.getHeaders();
		if (headers.getContentType() != null) {
			response.setContentType(headers.getContentType().toString());
		}
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, headers.getFirst(HttpHeaders.CONTENT_DISPOSITION));
		Assertions.assertNotNull(downloadResponse.getBody());
		downloadResponse.getBody().writeTo(response.getOutputStream());
		return response;
	}

	private Map<String, String> readZipEntries(byte[] content) throws IOException {
		Map<String, String> entries = new LinkedHashMap<>();
		try (InputStream inputStream = new ByteArrayInputStream(content);
				ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
			ZipEntry entry;
			while ((entry = zipInputStream.getNextEntry()) != null) {
				entries.put(entry.getName(), new String(zipInputStream.readAllBytes(), StandardCharsets.UTF_8));
				zipInputStream.closeEntry();
			}
		}
		return entries;
	}

	private SysFileRecordEntity buildEntity(Long id) {
		SysFileRecordEntity entity = new SysFileRecordEntity();
		entity.setId(id);
		entity.setStoragePlatform(StoragePlatformEnum.MINIO);
		entity.setContentType("image/png");
		entity.setUrl("https://cdn.example.com/a.png");
		entity.setOriginalName("a.png");
		entity.setBucket("public");
		entity.setObjectKey("a.png");
		entity.setCreateUser(1L);
		return entity;
	}

	private SysFileRecordPageRowPO buildPageRow(Long id) {
		SysFileRecordPageRowPO row = new SysFileRecordPageRowPO();
		row.setId(id);
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
