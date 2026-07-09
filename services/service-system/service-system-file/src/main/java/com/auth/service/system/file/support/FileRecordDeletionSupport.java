package com.auth.service.system.file.support;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.module.file.api.model.enums.FileDeleteSource;
import cn.hutool.core.util.BooleanUtil;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件记录删除编排：公开文件物理删除，私有文件逻辑删除
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class FileRecordDeletionSupport {

	private final StoragePlatformFacadeRegistry facadeRegistry;

	private final FileRecordMapper fileRecordMapper;

	/**
	 * 对给定记录执行删除
	 * @param entities 待删除记录
	 * @param deleteSource 逻辑删除来源
	 * @param deletedBy 删除人
	 */
	public void applyDeletion(List<FileRecordEntity> entities, String deleteSource, Long deletedBy) {
		if (CollUtil.isEmpty(entities)) {
			return;
		}

		if (FileDeleteSource.parse(deleteSource).isEmpty()) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED, "deleteSource");
		}

		List<FileRecordEntity> privateEntities = entities.stream()
			.filter(entity -> BooleanUtil.isTrue(entity.getIsPrivate()))
			.toList();
		for (FileRecordEntity entity : privateEntities) {
			entity.setDeletedAt(LocalDateTime.now());
			entity.setDeletedBy(deletedBy);
			entity.setDeleteSource(deleteSource);
			entity.setIsDeleted(true);
		}
		if (CollUtil.isNotEmpty(privateEntities)) {
			for (FileRecordEntity entity : privateEntities) {
				fileRecordMapper.updateById(entity);
			}
		}

		List<FileRecordEntity> publicEntities = entities.stream()
			.filter(entity -> BooleanUtil.isFalse(entity.getIsPrivate()))
			.toList();
		if (publicEntities.isEmpty()) {
			return;
		}

		for (FileRecordEntity entity : publicEntities) {
			FileStorageProvider provider = facadeRegistry.resolve(entity.getStoragePlatform()).provider();
			provider.delete(entity.getBucket(), entity.getObjectKey());
		}
		fileRecordMapper.deletePhysicallyByIds(publicEntities.stream().map(FileRecordEntity::getId).toList(), false);
	}

}
