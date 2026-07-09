package com.auth.service.system.file.controller;

import com.auth.module.file.api.model.enums.FileDeleteSource;
import com.auth.service.system.file.service.FileRecordQueryService;
import com.auth.service.system.file.service.FileRecordService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

/**
 * {@link FileRecordController} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileRecordController 文件治理")
@ExtendWith(MockitoExtension.class)
class FileRecordControllerTest {

	@Mock
	private FileRecordQueryService fileRecordQueryService;

	@Mock
	private FileRecordService fileRecordService;

	@Test
	@DisplayName("delete：管理端删除写入 SYSTEM_ACTION 且不限归属")
	void deletePassesSystemActionAndNullOwner() {
		// 验证管理端删除不限归属用户，并标记 SYSTEM_ACTION 来源。
		FileRecordController controller = new FileRecordController(fileRecordQueryService, fileRecordService);
		List<Long> ids = List.of(9301L, 9302L);

		controller.delete(ids);

		verify(fileRecordService).deleteByIds(ids, null, FileDeleteSource.SYSTEM_ACTION.getCode());
	}

}
