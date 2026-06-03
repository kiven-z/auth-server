package com.auth.service.system.admin.convert;

import com.auth.service.system.admin.model.po.dept.SysDeptBoundRolePO;
import com.auth.service.system.admin.model.vo.dept.SysDeptAssignedRoleRow;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 部门扩展管理 VO 转换
 *
 * @author bunny
 */
@Mapper
public interface SysDeptManageConverter {

	SysDeptManageConverter INSTANCE = Mappers.getMapper(SysDeptManageConverter.class);

	/**
	 * 已授权角色 PO → 分配行 VO
	 * @param po 持久层投影
	 * @return 分配行 VO
	 */
	SysDeptAssignedRoleRow toAssignedRoleRow(SysDeptBoundRolePO po);

	/**
	 * 已授权角色 PO 列表 → 分配行 VO 列表
	 * @param poList 持久层投影列表
	 * @return 分配行 VO 列表
	 */
	List<SysDeptAssignedRoleRow> toAssignedRoleRowList(List<SysDeptBoundRolePO> poList);

}
