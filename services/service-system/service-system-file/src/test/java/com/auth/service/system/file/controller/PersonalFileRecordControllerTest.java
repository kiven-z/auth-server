package com.auth.service.system.file.controller;

import com.auth.module.file.api.model.enums.FileDeleteSource;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.file.service.FileRecordQueryService;
import com.auth.service.system.file.service.FileRecordService;
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
 * {@link PersonalFileRecordController} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("PersonalFileRecordController 个人文件治理")
@ExtendWith(MockitoExtension.class)
class PersonalFileRecordControllerTest {

	@Mock
	private FileRecordQueryService fileRecordQueryService;

	@Mock
	private FileRecordService fileRecordService;

	@Test
	@DisplayName("delete：个人端透传归属用户与 USER_SELF 来源")
	void deletePassesOwnerUserIdAndUserSelfDeleteSource() {
		// 验证个人端删除会限制归属用户并标记 USER_SELF 来源。
		PersonalFileRecordController controller = new PersonalFileRecordController(fileRecordQueryService,
				fileRecordService);
		List<Long> ids = List.of(9401L);
		try (MockedStatic<SecurityUserUtils> securityUser = mockStatic(SecurityUserUtils.class)) {
			securityUser.when(SecurityUserUtils::getUserId).thenReturn(8001L);

			controller.delete(ids);
		}

		verify(fileRecordService).deleteByIds(ids, 8001L, FileDeleteSource.USER_SELF.getCode());
	}

}
