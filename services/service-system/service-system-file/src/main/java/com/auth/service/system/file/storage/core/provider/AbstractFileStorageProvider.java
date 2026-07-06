package com.auth.service.system.file.storage.core.provider;

import java.io.InputStream;

import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.model.value.FileUploadCommand;
import com.auth.service.system.file.model.value.StoredFile;
import com.auth.service.system.file.storage.core.classifier.StorageExceptionClassifier;
import com.auth.service.system.file.storage.core.validator.StoragePlatformConfigValidator;

import lombok.extern.slf4j.Slf4j;

/**
 * 文件存储 Provider 抽象基类
 *
 * <p>
 * 集中处理 SDK 异常翻译与 {@link #resolvePublicUrl(String, String)} 前的配置校验， 子类只保留平台 SDK 的差异部分。
 * </p>
 *
 * @param <C> 平台客户端类型
 * @author Bunny
 */
@Slf4j
public abstract class AbstractFileStorageProvider<C> implements FileStorageProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final StoredFile upload(FileUploadCommand command) {
		try {
			return doUpload(client(), command);
		}
		catch (Exception exception) {
			throw translate("upload", exception);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final InputStream download(String bucket, String objectKey) {
		try {
			return doDownload(client(), bucket, objectKey);
		}
		catch (Exception exception) {
			throw translate("download", exception);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String presignGetUrl(String bucket, String objectKey, int expireSeconds) {
		try {
			return doPresignGetUrl(client(), bucket, objectKey, expireSeconds);
		}
		catch (Exception exception) {
			throw translate("presignGetUrl", exception);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String resolvePublicUrl(String bucket, String objectKey) {
		validator().validateOrThrow();
		return doResolvePublicUrl(bucket, objectKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean objectExists(String bucket, String objectKey) {
		try {
			return doObjectExists(client(), bucket, objectKey);
		}
		catch (Exception exception) {
			throw translate("objectExists", exception);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void delete(String bucket, String objectKey) {
		try {
			doDelete(client(), bucket, objectKey);
		}
		catch (Exception exception) {
			if (exceptionClassifier().isNotFound(exception)) {
				log.warn("Storage object already absent, skip delete: bucket={}, objectKey={}, reason={}", bucket,
						objectKey, exception.getMessage());
				return;
			}
			throw translate("delete", exception);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void move(String bucket, String sourceObjectKey, String destinationObjectKey) {
		if (sourceObjectKey.equals(destinationObjectKey)) {
			return;
		}
		try {
			doMove(client(), bucket, sourceObjectKey, destinationObjectKey);
		}
		catch (Exception exception) {
			throw translate("move", exception);
		}
	}

	/**
	 * 将 SDK 抛出的异常翻译为业务异常，默认统一映射为 {@link FileUploadResultCode#FILE_UPLOAD_FAILED}。
	 * @param op 触发异常的操作名（upload/download/presignGetUrl/delete）
	 * @param exception SDK 抛出的原始异常
	 * @return 已翻译的业务异常
	 */
	protected FileStorageException translate(String op, Exception exception) {
		return new FileStorageException(FileUploadResultCode.FILE_UPLOAD_FAILED, platform().name(),
				exception.getMessage());
	}

	/**
	 * 当前 Provider 所属存储平台
	 * @return 存储平台枚举
	 */
	protected abstract StoragePlatformEnum platform();

	/**
	 * 获取平台客户端实例（由对应 ClientManager 懒加载）
	 * @return 平台客户端
	 */
	protected abstract C client();

	/**
	 * 提供给 {@link #resolvePublicUrl(String, String)} 前置断言的配置校验器
	 * @return 平台配置校验器
	 */
	protected abstract StoragePlatformConfigValidator validator();

	/**
	 * 提供给删除等幂等场景的异常分类器
	 * @return 平台异常分类器
	 */
	protected abstract StorageExceptionClassifier exceptionClassifier();

	/**
	 * 由子类基于平台 SDK 完成上传逻辑
	 * @param client 平台客户端
	 * @param command 文件上传命令
	 * @return 存储文件
	 */
	protected abstract StoredFile doUpload(C client, FileUploadCommand command);

	/**
	 * 由子类基于平台 SDK 完成下载逻辑
	 * @param client 平台客户端
	 * @param bucket 存储桶
	 * @param objectKey 对象键
	 * @return 输入流
	 */
	protected abstract InputStream doDownload(C client, String bucket, String objectKey);

	/**
	 * 由子类基于平台 SDK 生成预签名地址
	 * @param client 平台客户端
	 * @param bucket 存储桶
	 * @param objectKey 对象键
	 * @param expireSeconds 过期时间
	 * @return 预签名地址
	 */
	protected abstract String doPresignGetUrl(C client, String bucket, String objectKey, int expireSeconds);

	/**
	 * 由子类基于平台 SDK 完成对象删除
	 * @param client 平台客户端
	 * @param bucket 存储桶
	 * @param objectKey 对象键
	 */
	protected abstract void doDelete(C client, String bucket, String objectKey);

	/**
	 * 由子类基于平台 SDK 完成对象迁移（同桶 copy + delete）
	 * @param client 平台客户端
	 * @param bucket 存储桶
	 * @param sourceObjectKey 源对象键
	 * @param destinationObjectKey 目标对象键
	 */
	protected abstract void doMove(C client, String bucket, String sourceObjectKey, String destinationObjectKey);

	/**
	 * 由子类基于平台 SDK 判断对象是否存在
	 * @param client 平台客户端
	 * @param bucket 存储桶
	 * @param objectKey 对象键
	 * @return 是否存在
	 */
	protected abstract boolean doObjectExists(C client, String bucket, String objectKey);

	/**
	 * 由子类拼装公开访问地址；无需感知客户端。
	 * @param bucket 存储桶
	 * @param objectKey 对象键
	 * @return 公开访问地址
	 */
	protected abstract String doResolvePublicUrl(String bucket, String objectKey);

}
