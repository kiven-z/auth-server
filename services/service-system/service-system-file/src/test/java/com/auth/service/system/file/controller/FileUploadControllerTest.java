package com.auth.service.system.file.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.file.api.model.dto.FileUploadResultDTO;
import com.auth.service.system.file.model.form.FileUploadForm;
import com.auth.service.system.file.model.form.MultipleFileUploadForm;
import com.auth.service.system.file.service.FileUploadService;
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
 * {@link FileUploadController} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileUploadController 文件上传")
@ExtendWith(MockitoExtension.class)
class FileUploadControllerTest {

	@Mock
	private FileUploadService fileUploadService;

	@Test
	@DisplayName("upload：委托 FileUploadService.upload")
	void uploadDelegatesToService() {
		// 验证单文件上传接口仅转发至服务层。
		FileUploadController controller = new FileUploadController(fileUploadService);
		FileUploadForm form = new FileUploadForm();
		FileUploadResultDTO response = new FileUploadResultDTO();
		when(fileUploadService.upload(form)).thenReturn(response);

		Result<FileUploadResultDTO> result = controller.upload(form);

		verify(fileUploadService).upload(form);
		assertThat(result.getData()).isSameAs(response);
	}

	@Test
	@DisplayName("uploadMultiple：委托 FileUploadService.uploadMultiple")
	void uploadMultipleDelegatesToService() {
		// 验证多文件上传接口仅转发至服务层。
		FileUploadController controller = new FileUploadController(fileUploadService);
		MultipleFileUploadForm form = new MultipleFileUploadForm();
		List<FileUploadResultDTO> responses = List.of(new FileUploadResultDTO());
		when(fileUploadService.uploadMultiple(form)).thenReturn(responses);

		Result<List<FileUploadResultDTO>> result = controller.uploadMultiple(form);

		verify(fileUploadService).uploadMultiple(form);
		assertThat(result.getData()).isSameAs(responses);
	}

}
