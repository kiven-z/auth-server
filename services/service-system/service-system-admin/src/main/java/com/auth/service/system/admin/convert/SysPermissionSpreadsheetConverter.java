package com.auth.service.system.admin.convert;

import com.auth.common.mapstruct.qualifier.MapStructExportQualifiers;
import com.auth.service.system.admin.excel.permission.SysPermissionSpreadsheetRow;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.enums.PermissionTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 权限表格行转换器
 *
 * @author Bunny
 */
@Mapper(uses = MapStructExportQualifiers.class)
public interface SysPermissionSpreadsheetConverter {

	SysPermissionSpreadsheetConverter INSTANCE = Mappers.getMapper(SysPermissionSpreadsheetConverter.class);

	/**
	 * 权限类型编码 → 导出文案
	 * @param permissionType 类型编码
	 * @return 中文标签
	 */
	@Named("permissionTypeLabel")
	default String permissionTypeLabel(Integer permissionType) {
		String desc = PermissionTypeEnum.getDesc(permissionType);
		return desc != null ? desc : "";
	}

	/**
	 * 实体列表 → Excel 行
	 * @param entities 权限实体
	 * @return 表格行
	 */
	List<SysPermissionSpreadsheetRow> toSpreadsheetRows(List<SysPermissionEntity> entities);

	/**
	 * 实体 → Excel 行
	 * @param entity 权限实体
	 * @return 表格行
	 */
	@Mapping(target = "permissionTypeLabel", source = "permissionType", qualifiedByName = "permissionTypeLabel")
	@Mapping(target = "statusLabel", source = "status", qualifiedByName = "statusLabel")
	@Mapping(target = "createTimeText", source = "createTime", qualifiedByName = "dateTimeText")
	@Mapping(target = "updateTimeText", source = "updateTime", qualifiedByName = "dateTimeText")
	SysPermissionSpreadsheetRow toSpreadsheetRow(SysPermissionEntity entity);

}
