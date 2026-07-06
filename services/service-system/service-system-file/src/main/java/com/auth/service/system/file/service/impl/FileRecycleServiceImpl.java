package com.auth.service.system.file.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.file.api.constants.FileDeleteSourceConstants;
import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.SysFileRecordMapper;
import com.auth.service.system.file.model.entity.SysFileRecordEntity;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.auth.service.system.file.service.FileRecycleService;
import com.auth.service.system.file.storage.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import com.auth.service.system.file.support.FileRecordPageAssembler;
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

	private final SysFileRecordMapper sysFileRecordMapper;

	private final FileRecordPageAssembler fileRecordPageAssembler;

	private final FileRecordReferenceChecker fileRecordReferenceChecker;

	private final StoragePlatformFacadeRegistry facadeRegistry;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<FileRecordPageVO> getPage(FileRecordPageQuery query) {
		// 按入口注入回收站分页范围
		query.setPrivateOnly(true);
		if (query.getOwnerUserId() != null) {
			query.setDeleteSource(FileDeleteSourceConstants.USER_SELF);
		}

		return fileRecordPageAssembler.assemblePage(query, sysFileRecordMapper::selectRecycleListByPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void restoreByIds(List<Long> ids, Long ownerUserId, Long operatorUserId) {
		List<SysFileRecordEntity> entities = resolveDeletedEntities(ids, ownerUserId);
		requireRestorablePrivateFiles(entities, ownerUserId);
		List<Long> normalizedIds = entities.stream().map(SysFileRecordEntity::getId).toList();
		sysFileRecordMapper.restoreByIds(normalizedIds, operatorUserId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void purgeByIds(List<Long> ids, Long ownerUserId) {
		List<SysFileRecordEntity> entities = resolveDeletedEntities(ids, ownerUserId);
		requireRestorablePrivateFiles(entities, ownerUserId);
		for (SysFileRecordEntity entity : entities) {
			StoragePlatformEnum platform = entity.getStoragePlatform();
			FileStorageProvider provider = facadeRegistry.resolve(platform).provider();
			provider.delete(entity.getBucket(), entity.getObjectKey());
		}

		List<Long> normalizedIds = entities.stream().map(SysFileRecordEntity::getId).toList();
		sysFileRecordMapper.deletePhysicallyByIds(normalizedIds);
	}

	/**
	 * 校验仅允许恢复/彻底删除逻辑删除的私有文件
	 * @param entities 已删除文件记录
	 * @param ownerUserId 归属用户 ID，非空时仅允许 USER_SELF 来源
	 */
	private void requireRestorablePrivateFiles(List<SysFileRecordEntity> entities, Long ownerUserId) {
		boolean hasInvalidRecord = entities.stream().anyMatch(entity -> {
			if (!Boolean.TRUE.equals(entity.getIsPrivate())) {
				return true;
			}
			return ownerUserId != null && !FileDeleteSourceConstants.USER_SELF.equals(entity.getDeleteSource());
		});
		if (hasInvalidRecord) {
			throw new FileStorageException(FileUploadResultCode.FILE_ACCESS_DENIED);
		}
	}

	/**
	 * 归一化批量 ID，加载已删除记录并在需要时校验归属
	 * @param ids 原始 ID 列表
	 * @param ownerUserId 归属用户 ID，为 {@code null} 时跳过归属校验
	 * @return 已校验的已删除文件记录
	 */
	private List<SysFileRecordEntity> resolveDeletedEntities(List<Long> ids, Long ownerUserId) {
		List<Long> normalizedIds = ids == null ? List.of() : ids.stream().filter(Objects::nonNull).distinct().toList();
		if (normalizedIds.isEmpty()) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED, "ids");
		}

		List<SysFileRecordEntity> entities = fileRecordReferenceChecker.getExistingBatch(normalizedIds, true);
		if (ownerUserId != null) {
			fileRecordReferenceChecker.requireOwnerBatch(entities, ownerUserId);
		}
		return entities;
	}

}
