package com.auth.service.system.admin.convert.menu;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.po.menu.SysMenuAssignedPermissionPO;
import com.auth.service.system.admin.model.po.menu.SysMenuAssignedRolePO;
import com.auth.service.system.admin.model.vo.menu.SysMenuAssignedPermissionRow;
import com.auth.service.system.admin.model.vo.menu.SysMenuAssignedRoleRow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 菜单角色/权限分配行转换
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface SysMenuManageConverter {

	SysMenuManageConverter INSTANCE = Mappers.getMapper(SysMenuManageConverter.class);

	/**
	 * 已分配角色 PO → 列表行（assigned 恒为 true）
	 * @param po 持久层投影
	 * @return 列表行
	 */
	@Mapping(target = "assigned", constant = "true")
	SysMenuAssignedRoleRow toAssignedRoleRow(SysMenuAssignedRolePO po);

	/**
	 * 已关联权限 PO → 列表行（assigned 恒为 true）
	 * @param po 持久层投影
	 * @return 列表行
	 */
	@Mapping(target = "assigned", constant = "true")
	SysMenuAssignedPermissionRow toAssignedPermissionRow(SysMenuAssignedPermissionPO po);

}
