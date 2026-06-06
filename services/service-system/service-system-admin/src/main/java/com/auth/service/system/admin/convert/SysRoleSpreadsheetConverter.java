package com.auth.service.system.admin.convert;

import com.auth.common.mapstruct.qualifier.MapStructExportQualifiers;
import com.auth.service.system.admin.excel.role.SysRoleSpreadsheetRow;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.enums.RoleTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 角色表格行转换器
 *
 * @author Bunny
 */
@Mapper(uses = MapStructExportQualifiers.class)
public interface SysRoleSpreadsheetConverter {

	SysRoleSpreadsheetConverter INSTANCE = Mappers.getMapper(SysRoleSpreadsheetConverter.class);

	/**
	 * 角色类型编码 → 导出文案
	 * @param roleType 类型编码
	 * @return 中文标签
	 */
	@Named("roleTypeLabel")
	default String roleTypeLabel(Integer roleType) {
		String desc = RoleTypeEnum.getDesc(roleType);
		return desc != null ? desc : "";
	}

	/**
	 * 实体列表 → Excel 行
	 * @param entities 角色实体
	 * @return 表格行
	 */
	List<SysRoleSpreadsheetRow> toSpreadsheetRows(List<SysRoleEntity> entities);

	/**
	 * 实体 → Excel 行
	 * @param entity 角色实体
	 * @return 表格行
	 */
	@Mapping(target = "roleTypeLabel", source = "roleType", qualifiedByName = "roleTypeLabel")
	@Mapping(target = "statusLabel", source = "status", qualifiedByName = "statusLabel")
	@Mapping(target = "createTimeText", source = "createTime", qualifiedByName = "dateTimeText")
	@Mapping(target = "updateTimeText", source = "updateTime", qualifiedByName = "dateTimeText")
	SysRoleSpreadsheetRow toSpreadsheetRow(SysRoleEntity entity);

}
