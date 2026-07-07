package com.auth.service.system.file.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.auth.module.file.api.constants.FileDeleteSourceConstants;
import com.auth.module.file.export.writer.ZipStreamWriter;
import com.auth.module.file.export.writer.ZipStreamWriter.ZipEntryPayload;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.service.FileRecordService;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import com.auth.service.system.file.support.FileRecordReferenceChecker;
import com.auth.service.system.file.utils.FileObjectKeyUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文件记录查询与治理服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class FileRecordServiceImpl extends ServiceImpl<FileRecordMapper, FileRecordEntity>
		implements FileRecordService {

	private final FileRecordReferenceChecker fileRecordReferenceChecker;

	private final StoragePlatformFacadeRegistry facadeRegistry;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteByIds(List<Long> ids, Long ownerUserId, Long operatorUserId) {
		var resolved = resolveBatchFileRecords(ids, ownerUserId);
		String deleteSource = ownerUserId != null ? FileDeleteSourceConstants.USER_SELF
				: FileDeleteSourceConstants.ADMIN_ACTION;

		List<Long> publicIds = new ArrayList<>();
		List<Long> privateIds = new ArrayList<>();
		for (FileRecordEntity entity : resolved.entities()) {
			if (BooleanUtil.isTrue(entity.getIsPrivate())) {
				privateIds.add(entity.getId());
			}
			else {
				publicIds.add(entity.getId());
			}
		}

		if (!publicIds.isEmpty()) {
			List<FileRecordEntity> entities = resolved.entities()
				.stream()
				.filter(entity -> publicIds.contains(entity.getId()))
				.toList();

			for (FileRecordEntity entity : entities) {
				FileStorageProvider provider = facadeRegistry.resolve(entity.getStoragePlatform()).provider();
				provider.delete(entity.getBucket(), entity.getObjectKey());
			}
			baseMapper.deletePhysicallyActiveByIds(publicIds);
		}
		if (!privateIds.isEmpty()) {
			baseMapper.markDeletedByIds(privateIds, operatorUserId, operatorUserId, deleteSource);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public StreamingResponseBody batchDownload(List<Long> ids, Long ownerUserId) {
		var resolved = resolveBatchFileRecords(ids, ownerUserId);

		Map<Long, FileRecordEntity> entityById = resolved.entities()
			.stream()
			.collect(Collectors.toMap(FileRecordEntity::getId, Function.identity()));
		List<FileRecordEntity> orderedEntities = resolved.ids().stream().map(entityById::get).toList();

		List<ZipEntryPayload> payloads = orderedEntities.stream().map(entity -> {
			FileStorageProvider provider = facadeRegistry.resolve(entity.getStoragePlatform()).provider();
			Long id = entity.getId();
			String originalName = entity.getOriginalName();
			String extension = entity.getExtension();
			String entryName = ZipStreamWriter.resolveEntryName(originalName, extension, "file-" + id);
			return new ZipEntryPayload(entryName, () -> {
				String bucket = entity.getBucket();
				String objectKey = entity.getObjectKey();

				return provider.download(bucket, objectKey);
			});
		}).toList();

		return outputStream -> {
			try {
				ZipStreamWriter.write(outputStream, payloads);
			}
			catch (IOException exception) {
				throw new SystemBusinessException(SystemCommonResultCode.OPERATION_FAILED, exception.getMessage());
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updatePrivacyByIds(List<Long> ids, boolean targetPrivate, Long ownerUserId, Long operatorUserId) {
		var resolved = resolveBatchFileRecords(ids, ownerUserId);

		for (FileRecordEntity entity : resolved.entities()) {
			boolean currentPrivate = BooleanUtil.isTrue(entity.getIsPrivate());
			if (currentPrivate == targetPrivate) {
				continue;
			}

			String newObjectKey = FileObjectKeyUtil.switchVisibilityPrefix(entity.getObjectKey(), targetPrivate);
			FileStorageProvider provider = facadeRegistry.resolve(entity.getStoragePlatform()).provider();
			provider.move(entity.getBucket(), entity.getObjectKey(), newObjectKey);

			String newUrl = provider.resolvePublicUrl(entity.getBucket(), newObjectKey);
			baseMapper.updatePrivacyById(entity.getId(), newObjectKey, targetPrivate, newUrl, operatorUserId);
		}
	}

	/**
	 * 归一化批量 ID，加载活跃记录并在需要时校验归属
	 * @param ids 原始 ID 列表
	 * @param ownerUserId 归属用户 ID，为 {@code null} 时跳过归属校验
	 * @return 归一化后的 ID 与对应实体
	 */
	private ResolvedBatchFileRecords resolveBatchFileRecords(List<Long> ids, Long ownerUserId) {
		List<Long> normalizedIds = ids == null ? List.of() : ids.stream().filter(Objects::nonNull).distinct().toList();
		if (normalizedIds.isEmpty()) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED, "ids");
		}

		List<FileRecordEntity> entities = fileRecordReferenceChecker.getExistingBatch(normalizedIds, false);
		if (ownerUserId != null) {
			fileRecordReferenceChecker.requireOwnerBatch(entities, ownerUserId);
		}
		return new ResolvedBatchFileRecords(normalizedIds, entities);
	}

	/**
	 * 批量操作共用的 ID 归一化、存在性校验与归属校验结果
	 *
	 * @param ids 原始 ID 列表
	 * @param entities 已校验的活跃文件记录
	 */
	private record ResolvedBatchFileRecords(List<Long> ids, List<FileRecordEntity> entities) {
	}

}
