package com.auth.service.system.file.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.common.data.model.PageResponse;
import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.mapper.SysFileRecordMapper;
import com.auth.service.system.file.model.entity.SysFileRecordEntity;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.auth.service.system.file.service.FileRecycleService;
import com.auth.service.system.file.storage.FileStorageProvider;
import com.auth.service.system.file.storage.FileStorageProviderRegistry;
import com.auth.service.system.file.support.FileRecordPageAssembler;
import com.auth.service.system.file.support.FileRecordReferenceChecker;

import lombok.RequiredArgsConstructor;

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

	private final FileStorageProviderRegistry storageProviderRegistry;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<FileRecordPageVO> getPage(FileRecordPageQuery query) {
		return fileRecordPageAssembler.assemblePage(query, sysFileRecordMapper::selectRecycleListByPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void restoreByIds(List<Long> ids, Long ownerUserId, Long operatorUserId) {
		List<SysFileRecordEntity> entities = resolveDeletedEntities(ids, ownerUserId);
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
		for (SysFileRecordEntity entity : entities) {
			StoragePlatformEnum platform = entity.getStoragePlatform();
			FileStorageProvider provider = storageProviderRegistry.resolve(platform);
			provider.delete(entity.getBucket(), entity.getObjectKey());
		}

		List<Long> normalizedIds = entities.stream().map(SysFileRecordEntity::getId).toList();
		sysFileRecordMapper.deletePhysicallyByIds(normalizedIds);
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
