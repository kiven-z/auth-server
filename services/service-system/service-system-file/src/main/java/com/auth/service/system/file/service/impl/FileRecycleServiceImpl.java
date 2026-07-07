package com.auth.service.system.file.service.impl;

import com.auth.module.file.api.constants.FileDeleteSourceConstants;
import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.service.FileRecycleService;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import com.auth.service.system.file.support.FileRecordReferenceChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 文件回收站服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class FileRecycleServiceImpl implements FileRecycleService {

	private final FileRecordMapper fileRecordMapper;

	private final FileRecordReferenceChecker fileRecordReferenceChecker;

	private final StoragePlatformFacadeRegistry facadeRegistry;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void restoreByIds(List<Long> ids, Long ownerUserId, Long operatorUserId) {
		List<FileRecordEntity> entities = resolveDeletedEntities(ids, ownerUserId);
		requireRestorablePrivateFiles(entities, ownerUserId);
		List<Long> normalizedIds = entities.stream().map(FileRecordEntity::getId).toList();
		fileRecordMapper.restoreByIds(normalizedIds, operatorUserId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void purgeByIds(List<Long> ids, Long ownerUserId) {
		List<FileRecordEntity> entities = resolveDeletedEntities(ids, ownerUserId);
		requireRestorablePrivateFiles(entities, ownerUserId);
		for (FileRecordEntity entity : entities) {
			StoragePlatformEnum platform = entity.getStoragePlatform();
			FileStorageProvider provider = facadeRegistry.resolve(platform).provider();
			provider.delete(entity.getBucket(), entity.getObjectKey());
		}

		List<Long> normalizedIds = entities.stream().map(FileRecordEntity::getId).toList();
		fileRecordMapper.deletePhysicallyByIds(normalizedIds);
	}

	/**
	 * 校验仅允许恢复/彻底删除逻辑删除的私有文件
	 * @param entities 已删除文件记录
	 * @param ownerUserId 归属用户 ID，非空时仅允许 USER_SELF 来源
	 */
	private void requireRestorablePrivateFiles(List<FileRecordEntity> entities, Long ownerUserId) {
		List<Long> invalidIds = entities.stream()
			.filter(entity -> !Boolean.TRUE.equals(entity.getIsPrivate())
					|| (ownerUserId != null && !FileDeleteSourceConstants.USER_SELF.equals(entity.getDeleteSource())))
			.map(FileRecordEntity::getId)
			.toList();
		if (!invalidIds.isEmpty()) {
			throw new FileStorageException(FileUploadResultCode.FILE_RECORD_NOT_FOUND, invalidIds);
		}
	}

	/**
	 * 归一化批量 ID，加载已删除记录并在需要时校验归属
	 * @param ids 原始 ID 列表
	 * @param ownerUserId 归属用户 ID，为 {@code null} 时跳过归属校验
	 * @return 已校验的已删除文件记录
	 */
	private List<FileRecordEntity> resolveDeletedEntities(List<Long> ids, Long ownerUserId) {
		List<Long> normalizedIds = ids == null ? List.of() : ids.stream().filter(Objects::nonNull).distinct().toList();
		if (normalizedIds.isEmpty()) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED, "ids");
		}

		List<FileRecordEntity> entities = fileRecordReferenceChecker.getExistingBatch(normalizedIds, true);
		if (ownerUserId != null) {
			fileRecordReferenceChecker.requireOwnerBatch(entities, ownerUserId);
		}
		return entities;
	}

}
