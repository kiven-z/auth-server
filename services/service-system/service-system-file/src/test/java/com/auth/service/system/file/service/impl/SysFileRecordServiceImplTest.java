package com.auth.service.system.file.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.file.exception.FileException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.SysFileRecordMapper;
import com.auth.service.system.file.model.entity.SysFileRecordEntity;
import com.auth.service.system.file.model.po.SysFileRecordPageRowPO;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordDetailVO;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.auth.service.system.file.support.FileRecordReferenceChecker;
import com.auth.service.system.file.support.FileUrlSigner;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

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

	private SysFileRecordServiceImpl sysFileRecordService;

	@BeforeEach
	void setUp() throws Exception {
		sysFileRecordService = spy(
				new SysFileRecordServiceImpl(fileRecordReferenceChecker, fileUrlSigner, auditUserDisplayService));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysFileRecordService, sysFileRecordMapper);
	}

	@Test
	@DisplayName("getDetail：返回详情并补充签名访问地址")
	void getDetailReturnsSignedAccessUrl() {
		// 验证详情查询会补充签名访问地址。
		SysFileRecordEntity entity = buildEntity(1001L);
		when(fileRecordReferenceChecker.getExistingActive(1001L)).thenReturn(entity);
		when(fileUrlSigner.sign(entity.getUrl(), entity.getContentType(), entity.getStoragePlatform()))
			.thenReturn(entity.getUrl() + "?expires=100");

		FileRecordDetailVO detail = sysFileRecordService.getDetail(1001L, null);

		verify(auditUserDisplayService)
			.enrichAuditUsernames(org.mockito.ArgumentMatchers.<List<FileRecordDetailVO>>any(), isNull(), isNull());
		assertThat(detail.getId()).isEqualTo(1001L);
		assertThat(detail.getAccessUrl()).isEqualTo("https://cdn.example.com/a.png?expires=100");
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
		Page<SysFileRecordPageRowPO> page = new Page<>(1, 20, 1);
		page.setRecords(List.of(visible));
		when(sysFileRecordMapper.selectListByPage(org.mockito.ArgumentMatchers.any(), eq(query))).thenReturn(page);
		when(fileUrlSigner.sign(visible.getUrl(), visible.getContentType(), visible.getStoragePlatform()))
			.thenReturn(visible.getUrl() + "?expires=200");

		PageResponse<FileRecordPageVO> pageResult = sysFileRecordService.getPage(query);

		assertThat(pageResult.getList()).hasSize(1);
		assertThat(pageResult.getList().get(0).getAccessUrl()).endsWith("expires=200");
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
		Page<SysFileRecordPageRowPO> page = new Page<>(1, 20, 1);
		page.setRecords(List.of(visible));
		when(sysFileRecordMapper.selectListByPage(org.mockito.ArgumentMatchers.any(), eq(query))).thenReturn(page);
		when(fileUrlSigner.sign(visible.getUrl(), visible.getContentType(), visible.getStoragePlatform()))
			.thenReturn(visible.getUrl() + "?expires=201");

		PageResponse<FileRecordPageVO> pageResult = sysFileRecordService.getPage(query);

		assertThat(pageResult.getList()).hasSize(1);
		verify(sysFileRecordMapper).selectListByPage(org.mockito.ArgumentMatchers.any(),
				argThat(param -> param.getOwnerUserId() == null));
	}

	@Test
	@DisplayName("getDetail：个人访问他人记录时拒绝")
	void getDetailThrowsWhenOwnerMismatch() {
		// 验证个人详情查询会进行归属校验并拒绝访问他人记录。
		SysFileRecordEntity entity = buildEntity(3001L);
		when(fileRecordReferenceChecker.getExistingActive(3001L)).thenReturn(entity);
		doThrow(new FileException(FileUploadResultCode.FILE_ACCESS_DENIED, 3001L)).when(fileRecordReferenceChecker)
			.requireOwner(entity, 9002L);

		assertThatThrownBy(() -> sysFileRecordService.getDetail(3001L, 9002L)).isInstanceOf(FileException.class)
			.extracting(ex -> ((FileException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_ACCESS_DENIED);
	}

	@Test
	@DisplayName("deleteById：个人删除他人记录时拒绝")
	void deleteByIdThrowsWhenOwnerMismatch() {
		// 验证个人删除会进行归属校验并拒绝删除他人记录。
		SysFileRecordEntity entity = buildEntity(3002L);
		when(fileRecordReferenceChecker.getExistingActive(3002L)).thenReturn(entity);
		doThrow(new FileException(FileUploadResultCode.FILE_ACCESS_DENIED, 3002L)).when(fileRecordReferenceChecker)
			.requireOwner(entity, 9003L);

		assertThatThrownBy(() -> sysFileRecordService.deleteById(3002L, 9003L)).isInstanceOf(FileException.class)
			.extracting(ex -> ((FileException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_ACCESS_DENIED);
		verify(sysFileRecordService, never()).removeById(3002L);
	}

	@Test
	@DisplayName("deleteById：存在性校验后逻辑删除")
	void deleteByIdRemovesAfterExistenceCheck() {
		// 验证删除前会校验记录存在并调用逻辑删除。
		SysFileRecordEntity entity = buildEntity(3002L);
		when(fileRecordReferenceChecker.getExistingActive(3002L)).thenReturn(entity);
		doReturn(true).when(sysFileRecordService).removeById(3002L);

		sysFileRecordService.deleteById(3002L, null);

		verify(fileRecordReferenceChecker).getExistingActive(3002L);
		verify(sysFileRecordService).removeById(3002L);
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
