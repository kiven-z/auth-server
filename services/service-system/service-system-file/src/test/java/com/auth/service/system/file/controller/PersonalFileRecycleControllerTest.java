package com.auth.service.system.file.controller;

import com.auth.module.file.api.constants.FileDeleteSourceConstants;
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
	@DisplayName("restore：个人端透传归属用户与 USER_SELF 来源")
	void restorePassesOwnerUserIdAndUserSelfDeleteSource() {
		// 验证个人端恢复会限制归属用户与删除来源。
		PersonalFileRecycleController controller = new PersonalFileRecycleController(null, fileRecycleService);
		List<Long> ids = List.of(9201L);
		try (MockedStatic<SecurityUserUtils> securityUser = mockStatic(SecurityUserUtils.class)) {
			securityUser.when(SecurityUserUtils::getUserId).thenReturn(8001L);

			controller.restore(ids);
		}

		verify(fileRecycleService).restoreByIds(ids, 8001L, FileDeleteSourceConstants.USER_SELF);
	}

	@Test
	@DisplayName("purge：个人端透传归属用户与 USER_SELF 来源")
	void purgePassesOwnerUserIdAndUserSelfDeleteSource() {
		// 验证个人端彻底删除会限制归属用户与删除来源。
		PersonalFileRecycleController controller = new PersonalFileRecycleController(null, fileRecycleService);
		List<Long> ids = List.of(9202L);
		try (MockedStatic<SecurityUserUtils> securityUser = mockStatic(SecurityUserUtils.class)) {
			securityUser.when(SecurityUserUtils::getUserId).thenReturn(8001L);

			controller.purge(ids);
		}

		verify(fileRecycleService).purgeByIds(ids, 8001L, FileDeleteSourceConstants.USER_SELF);
	}

}
