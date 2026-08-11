package com.auth.service.system.file.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.file.mapper.FileExportAttemptMapper;
import com.auth.service.system.file.mapper.FileExportTaskMapper;
import com.auth.service.system.file.model.entity.FileExportAttemptEntity;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.enums.FileExportAttemptResult;
import com.auth.service.system.file.model.enums.FileExportTaskStatus;
import com.auth.service.system.file.model.po.FileExportTaskAdminPageRowPO;
import com.auth.service.system.file.model.query.FileExportTaskAdminPageQuery;
import com.auth.service.system.file.model.vo.FileExportAttemptVO;
import com.auth.service.system.file.model.vo.FileExportTaskAdminDetailVO;
import com.auth.service.system.file.model.vo.FileExportTaskAdminPageVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link FileExportTaskAdminQueryServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("FileExportTaskAdminQueryServiceImpl 管理端查询")
@ExtendWith(MockitoExtension.class)
class FileExportTaskAdminQueryServiceImplTest {

	@Mock
	private FileExportTaskMapper fileExportTaskMapper;

	@Mock
	private FileExportAttemptMapper fileExportAttemptMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private static FileExportAttemptEntity newAttempt(Long id, Long taskId, int attemptNo, String result) {
		FileExportAttemptEntity entity = new FileExportAttemptEntity();
		setBaseId(entity, id);
		entity.setTaskId(taskId);
		entity.setAttemptNo(attemptNo);
		entity.setWorkerId("worker-" + attemptNo);
		entity.setResult(result);
		entity.setProcessedRows(10L);
		entity.setStartedAt(Instant.parse("2026-08-08T10:00:00Z"));
		return entity;
	}

