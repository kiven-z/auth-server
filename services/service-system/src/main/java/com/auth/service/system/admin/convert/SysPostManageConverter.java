package com.auth.service.system.admin.convert;

import com.auth.service.system.admin.model.po.dept.SysDeptBoundRolePO;
import com.auth.service.system.admin.model.vo.post.SysPostAssignedRoleRow;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 岗位扩展管理 VO 转换
 *
 * @author Bunny
 */
@Mapper
public interface SysPostManageConverter {

	SysPostManageConverter INSTANCE = Mappers.getMapper(SysPostManageConverter.class);

	/**
	 * 已授权角色 PO 列表 → 分配行 VO 列表 FIXME SysDeptBoundRolePO
	 * 是否可以合并下，下面的toAssignedRoleRowList方法和grant_table很多都是一样的
	 * @param poList 持久层投影列表
	 * @return 分配行 VO 列表
	 */
	List<SysPostAssignedRoleRow> toAssignedRoleRowList(List<SysDeptBoundRolePO> poList);

}
