package com.auth.service.system.admin.convert.menu;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuSaveForm;
import com.auth.service.system.admin.model.po.permission.SysPermissionBoundRolePO;
import com.auth.service.system.admin.model.po.role.SysRoleAssignedPermissionPO;
import com.auth.service.system.admin.model.vo.menu.SysMenuDetailVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuListVO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionBoundRoleItemVO;
import com.auth.service.system.admin.model.vo.role.SysRoleBoundPermissionItemVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 菜单转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface SysMenuConverter {

	SysMenuConverter INSTANCE = Mappers.getMapper(SysMenuConverter.class);

	/**
	 * 详情 VO：继承列表 VO 的映射 + 额外字段 parentId、status 等继承自 toListVo 的映射规则
	 * 注意：如果字段名相同且无特殊转换，MapStruct 会自动映射
	 * @param entity 菜单实体
	 * @return 详情 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	@Mapping(target = "boundRoles", ignore = true)
	@Mapping(target = "boundPermissions", ignore = true)
	SysMenuDetailVO toDetailVo(SysMenuEntity entity);

	/**
	 * 已绑定角色 PO 列表 → 详情项 VO 列表
	 * @param poList 持久层投影列表
	 * @return 详情项 VO 列表
	 */
	List<SysPermissionBoundRoleItemVO> toBoundRoleItemList(List<SysPermissionBoundRolePO> poList);

	/**
	 * 已绑定权限 PO 列表 → 详情项 VO 列表
	 * @param poList 持久层投影列表
	 * @return 详情项 VO 列表
	 */
	List<SysRoleBoundPermissionItemVO> toBoundPermissionItemList(List<SysRoleAssignedPermissionPO> poList);

	/**
	 * 列表行 VO
	 * @param entity 菜单实体
	 * @return 列表 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	SysMenuListVO toListVo(SysMenuEntity entity);

	/**
	 * 列表行 VO 批量
	 * @param entities 菜单实体列表
	 * @return 列表 VO
	 */
	List<SysMenuListVO> toListVoList(List<SysMenuEntity> entities);

	/**
	 * 映射基本字段
	 * @param form 菜单保存表单
	 * @return 菜单实体
	 */
	@Mapping(target = "createTime", ignore = true)
	@Mapping(target = "updateTime", ignore = true)
	@Mapping(target = "createUser", ignore = true)
	@Mapping(target = "updateUser", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "extraMeta", ignore = true)
	SysMenuEntity toMenuEntity(SysMenuSaveForm form);

}