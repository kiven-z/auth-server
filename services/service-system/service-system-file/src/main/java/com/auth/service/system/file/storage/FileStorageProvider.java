package com.auth.service.system.file.storage;

import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.file.model.value.FileUploadCommand;
import com.auth.service.system.file.model.value.StoredFile;

import java.io.InputStream;

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

	/**
	 * 下载文件输入流
	 * @param bucket 存储桶
	 * @param objectKey 对象键
	 * @return 输入流
	 */
	InputStream download(String bucket, String objectKey);

	/**
	 * 生成对象读取预签名地址
	 * @param bucket 存储桶
	 * @param objectKey 对象键
	 * @param expireSeconds 过期秒数
	 * @return 预签名地址
	 */
	String presignGetUrl(String bucket, String objectKey, int expireSeconds);

	/**
	 * 删除存储对象
	 * @param bucket 存储桶
	 * @param objectKey 对象键
	 */
	void delete(String bucket, String objectKey);

}
