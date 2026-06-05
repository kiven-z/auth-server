package com.auth.service.system.admin.convert;

import com.auth.common.mapstruct.qualifier.MapStructExportQualifiers;
import com.auth.service.system.admin.excel.dept.SysDeptSpreadsheetRow;
import com.auth.service.system.admin.model.po.dept.SysDeptPageRowPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 部门表格行转换器
 *
 * @author Bunny
 */
@Mapper(uses = MapStructExportQualifiers.class)
public interface SysDeptSpreadsheetConverter {

	SysDeptSpreadsheetConverter INSTANCE = Mappers.getMapper(SysDeptSpreadsheetConverter.class);

	/**
	 * 分页 PO 列表 → Excel 行
	 * @param rows 持久层投影
	 * @return 表格行
	 */
	List<SysDeptSpreadsheetRow> toSpreadsheetRows(List<SysDeptPageRowPO> rows);

	/**
	 * 分页 PO → Excel 行
	 * @param po 持久层投影
	 * @return 表格行
	 */
	@Mapping(target = "deptPath", ignore = true)
	@Mapping(target = "statusLabel", source = "status", qualifiedByName = "statusLabel")
	@Mapping(target = "createTimeText", source = "createTime", qualifiedByName = "dateTimeText")
	@Mapping(target = "updateTimeText", source = "updateTime", qualifiedByName = "dateTimeText")
	SysDeptSpreadsheetRow toSpreadsheetRow(SysDeptPageRowPO po);

}
