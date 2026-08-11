package com.auth.service.system.file.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.file.model.entity.FileExportAttemptEntity;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.po.FileExportTaskAdminPageRowPO;
import com.auth.service.system.file.model.vo.FileExportAttemptVO;
import com.auth.service.system.file.model.vo.FileExportTaskAdminDetailVO;
import com.auth.service.system.file.model.vo.FileExportTaskAdminPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 文件异步导出任务管理端查询对象映射
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface FileExportTaskAdminQueryConverter {

	FileExportTaskAdminQueryConverter INSTANCE = Mappers.getMapper(FileExportTaskAdminQueryConverter.class);

	/**
	 * 管理端分页行 PO 映射为分页 VO
	 * @param po 持久层投影
	 * @return 分页行
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	FileExportTaskAdminPageVO toPageVo(FileExportTaskAdminPageRowPO po);

	/**
	 * 任务实体映射为管理端详情 VO
	 * @param entity 任务实体
	 * @return 详情
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	FileExportTaskAdminDetailVO toDetailVo(FileExportTaskEntity entity);

	/**
	 * 执行尝试实体映射为 VO
	 * @param entity 执行尝试
	 * @return 执行尝试行
	 */
	FileExportAttemptVO toAttemptVo(FileExportAttemptEntity entity);

}
