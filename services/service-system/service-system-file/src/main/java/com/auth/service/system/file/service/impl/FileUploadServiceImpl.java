package com.auth.service.system.file.service.impl;

import com.auth.module.file.api.enums.FileBizType;
import com.auth.module.file.api.enums.FileBizType.ResolvedVisibility;
import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.convert.FileUploadConverter;
import com.auth.service.system.file.mapper.SysFileRecordMapper;
import com.auth.service.system.file.model.entity.SysFileRecordEntity;
import com.auth.service.system.file.model.form.FileUploadForm;
import com.auth.service.system.file.model.value.FileUploadCommand;
import com.auth.service.system.file.model.value.StoredFile;
import com.auth.service.system.file.model.vo.FileUploadResponseVO;
import com.auth.service.system.file.service.FileUploadService;
import com.auth.service.system.file.storage.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.support.FileObjectKeyUtil;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 文件上传服务实现类
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class FileUploadServiceImpl extends ServiceImpl<SysFileRecordMapper, SysFileRecordEntity>
		implements FileUploadService {

	private final FileUploadProperties fileUploadProperties;

	private final StoragePlatformFacadeRegistry facadeRegistry;

	/**
	 * 组装文件上传记录实体
	 * @param storedFile 存储结果
	 * @param form 上传表单
	 * @param resolvedBizTypeCode 归一化业务类型编码
	 * @param resolvedIsPrivate 最终私有性
	 * @return 文件记录实体
	 */
	public static SysFileRecordEntity toEntity(StoredFile storedFile, FileUploadForm form, String resolvedBizTypeCode,
			boolean resolvedIsPrivate) {
		SysFileRecordEntity entity = new SysFileRecordEntity();
		entity.setStoragePlatform(storedFile.getStoragePlatform());
		entity.setBucket(storedFile.getBucket());
		entity.setObjectKey(storedFile.getObjectKey());
		entity.setUrl(storedFile.getUrl());
		entity.setOriginalName(storedFile.getOriginalName());
		entity.setExtension(storedFile.getExtension());
		entity.setContentType(storedFile.getContentType());
		entity.setSize(storedFile.getSize());
		entity.setEtag(storedFile.getEtag());

		entity.setBizType(resolvedBizTypeCode);
		entity.setBizId(form.getBizId());
		entity.setRemark(form.getRemark());
		entity.setIsPrivate(resolvedIsPrivate);

		entity.setUploadMode("SIMPLE");
		return entity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public FileUploadResponseVO upload(FileUploadForm form) {
		if (form == null || form.getFile() == null || form.getFile().isEmpty()) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED, "file");
		}

		// 解析存储平台
		String storagePlatform = form.getStoragePlatform();
		StoragePlatformEnum platform = StoragePlatformEnum.fromNullable(storagePlatform);
		platform = Objects.requireNonNullElse(platform, fileUploadProperties.getDefaultPlatform());

		ResolvedVisibility visibility = FileBizType.resolveVisibility(form.getBizType(), form.getIsPrivate());
		String bizTypeCode = visibility.bizTypeCode();
		boolean isPrivate = visibility.isPrivate();

		FileStorageProvider provider = facadeRegistry.resolve(platform).provider();
		String objectKey = FileObjectKeyUtil.build(bizTypeCode, isPrivate, form.getFile().getOriginalFilename());
		FileUploadCommand command = FileUploadCommand.builder()
			.storagePlatform(platform)
			.file(form.getFile())
			.objectKey(objectKey)
			.build();

		StoredFile storedFile = provider.upload(command);
		SysFileRecordEntity entity = toEntity(storedFile, form, bizTypeCode, isPrivate);
		save(entity);

		return FileUploadConverter.INSTANCE.toUploadResponse(entity);
	}

}
