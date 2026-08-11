package com.auth.service.system.file.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.query.FileExportTaskPageQuery;
import com.auth.service.system.file.model.vo.FileExportTaskDetailVO;
import com.auth.service.system.file.model.vo.FileExportTaskPageVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 文件异步导出任务查询服务
 *
 * @author Bunny
 */
public interface FileExportTaskQueryService extends IService<FileExportTaskEntity> {

	/**
	 * 按创建人分页查询导出任务
	 * @param query 查询条件
	 * @param createdBy 创建人ID
	 * @return 分页结果
	 */
	PageResponse<FileExportTaskPageVO> getPage(FileExportTaskPageQuery query, Long createdBy);

	/**
	 * 按创建人查询导出任务详情
	 * @param id 任务ID
	 * @param createdBy 创建人ID
	 * @return 详情
	 */
	FileExportTaskDetailVO getDetail(Long id, Long createdBy);

}
