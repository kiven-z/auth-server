package com.auth.service.system.admin.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundRolePO;
import com.auth.service.system.admin.model.vo.dept.SysDeptBoundRoleItemVO;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * grant_table 主体角色授权 VO 转换
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface GrantTableSubjectRoleConverter {

	GrantTableSubjectRoleConverter INSTANCE = Mappers.getMapper(GrantTableSubjectRoleConverter.class);

	/**
	 * 已授权角色 PO 列表 → 管理端分配行 VO 列表
	 * @param poList 持久层投影列表
	 * @return 分配行 VO 列表
	 */
	List<GrantTableAssignedRoleRow> toAssignedRoleRowList(List<GrantTableBoundRolePO> poList);

	/**
	 * 已授权角色 PO 列表 → 详情内嵌项 VO 列表
	 * @param poList 持久层投影列表
	 * @return 详情项 VO 列表
	 */
	List<SysDeptBoundRoleItemVO> toBoundRoleItemList(List<GrantTableBoundRolePO> poList);

}
