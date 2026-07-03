package com.auth.service.system.file.storage;

import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.file.model.value.FileUploadCommand;
import com.auth.service.system.file.model.value.StoredFile;

/**
 * 文件存储策略接口
 *
 * @author Bunny
 */
public interface FileStorageProvider {

	/**
	 * 当前实现支持的平台
	 * @return 平台
	 */
	StoragePlatformEnum platform();

	/**
	 * 上传文件并返回存储结果
	 * @param command 上传命令
	 * @return 上传结果
	 */
	StoredFile upload(FileUploadCommand command);

}
