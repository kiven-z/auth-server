package com.auth.service.system.file.service;

import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.vo.FileExportTaskDownloadLinkVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 文件异步导出任务写服务
 *
 * @author Bunny
 */
public interface FileExportTaskWriteService extends IService<FileExportTaskEntity> {

	/**
	 * 取消异步导出任务
	 * @param taskId 任务ID
	 * @param createdBy 创建人
	 */
	void cancelTask(Long taskId, Long createdBy);

	/**
	 * 强制取消异步导出任务（不校验创建人）
	 * @param taskId 任务ID
	 */
	void forceCancelTask(Long taskId);

	/**
	 * 重试异步导出任务
	 * @param taskId 任务ID
	 * @param createdBy 创建人
	 */
	void retryTask(Long taskId, Long createdBy);

	/**
	 * 强制重试异步导出任务（不校验创建人；沿用原快照）
	 * @param taskId 任务ID
	 */
	void forceRetryTask(Long taskId);

	/**
	 * 获取异步导出任务下载链接
	 * @param taskId 任务ID
	 * @param createdBy 创建人
	 * @return 下载链接
	 */
	FileExportTaskDownloadLinkVO getDownloadLink(Long taskId, Long createdBy);

}
