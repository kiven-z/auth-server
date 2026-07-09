package com.auth.service.system.file.service;

import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 文件回收站服务
 *
 * @author Bunny
 */
public interface FileRecycleService extends IService<FileRecordEntity> {

	/**
	 * 按主键集合恢复软删除的文件记录
	 * @param ids 文件记录主键集合
	 * @param ownerUserId 归属用户 ID，为 {@code null} 时跳过归属校验
	 * @param deleteSources 删除来源集合
	 */
	void restoreByIds(List<Long> ids, Long ownerUserId, List<String> deleteSources);

	/**
	 * 按主键集合彻底删除文件记录及存储对象
	 * @param ids 文件记录主键集合
	 * @param ownerUserId 归属用户 ID，非空时仅处理该用户创建的已删私有文件
	 * @param deleteSources 删除来源集合
	 */
	void purgeByIds(List<Long> ids, Long ownerUserId, List<String> deleteSources);

}
