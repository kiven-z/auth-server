package com.auth.service.system.admin.convert;

import com.auth.service.system.admin.model.po.role.SysRoleAssignedPermissionPO;
import com.auth.service.system.admin.model.vo.role.SysRoleAssignedPermissionRow;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 角色权限分配行转换
 *
 * @author Bunny
 */
@Mapper
public interface SysRolePermissionConverter {

	SysRolePermissionConverter INSTANCE = Mappers.getMapper(SysRolePermissionConverter.class);

	/**
	 * 已分配权限 PO → 列表行
	 * @param po 持久层投影
	 * @return 列表行
	 */
	SysRoleAssignedPermissionRow toAssignedPermissionRow(SysRoleAssignedPermissionPO po);

	/**
	 * 已分配权限 PO 列表 → 列表行
	 * @param poList 持久层投影列表
	 * @return 列表行
	 */
	List<SysRoleAssignedPermissionRow> toAssignedPermissionRowList(List<SysRoleAssignedPermissionPO> poList);

}
