package com.auth.module.file.api.port;

import com.auth.module.file.api.model.dto.FileExportTaskCreateResult;
import com.auth.module.file.api.model.request.FileExportTaskCreateRequest;

/**
 * 跨模块异步导出任务创建（进程内集成契约）
 *
 * @author Bunny
 */
public interface FileExportTaskOperations {

	/**
	 * 创建异步导出任务
	 * @param request 创建请求（查询条件由调用方冻结；数据权限由文件侧按当前登录画像冻结）
	 * @return 创建结果
	 */
	FileExportTaskCreateResult createTask(FileExportTaskCreateRequest request);

}
