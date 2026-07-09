package com.auth.service.system.file.support;

import com.auth.module.file.api.model.enums.FileDeleteSource;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * {@link FileRecordDeletionSupport} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileRecordDeletionSupport 删除编排")
@ExtendWith(MockitoExtension.class)
class FileRecordDeletionSupportTest {

	@Mock
	private StoragePlatformFacadeRegistry facadeRegistry;

	@Mock
	private FileRecordMapper fileRecordMapper;

	@InjectMocks
	private FileRecordDeletionSupport fileRecordDeletionSupport;

	@Test
	@DisplayName("applyDeletion：未知 deleteSource 时抛出 PARAM_REQUIRED")
	void applyDeletionRejectsUnknownDeleteSource() {
		// 验证删除来源必须在枚举注册范围内。
		FileRecordEntity entity = new FileRecordEntity();
		entity.setId(1L);
		entity.setIsPrivate(true);

		ThrowingCallable executable = () -> fileRecordDeletionSupport.applyDeletion(List.of(entity), "UNKNOWN_SOURCE",
				9001L);
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);

		verify(fileRecordMapper, never()).updateById(entity);
	}

	@Test
	@DisplayName("applyDeletion：合法 deleteSource 时写入逻辑删除字段")
	void applyDeletionAcceptsRegisteredDeleteSource() {
		// 验证合法删除来源会落库到 delete_source。
		FileRecordEntity entity = new FileRecordEntity();
		entity.setId(2L);
		entity.setIsPrivate(true);

		fileRecordDeletionSupport.applyDeletion(List.of(entity), FileDeleteSource.USER_SELF.getCode(), 9001L);

		verify(fileRecordMapper).updateById(entity);
	}

}
