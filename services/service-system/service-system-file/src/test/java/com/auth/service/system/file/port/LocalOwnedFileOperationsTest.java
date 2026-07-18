package com.auth.service.system.file.port;

import com.auth.module.file.api.model.request.OwnedFileAssertByUrlRequest;
import com.auth.module.file.api.model.request.OwnedFileDeleteByUrlRequest;
import com.auth.service.system.file.service.FileRecordService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/**
 * {@link LocalOwnedFileOperations} 单元测试。
 */
@DisplayName("LocalOwnedFileOperations 进程内文件归属操作")
@ExtendWith(MockitoExtension.class)
class LocalOwnedFileOperationsTest {

	@Mock
	private FileRecordService fileRecordService;

	@InjectMocks
	private LocalOwnedFileOperations localOwnedFileOperations;

	@Test
	@DisplayName("assertOwnedFileUrl：委托 FileRecordService")
	void assertOwnedFileUrl_delegatesToFileRecordService() {
		OwnedFileAssertByUrlRequest request = OwnedFileAssertByUrlRequest.builder()
			.url("https://cdn.example.com/avatar.png")
			.ownerUserId(1L)
			.bizType("AVATAR")
			.build();

		localOwnedFileOperations.assertOwnedFileUrl(request);

		verify(fileRecordService).assertOwnedFileUrl(request);
	}

	@Test
	@DisplayName("tryDeleteOwnedByUrl：委托 FileRecordService")
	void tryDeleteOwnedByUrl_delegatesToFileRecordService() {
		OwnedFileDeleteByUrlRequest request = OwnedFileDeleteByUrlRequest.builder()
			.url("https://cdn.example.com/avatar-old.png")
			.ownerUserId(1L)
			.bizType("AVATAR")
			.deleteSource("SYSTEM_ACTION")
			.build();

		localOwnedFileOperations.tryDeleteOwnedByUrl(request);

		verify(fileRecordService).tryDeleteOwnedByUrl(request);
	}

}
