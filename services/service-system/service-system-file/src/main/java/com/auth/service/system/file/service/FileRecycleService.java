package com.auth.service.system.file.service;

import java.util.List;

/**
 * 文件回收站服务
 *
 * @author Bunny
 */
public interface FileRecycleService {

	/**
	 * 按主键集合恢复软删除的文件记录
	 * @param ids 文件记录主键集合
	 * @param ownerUserId 归属用户 ID，为 {@code null} 时跳过归属校验
	 * @param operatorUserId 操作用户 ID，用于审计更新人
	 */
	void restoreByIds(List<Long> ids, Long ownerUserId, Long operatorUserId);

	/**
	 * 按主键集合彻底删除文件记录及存储对象
	 * @param ids 文件记录主键集合
	 * @param ownerUserId 归属用户 ID，为 {@code null} 时跳过归属校验
	 */
	void purgeByIds(List<Long> ids, Long ownerUserId);

}
