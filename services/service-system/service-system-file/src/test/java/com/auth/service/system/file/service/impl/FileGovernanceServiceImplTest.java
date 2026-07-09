package com.auth.service.system.file.service.impl;

import com.auth.module.file.api.model.enums.FileDeleteSource;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.module.file.api.model.request.OwnedFileAssertByUrlRequest;
import com.auth.module.file.api.model.request.OwnedFileDeleteByUrlRequest;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.support.FileRecordDeletionSupport;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link FileGovernanceServiceImpl} 单元测试
 */
@DisplayName("FileGovernanceServiceImpl 文件治理")
@ExtendWith(MockitoExtension.class)
class FileGovernanceServiceImplTest {

	private static final Long OWNER_USER_ID = 9001L;

	private static final String AVATAR_URL = "https://cdn.example.com/public/avatar/20260702/a.png";

	private static final String AVATAR_OBJECT_KEY = "public/avatar/20260702/a.png";

	@Mock
	private FileRecordMapper fileRecordMapper;

	@Mock
	private FileRecordDeletionSupport fileRecordDeletionSupport;

	@InjectMocks
	private FileGovernanceServiceImpl fileGovernanceService;

	private static FileRecordEntity buildEntity() {
		FileRecordEntity entity = new FileRecordEntity();
		entity.setId(100L);
		entity.setStoragePlatform(StoragePlatformEnum.MINIO);
		entity.setBucket("public");
		entity.setObjectKey(AVATAR_OBJECT_KEY);
		entity.setUrl(AVATAR_URL);
		entity.setIsPrivate(false);
		entity.setBizType("avatar");
		entity.setCreateUser(OWNER_USER_ID);
		return entity;
	}

	@Test
	@DisplayName("assertOwnedFileUrl：命中记录时不抛异常")
	void assertOwnedFileUrl_success() {
		// 按 URL 查询活跃记录
		FileRecordEntity entity = buildEntity();
		when(fileRecordMapper.selectActiveByUrlAndOwner(eq(AVATAR_URL), isNull(), eq(OWNER_USER_ID), eq("avatar")))
			.thenReturn(entity);

		assertThatCode(() -> fileGovernanceService.assertOwnedFileUrl(OwnedFileAssertByUrlRequest.builder()
			.url(AVATAR_URL)
			.ownerUserId(OWNER_USER_ID)
			.bizType("avatar")
			.build())).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("assertOwnedFileUrl：未命中时抛出 FILE_RECORD_NOT_FOUND")
	void assertOwnedFileUrl_notFound() {
		// 未命中活跃记录
		when(fileRecordMapper.selectActiveByUrlAndOwner(eq(AVATAR_URL), isNull(), eq(OWNER_USER_ID), eq("avatar")))
			.thenReturn(null);

		ThrowingCallable executable = () -> fileGovernanceService
			.assertOwnedFileUrl(OwnedFileAssertByUrlRequest.builder()
				.url(AVATAR_URL)
				.ownerUserId(OWNER_USER_ID)
				.bizType("avatar")
				.build());
		assertThatThrownBy(executable).isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
	}

	@Test
	@DisplayName("tryDeleteOwnedByUrl：未命中时静默跳过")
	void tryDeleteOwnedByUrl_notFoundSkips() {
		String url = "https://cdn.example.com/public/avatar/20260702/old.png";
		String objectKey = "public/avatar/20260702/old.png";
		// 按 objectKey 查询未命中
		when(fileRecordMapper.selectActiveByUrlAndOwner(isNull(), eq(objectKey), eq(OWNER_USER_ID), eq("avatar")))
			.thenReturn(null);

		fileGovernanceService.tryDeleteOwnedByUrl(OwnedFileDeleteByUrlRequest.builder()
			.url(url)
			.ownerUserId(OWNER_USER_ID)
			.deleteSource(FileDeleteSource.SYSTEM_ACTION.getCode())
			.bizType("avatar")
			.build());

		verify(fileRecordDeletionSupport, never()).applyDeletion(anyList(),
				eq(FileDeleteSource.SYSTEM_ACTION.getCode()), eq(OWNER_USER_ID));
	}

	@Test
	@DisplayName("tryDeleteOwnedByUrl：命中记录时执行删除")
	void tryDeleteOwnedByUrl_deletesWhenFound() {
		FileRecordEntity entity = buildEntity();
		// 按 objectKey 查询命中
		when(fileRecordMapper.selectActiveByUrlAndOwner(isNull(), eq(AVATAR_OBJECT_KEY), eq(OWNER_USER_ID),
				eq("avatar")))
			.thenReturn(entity);

		fileGovernanceService.tryDeleteOwnedByUrl(OwnedFileDeleteByUrlRequest.builder()
			.url(AVATAR_URL)
			.ownerUserId(OWNER_USER_ID)
			.deleteSource(FileDeleteSource.SYSTEM_ACTION.getCode())
			.bizType("avatar")
			.build());

		verify(fileRecordDeletionSupport).applyDeletion(anyList(), eq(FileDeleteSource.SYSTEM_ACTION.getCode()),
				eq(OWNER_USER_ID));
	}

}
