package com.auth.service.system.admin.convert;

import com.auth.common.mapstruct.qualifier.MapStructExportQualifiers;
import com.auth.service.system.admin.excel.post.SysPostExportRow;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 岗位导出行转换器
 *
 * @author Bunny
 */
@Mapper(uses = MapStructExportQualifiers.class)
public interface SysPostExportConverter {

	SysPostExportConverter INSTANCE = Mappers.getMapper(SysPostExportConverter.class);

	/**
	 * 分页 PO 列表 → Excel 行
	 * @param rows 持久层投影
	 * @return 导出行
	 */
	List<SysPostExportRow> toExportRows(List<SysPostPageRowPO> rows);

	/**
	 * 分页 PO → Excel 行
	 * @param po 持久层投影
	 * @return 导出行
	 */
	@Mapping(target = "statusLabel", source = "status", qualifiedByName = "statusLabel")
	@Mapping(target = "createTimeText", source = "createTime", qualifiedByName = "dateTimeText")
	@Mapping(target = "updateTimeText", source = "updateTime", qualifiedByName = "dateTimeText")
	SysPostExportRow toExportRow(SysPostPageRowPO po);

}
