package com.auth.service.system.admin.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundDeptPO;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundPostPO;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundUserPO;
import com.auth.service.system.admin.model.vo.role.SysRoleBoundDeptItemVO;
import com.auth.service.system.admin.model.vo.role.SysRoleBoundPostItemVO;
import com.auth.service.system.admin.model.vo.role.SysRoleBoundUserItemVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * grant_table 按角色反查授权主体 PO → 角色详情 VO 转换
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface GrantTableBoundSubjectConverter {

	GrantTableBoundSubjectConverter INSTANCE = Mappers.getMapper(GrantTableBoundSubjectConverter.class);

	/**
	 * 已授权用户 PO 列表 → 详情项 VO 列表
	 * @param poList 持久层投影列表
	 * @return 详情项 VO 列表
	 */
	List<SysRoleBoundUserItemVO> toBoundUserItemList(List<GrantTableBoundUserPO> poList);

	/**
	 * 已授权部门 PO 列表 → 详情项 VO 列表
	 * @param poList 持久层投影列表
	 * @return 详情项 VO 列表
	 */
	List<SysRoleBoundDeptItemVO> toBoundDeptItemList(List<GrantTableBoundDeptPO> poList);

	/**
	 * 已授权岗位 PO 列表 → 详情项 VO 列表
	 * @param poList 持久层投影列表
	 * @return 详情项 VO 列表
	 */
	List<SysRoleBoundPostItemVO> toBoundPostItemList(List<GrantTableBoundPostPO> poList);

}
