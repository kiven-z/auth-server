package com.auth.service.system.file.controller;

import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.file.service.FileRecycleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.mockStatic;
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
	@DisplayName("restore：管理端归属为空时透传操作用户")
	void restorePassesNullOwnerAndOperatorUser() {
		// 验证管理端恢复不做归属限制，但会传递操作用户用于审计。
		FileRecycleController controller = new FileRecycleController(fileRecycleService);
		List<Long> ids = List.of(9101L, 9102L);
		try (MockedStatic<SecurityUserUtils> securityUser = mockStatic(SecurityUserUtils.class)) {
			securityUser.when(SecurityUserUtils::getUserId).thenReturn(7001L);

			controller.restore(ids);
		}

		verify(fileRecycleService).restoreByIds(ids, null, 7001L);
	}

}
