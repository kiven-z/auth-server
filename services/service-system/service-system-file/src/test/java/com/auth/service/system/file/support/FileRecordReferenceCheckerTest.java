package com.auth.service.system.file.support;

import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.exception.FileException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.SysFileRecordMapper;
import com.auth.service.system.file.model.entity.SysFileRecordEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * {@link FileRecordReferenceChecker} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileRecordReferenceChecker 可见性校验")
@ExtendWith(MockitoExtension.class)
class FileRecordReferenceCheckerTest {

	@Mock
	private SysFileRecordMapper sysFileRecordMapper;

	@InjectMocks
	private FileRecordReferenceChecker fileRecordReferenceChecker;

	@Test
	@DisplayName("getExistingActive：已删除记录在 @TableLogic 下 selectById 返回 null")
	void getExistingActiveThrowsWhenRecordDeleted() {
		// 验证逻辑删除记录在详情查询阶段会被识别为不存在。
		when(sysFileRecordMapper.selectById(1001L)).thenReturn(null);

		assertThatThrownBy(() -> fileRecordReferenceChecker.getExistingActive(1001L)).isInstanceOf(FileException.class)
			.extracting(exception -> ((FileException) exception).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
	}

	@Test
	@DisplayName("getExistingActive：未删除记录可正常返回")
	void getExistingActiveReturnsWhenRecordVisible() {
		// 验证可见记录会按原样返回给后续流程。
		SysFileRecordEntity active = new SysFileRecordEntity();
		active.setId(1002L);
		when(sysFileRecordMapper.selectById(1002L)).thenReturn(active);

		SysFileRecordEntity result = fileRecordReferenceChecker.getExistingActive(1002L);

		assertThat(result).isSameAs(active);
	}

	@Test
	@DisplayName("requireOwner：用户为空时抛出 JWT_INVALID")
	void requireOwnerThrowsWhenOwnerUserIdMissing() {
		// 验证用户上下文缺失时不会放行归属校验。
		SysFileRecordEntity entity = new SysFileRecordEntity();
		entity.setId(1003L);
		entity.setCreateUser(1L);

		assertThatThrownBy(() -> fileRecordReferenceChecker.requireOwner(entity, null))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.JWT_INVALID);
	}

	@Test
	@DisplayName("requireOwner：访问他人记录时抛出 FILE_ACCESS_DENIED")
	void requireOwnerThrowsWhenOwnerMismatch() {
		// 验证当前用户与创建人不一致时拒绝访问。
		SysFileRecordEntity entity = new SysFileRecordEntity();
		entity.setId(1004L);
		entity.setCreateUser(2L);

		assertThatThrownBy(() -> fileRecordReferenceChecker.requireOwner(entity, 1L)).isInstanceOf(FileException.class)
			.extracting(ex -> ((FileException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_ACCESS_DENIED);
	}

	@Test
	@DisplayName("requireOwner：记录无创建人时抛出 FILE_ACCESS_DENIED")
	void requireOwnerThrowsWhenRecordOwnerMissing() {
		// 验证缺失创建人的历史数据不会被个人接口越权访问。
		SysFileRecordEntity entity = new SysFileRecordEntity();
		entity.setId(1005L);

		assertThatThrownBy(() -> fileRecordReferenceChecker.requireOwner(entity, 1L)).isInstanceOf(FileException.class)
			.extracting(ex -> ((FileException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_ACCESS_DENIED);
	}

	@Test
	@DisplayName("requireOwner：创建人与当前用户一致时通过")
	void requireOwnerPassesWhenOwnerMatched() {
		// 验证本人访问本人文件时允许通过校验。
		SysFileRecordEntity entity = new SysFileRecordEntity();
		entity.setId(1006L);
		entity.setCreateUser(9L);

		fileRecordReferenceChecker.requireOwner(entity, 9L);
	}

}
