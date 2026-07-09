package com.auth.service.system.file.service;

import com.auth.module.file.api.model.request.OwnedFileAssertByUrlRequest;
import com.auth.module.file.api.model.request.OwnedFileDeleteByUrlRequest;

/**
 * 文件 URL 归属校验与按 URL 治理
 *
 * @author Bunny
 */
public interface FileGovernanceService {

	/**
	 * 校验 URL 对应文件是否归属指定用户且匹配业务类型
	 * @param request 请求参数
	 */
	void assertOwnedFileUrl(OwnedFileAssertByUrlRequest request);

	/**
	 * 尝试按 URL 删除归属用户的活跃文件；未命中时静默跳过
	 * @param request 请求参数
	 */
	void tryDeleteOwnedByUrl(OwnedFileDeleteByUrlRequest request);

}
