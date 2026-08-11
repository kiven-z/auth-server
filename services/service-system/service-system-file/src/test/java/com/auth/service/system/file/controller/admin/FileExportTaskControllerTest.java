package com.auth.service.system.file.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.file.model.query.FileExportTaskAdminPageQuery;
import com.auth.service.system.file.model.vo.FileExportAttemptVO;
import com.auth.service.system.file.model.vo.FileExportTaskAdminDetailVO;
import com.auth.service.system.file.model.vo.FileExportTaskAdminPageVO;
import com.auth.service.system.file.service.FileExportTaskAdminQueryService;
import com.auth.service.system.file.service.FileExportTaskWriteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link FileExportTaskController} 单元测试
 *
 * @author Bunny
 */
@DisplayName("FileExportTaskController 管理端任务")
@ExtendWith(MockitoExtension.class)
class FileExportTaskControllerTest {

	@Mock
	private FileExportTaskAdminQueryService fileExportTaskAdminQueryService;

	@Mock
	private FileExportTaskWriteService fileExportTaskWriteService;

	@Test
	@DisplayName("page：调用管理端分页查询服务")
	void pageDelegatesToAdminQueryService() {
		FileExportTaskController controller = new FileExportTaskController(fileExportTaskAdminQueryService,
				fileExportTaskWriteService);
		FileExportTaskAdminPageQuery query = new FileExportTaskAdminPageQuery();
		PageResponse<FileExportTaskAdminPageVO> expected = PageResponse.of(1L, 20L, 1L,
				List.of(new FileExportTaskAdminPageVO()));
		when(fileExportTaskAdminQueryService.getPage(query)).thenReturn(expected);

		Result<PageResponse<FileExportTaskAdminPageVO>> response = controller.page(query);

		assertThat(response.getData()).isSameAs(expected);
		verify(fileExportTaskAdminQueryService).getPage(query);
	}

	@Test
	@DisplayName("detail：调用管理端详情查询服务")
	void detailDelegatesToAdminQueryService() {
		FileExportTaskController controller = new FileExportTaskController(fileExportTaskAdminQueryService,
				fileExportTaskWriteService);
		FileExportTaskAdminDetailVO expected = new FileExportTaskAdminDetailVO();
		expected.setBizType("SYS_ROLE");
		when(fileExportTaskAdminQueryService.getDetail(801L)).thenReturn(expected);

		Result<FileExportTaskAdminDetailVO> response = controller.detail(801L);

		assertThat(response.getData()).isSameAs(expected);
		verify(fileExportTaskAdminQueryService).getDetail(801L);
	}

	@Test
	@DisplayName("attempts：调用执行尝试查询服务")
	void attemptsDelegatesToAdminQueryService() {
		FileExportTaskController controller = new FileExportTaskController(fileExportTaskAdminQueryService,
				fileExportTaskWriteService);
		FileExportAttemptVO first = new FileExportAttemptVO();
		first.setAttemptNo(1);
		FileExportAttemptVO second = new FileExportAttemptVO();
		second.setAttemptNo(2);
		when(fileExportTaskAdminQueryService.listAttempts(802L)).thenReturn(List.of(first, second));

		Result<List<FileExportAttemptVO>> response = controller.attempts(802L);

		assertThat(response.getData()).hasSize(2);
		assertThat(response.getData()).extracting(FileExportAttemptVO::getAttemptNo).containsExactly(1, 2);
		verify(fileExportTaskAdminQueryService).listAttempts(802L);
	}

	@Test
	@DisplayName("forceCancel：调用写服务强制取消")
	void forceCancelDelegatesToWriteService() {
		FileExportTaskController controller = new FileExportTaskController(fileExportTaskAdminQueryService,
				fileExportTaskWriteService);

		Result<Void> response = controller.forceCancel(803L);

		assertThat(response).isNotNull();
		verify(fileExportTaskWriteService).forceCancelTask(803L);
	}

	@Test
	@DisplayName("forceRetry：调用写服务强制重试")
	void forceRetryDelegatesToWriteService() {
		FileExportTaskController controller = new FileExportTaskController(fileExportTaskAdminQueryService,
				fileExportTaskWriteService);

		Result<Void> response = controller.forceRetry(804L);

		assertThat(response).isNotNull();
		verify(fileExportTaskWriteService).forceRetryTask(804L);
	}

}
