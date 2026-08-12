package com.auth.module.file.api.port;

import com.auth.module.file.api.model.dto.ExportPreCheckResult;
import com.auth.module.file.api.model.dto.FileExportTaskCreateResult;
import com.auth.module.file.api.model.request.ExportPreCheckRequest;
import com.auth.module.file.api.model.request.FileExportTaskCreateRequest;

/**
 * 跨模块异步导出任务操作契约
 *
 * @author Bunny
 */
public interface FileExportTaskOperations {

	/**
	 * 导出前置检查（估算行数并判断产物格式）
	 * @param request 检查请求（路由参数与查询快照；数据权限由文件侧按当前登录画像获取）
	 * @return 检查结果（状态、预估行数、分片数、产物格式）
	 */
	ExportPreCheckResult preCheck(ExportPreCheckRequest request);

	/**
	 * 创建异步导出任务
	 * @param request 创建请求（查询条件由调用方冻结；数据权限由文件侧按当前登录画像冻结）
	 * @return 创建结果
	 */
	FileExportTaskCreateResult createTask(FileExportTaskCreateRequest request);

}
