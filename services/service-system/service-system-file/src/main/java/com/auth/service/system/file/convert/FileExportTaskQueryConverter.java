package com.auth.service.system.file.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.po.FileExportTaskPageRowPO;
import com.auth.service.system.file.model.vo.FileExportTaskDetailVO;
import com.auth.service.system.file.model.vo.FileExportTaskPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 文件异步导出任务查询对象映射
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface FileExportTaskQueryConverter {

	FileExportTaskQueryConverter INSTANCE = Mappers.getMapper(FileExportTaskQueryConverter.class);

	/**
	 * 分页行 PO 映射为分页 VO
	 * @param po 持久层投影
	 * @return 分页行
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	FileExportTaskPageVO toPageVo(FileExportTaskPageRowPO po);

	/**
	 * 任务实体映射为详情 VO
	 * @param entity 任务实体
	 * @return 详情
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	FileExportTaskDetailVO toDetailVo(FileExportTaskEntity entity);

}
