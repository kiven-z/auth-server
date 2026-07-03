package com.auth.service.system.file.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.config.FileUploadProperties;
import com.auth.service.system.file.convert.FileUploadConverter;
import com.auth.service.system.file.convert.FileUploadRecordAssembler;
import com.auth.service.system.file.exception.FileException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.SysFileRecordMapper;
import com.auth.service.system.file.model.entity.SysFileRecordEntity;
import com.auth.service.system.file.model.form.FileUploadForm;
import com.auth.service.system.file.model.value.FileUploadCommand;
import com.auth.service.system.file.model.value.StoredFile;
import com.auth.service.system.file.model.vo.FileUploadResponseVO;
import com.auth.service.system.file.service.FileUploadService;
import com.auth.service.system.file.storage.FileStorageProvider;
import com.auth.service.system.file.storage.FileStorageProviderRegistry;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReUtil;
import lombok.RequiredArgsConstructor;

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

	private final FileStorageProviderRegistry storageProviderRegistry;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public FileUploadResponseVO upload(FileUploadForm form) {
		if (form == null || form.getFile() == null || form.getFile().isEmpty()) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED, "file");
		}

		StoragePlatformEnum platform = resolvePlatform(form.getStoragePlatform());
		FileStorageProvider provider = storageProviderRegistry.resolve(platform);
		FileUploadCommand command = FileUploadCommand.builder()
			.storagePlatform(platform)
			.file(form.getFile())
			.objectKey(buildObjectKey(form.getBizType(), form.getFile().getOriginalFilename()))
			.build();

		StoredFile storedFile = provider.upload(command);
		SysFileRecordEntity entity = FileUploadRecordAssembler.toEntity(storedFile, form);
		save(entity);

		return FileUploadConverter.INSTANCE.toUploadResponse(entity);
	}

	/**
	 * 解析本次上传的平台
	 * @param requestPlatform 请求平台
	 * @return 存储平台
	 */
	private StoragePlatformEnum resolvePlatform(String requestPlatform) {
		try {
			StoragePlatformEnum platform = StoragePlatformEnum.fromNullable(requestPlatform);
			if (platform != null) {
				return platform;
			}
		}
		catch (IllegalArgumentException exception) {
			throw new FileException(FileUploadResultCode.FILE_STORAGE_PLATFORM_UNSUPPORTED, requestPlatform);
		}
		if (fileUploadProperties.getDefaultPlatform() == null) {
			throw new FileException(FileUploadResultCode.FILE_STORAGE_CONFIG_MISSING, "auth.file.default-platform");
		}
		return fileUploadProperties.getDefaultPlatform();
	}

	/**
	 * 生成对象键，避免同名覆盖
	 * @param bizType 业务类型
	 * @param originalFilename 原始文件名
	 * @return 对象键
	 */
	private String buildObjectKey(String bizType, String originalFilename) {
		String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
		String extension = FileUtil.extName(originalFilename);
		String randomName = UUID.fastUUID().toString(true);
		String filename = CharSequenceUtil.isBlank(extension) ? randomName : randomName + "." + extension;

		if (CharSequenceUtil.isBlank(bizType)) {
			return dateDir + "/" + filename;
		}

		bizType = ReUtil.replaceAll(CharSequenceUtil.trim(bizType), "[^a-zA-Z0-9_-]", "_");
		return bizType + "/" + dateDir + "/" + filename;
	}

}
