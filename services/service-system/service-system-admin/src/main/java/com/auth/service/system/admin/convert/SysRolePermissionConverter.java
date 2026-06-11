package com.auth.service.system.admin.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.po.role.SysRoleAssignedPermissionPO;
import com.auth.service.system.admin.model.vo.role.SysRoleAssignedPermissionRow;
import com.auth.service.system.admin.model.vo.role.SysRoleBoundPermissionItemVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 角色权限分配行转换
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface SysRolePermissionConverter {

	SysRolePermissionConverter INSTANCE = Mappers.getMapper(SysRolePermissionConverter.class);

	/**
	 * 已分配权限 PO 列表 → 列表行
	 * @param poList 持久层投影列表
	 * @return 列表行
	 */
	List<SysRoleAssignedPermissionRow> toAssignedPermissionRowList(List<SysRoleAssignedPermissionPO> poList);

	/**
	 * 已分配权限 PO 列表 → 角色详情项 VO 列表
	 * @param poList 持久层投影列表
	 * @return 详情项 VO 列表
	 */
	List<SysRoleBoundPermissionItemVO> toBoundPermissionItemList(List<SysRoleAssignedPermissionPO> poList);

}
