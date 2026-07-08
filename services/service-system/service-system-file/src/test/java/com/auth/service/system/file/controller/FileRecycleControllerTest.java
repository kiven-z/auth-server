package com.auth.service.system.file.controller;

import com.auth.service.system.file.service.FileRecycleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

/**
 * {@link FileRecycleController} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileRecycleController 回收站")
@ExtendWith(MockitoExtension.class)
class FileRecycleControllerTest {

	@Mock
	private FileRecycleService fileRecycleService;

	@Test
	@DisplayName("restore：管理端不做归属与删除来源限制")
	void restorePassesNullOwnerAndDeleteSource() {
		// 验证管理端恢复不限归属用户与删除来源，由 Service 统一校验。
		FileRecycleController controller = new FileRecycleController(null, fileRecycleService);
		List<Long> ids = List.of(9101L, 9102L);

		controller.restore(ids);

		verify(fileRecycleService).restoreByIds(ids, null, null);
	}

	@Test
	@DisplayName("purge：管理端不做归属与删除来源限制")
	void purgePassesNullOwnerAndDeleteSource() {
		// 验证管理端彻底删除不限归属用户与删除来源，由 Service 统一校验。
		FileRecycleController controller = new FileRecycleController(null, fileRecycleService);
		List<Long> ids = List.of(9103L, 9104L);

		controller.purge(ids);

		verify(fileRecycleService).purgeByIds(ids, null, null);
	}

}
