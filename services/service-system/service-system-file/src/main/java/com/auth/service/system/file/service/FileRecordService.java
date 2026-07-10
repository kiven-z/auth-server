package com.auth.service.system.file.service;

import com.auth.module.file.api.model.request.OwnedFileAssertByUrlRequest;
import com.auth.module.file.api.model.request.OwnedFileDeleteByUrlRequest;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

/**
 * 文件记录查询与治理服务。
 *
 * @author Bunny
 */
public interface FileRecordService extends IService<FileRecordEntity> {

	/**
	 * 批量下载 ZIP 文件名
	 */
	String BATCH_DOWNLOAD_FILE_NAME = "file-records.zip";

	/**
	 * 批量下载 ZIP MIME 类型
	 */
	String BATCH_DOWNLOAD_CONTENT_TYPE = "application/zip";

	/**
	 * 批量下载文件记录并输出 ZIP 流
	 * @param ids 文件记录主键集合
	 * @param ownerUserId 归属用户ID
	 * @return ZIP 流式响应体
	 */
	StreamingResponseBody batchDownload(List<Long> ids, Long ownerUserId);

	/**
	 * 批量切换文件隐私属性（公开 ↔ 私有），并迁移存储对象前缀
	 * @param ids 文件记录主键集合
	 * @param targetPrivate 目标是否私有
	 * @param ownerUserId 归属用户 ID，管理端传 {@code null}
	 */
	void updatePrivacyByIds(List<Long> ids, boolean targetPrivate, Long ownerUserId);

	/**
	 * 按主键集合删除文件记录：公开文件物理删除，私有文件逻辑删除；仅处理当前可见且未删除的记录，其余 ID 忽略。
	 * @param ids 文件记录主键集合
	 * @param ownerUserId 归属用户 ID，非空时仅删除该用户文件
	 * @param deleteSource 逻辑删除时写入的删除来源
	 */
	void deleteByIds(List<Long> ids, Long ownerUserId, String deleteSource);

	/**
	 * 尝试按 URL 删除归属用户的活跃文件；未命中时静默跳过
	 * @param request 请求参数
	 */
	void tryDeleteOwnedByUrl(OwnedFileDeleteByUrlRequest request);

	/**
	 * 校验 URL 对应文件是否归属指定用户且匹配业务类型
	 * @param request 请求参数
	 */
	void assertOwnedFileUrl(OwnedFileAssertByUrlRequest request);

}
