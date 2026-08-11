package com.auth.service.system.file.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.file.model.query.FileExportTaskAdminPageQuery;
import com.auth.service.system.file.model.vo.FileExportAttemptVO;
import com.auth.service.system.file.model.vo.FileExportTaskAdminDetailVO;
import com.auth.service.system.file.model.vo.FileExportTaskAdminPageVO;

import java.util.List;

/**
 * 文件异步导出任务管理端查询服务
 *
 * @author Bunny
 */
public interface FileExportTaskAdminQueryService {

	/**
	 * 管理端分页查询导出任务
	 * @param query 查询条件
	 * @return 分页结果
	 */
	PageResponse<FileExportTaskAdminPageVO> getPage(FileExportTaskAdminPageQuery query);

	/**
	 * 管理端查询导出任务详情
	 * @param id 任务ID
	 * @return 详情
	 */
	FileExportTaskAdminDetailVO getDetail(Long id);

	/**
	 * 查询导出任务执行尝试列表
	 * @param taskId 任务ID
	 * @return 执行尝试列表
	 */
	List<FileExportAttemptVO> listAttempts(Long taskId);

}
