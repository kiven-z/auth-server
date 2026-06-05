package com.auth.service.system.admin.convert;

import com.auth.service.system.admin.excel.post.SysPostExportRow;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 岗位导出行转换器
 *
 * @author Bunny
 */
@Mapper
public interface SysPostExportConverter {

	DateTimeFormatter EXPORT_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

	/**
	 * 启用状态 → 导出文案
	 * @param status 布尔状态
	 * @return 中文标签
	 */
	@Named("statusLabel")
	default String statusLabel(Boolean status) {
		// FIXME 将这个抽取到公共的 common-mapstruct
		return Boolean.TRUE.equals(status) ? "启用" : "停用";
	}

	/**
	 * 时间 → 导出文本
	 * @param dateTime 时间
	 * @return 格式化文本，空则返回空串
	 */
	@Named("dateTimeText")
	default String dateTimeText(LocalDateTime dateTime) {
		// FIXME 将这个抽取到公共的 common-mapstruct
		return dateTime == null ? "" : dateTime.format(EXPORT_DATE_TIME);
	}

}