	private static void setBaseId(Object entity, Long id) {
		try {
			Field idField = entity.getClass().getSuperclass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(entity, id);
		}
		catch (ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

	@Test
	@DisplayName("getPage：无创建人过滤时透传查询条件")
	void getPagePassesQueryWithoutCreatedByFilter() {
		FileExportTaskAdminQueryServiceImpl service = newService();
		FileExportTaskAdminPageQuery query = new FileExportTaskAdminPageQuery();
		query.setPageIndex(1);
		query.setPageSize(20);
		query.setStatus(FileExportTaskStatus.FAILED.name());
		query.setErrorCode("EXPORT_FAILED");

		FileExportTaskAdminPageRowPO row = new FileExportTaskAdminPageRowPO();
		row.setId(1001L);
		row.setBizType("SYS_USER");
		row.setStatus(FileExportTaskStatus.FAILED.name());
		row.setErrorCode("EXPORT_FAILED");
		row.setErrorMessage("boom");
		row.setLockedBy("worker-a");
		row.setCreatedBy(88L);

		@SuppressWarnings("unchecked")
		IPage<FileExportTaskAdminPageRowPO> page = org.mockito.Mockito.mock(IPage.class);
		when(page.convert(any())).thenAnswer(invocation -> {
			java.util.function.Function<FileExportTaskAdminPageRowPO, FileExportTaskAdminPageVO> mapper = invocation
				.getArgument(0);
			IPage<FileExportTaskAdminPageVO> voPage = new Page<>(1, 20);
			voPage.setRecords(List.of(mapper.apply(row)));
			voPage.setTotal(1);
			return voPage;
		});
		when(fileExportTaskMapper.selectPageAdmin(any(), eq(query))).thenReturn(page);

		PageResponse<FileExportTaskAdminPageVO> result = service.getPage(query);

		assertThat(result.getList()).hasSize(1);
		FileExportTaskAdminPageVO vo = result.getList().get(0);
		assertThat(vo.getErrorMessage()).isEqualTo("boom");
		assertThat(vo.getLockedBy()).isEqualTo("worker-a");
		assertThat(vo.getCreatedBy()).isEqualTo(88L);
		assertThat(vo.getClass().getDeclaredFields()).extracting(Field::getName).doesNotContain("scopeSnapshotJson");

		ArgumentCaptor<FileExportTaskAdminPageQuery> queryCaptor = ArgumentCaptor
			.forClass(FileExportTaskAdminPageQuery.class);
		verify(fileExportTaskMapper).selectPageAdmin(any(), queryCaptor.capture());
		assertThat(queryCaptor.getValue().getCreatedBy()).isNull();
		assertThat(queryCaptor.getValue().getErrorCode()).isEqualTo("EXPORT_FAILED");
	}

	@Test
	@DisplayName("getPage：按创建人过滤时透传 createdBy")
	void getPagePassesCreatedByFilter() {
		FileExportTaskAdminQueryServiceImpl service = newService();
		FileExportTaskAdminPageQuery query = new FileExportTaskAdminPageQuery();
		query.setPageIndex(1);
		query.setPageSize(10);
		query.setCreatedBy(9001L);

		@SuppressWarnings("unchecked")
		IPage<FileExportTaskAdminPageRowPO> page = org.mockito.Mockito.mock(IPage.class);
		when(page.convert(any())).thenAnswer(invocation -> {
			IPage<FileExportTaskAdminPageVO> voPage = new Page<>(1, 10);
			voPage.setRecords(List.of());
			voPage.setTotal(0);
			return voPage;
		});
		when(fileExportTaskMapper.selectPageAdmin(any(), eq(query))).thenReturn(page);

		service.getPage(query);

		ArgumentCaptor<FileExportTaskAdminPageQuery> queryCaptor = ArgumentCaptor
			.forClass(FileExportTaskAdminPageQuery.class);
		verify(fileExportTaskMapper).selectPageAdmin(any(), queryCaptor.capture());
		assertThat(queryCaptor.getValue().getCreatedBy()).isEqualTo(9001L);
	}

	@Test
	@DisplayName("getDetail：返回租约字段且不含权限快照")
	void getDetailReturnsOpsFieldsWithoutScopeSnapshot() {
		FileExportTaskAdminQueryServiceImpl service = newService();
		FileExportTaskEntity entity = new FileExportTaskEntity();
		setBaseId(entity, 5001L);
		entity.setBizType("SYS_USER");
		entity.setExportFormat("XLSX");
		entity.setQuerySnapshotJson("{\"username\":\"alice\"}");
		entity.setScopeSnapshotJson("{\"userId\":9}");
		entity.setStatus(FileExportTaskStatus.RUNNING.name());
		entity.setErrorMessage(null);
		entity.setLockedBy("worker-1");
		entity.setLeaseUntil(Instant.parse("2026-08-08T12:00:00Z"));
		entity.setCreatedBy(42L);
		when(fileExportTaskMapper.selectById(5001L)).thenReturn(entity);

		FileExportTaskAdminDetailVO detail = service.getDetail(5001L);

		assertThat(detail.getQuerySnapshotJson()).isEqualTo("{\"username\":\"alice\"}");
		assertThat(detail.getLockedBy()).isEqualTo("worker-1");
		assertThat(detail.getLeaseUntil()).isEqualTo(Instant.parse("2026-08-08T12:00:00Z"));
		assertThat(detail.getCreatedBy()).isEqualTo(42L);
		assertThat(detail.getClass().getDeclaredFields()).extracting(Field::getName)
			.doesNotContain("scopeSnapshotJson");
	}

	@Test
	@DisplayName("getDetail：任务不存在抛 DATA_UNAVAILABLE")
	void getDetailThrowsWhenMissing() {
		FileExportTaskAdminQueryServiceImpl service = newService();
		when(fileExportTaskMapper.selectById(404L)).thenReturn(null);

		assertThatThrownBy(() -> service.getDetail(404L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);
	}

	@Test
	@DisplayName("listAttempts：按 attemptNo 升序返回")
	void listAttemptsReturnsOrderedRows() {
		FileExportTaskAdminQueryServiceImpl service = newService();
		FileExportTaskEntity task = new FileExportTaskEntity();
		setBaseId(task, 7001L);
		when(fileExportTaskMapper.selectById(7001L)).thenReturn(task);

		FileExportAttemptEntity first = newAttempt(1L, 7001L, 1, FileExportAttemptResult.FAILED.name());
		FileExportAttemptEntity second = newAttempt(2L, 7001L, 2, FileExportAttemptResult.SUCCESS.name());
		when(fileExportAttemptMapper.selectList(any())).thenReturn(List.of(first, second));

		List<FileExportAttemptVO> attempts = service.listAttempts(7001L);

		assertThat(attempts).extracting(FileExportAttemptVO::getAttemptNo).containsExactly(1, 2);
		assertThat(attempts.get(0).getResult()).isEqualTo(FileExportAttemptResult.FAILED.name());
		assertThat(attempts.get(1).getResult()).isEqualTo(FileExportAttemptResult.SUCCESS.name());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<LambdaQueryWrapper<FileExportAttemptEntity>> wrapperCaptor = ArgumentCaptor
			.forClass(LambdaQueryWrapper.class);
		verify(fileExportAttemptMapper).selectList(wrapperCaptor.capture());
		assertThat(wrapperCaptor.getValue()).isNotNull();
	}

	@Test
	@DisplayName("listAttempts：任务不存在抛 DATA_UNAVAILABLE")
	void listAttemptsThrowsWhenTaskMissing() {
		FileExportTaskAdminQueryServiceImpl service = newService();
		when(fileExportTaskMapper.selectById(7002L)).thenReturn(null);

		assertThatThrownBy(() -> service.listAttempts(7002L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);
	}

	private FileExportTaskAdminQueryServiceImpl newService() {
		return new FileExportTaskAdminQueryServiceImpl(fileExportTaskMapper, fileExportAttemptMapper,
				auditUserDisplayService);
	}

}
