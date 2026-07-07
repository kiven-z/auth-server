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
 * {@link PersonalFileRecycleController} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("PersonalFileRecycleController 回收站")
@ExtendWith(MockitoExtension.class)
class PersonalFileRecycleControllerTest {

	@Mock
	private FileRecycleService fileRecycleService;

	@Test
	@DisplayName("restore：个人端归属用户与操作用户相同")
	void restorePassesSameOwnerAndOperatorUser() {
		// 验证个人端恢复会同时传递归属用户和操作用户。
		PersonalFileRecycleController controller = new PersonalFileRecycleController(null, fileRecycleService);
		List<Long> ids = List.of(9201L);
		try (MockedStatic<SecurityUserUtils> securityUser = mockStatic(SecurityUserUtils.class)) {
			securityUser.when(SecurityUserUtils::getUserId).thenReturn(8001L);

			controller.restore(ids);
		}

		verify(fileRecycleService).restoreByIds(ids, 8001L, 8001L);
	}

}
