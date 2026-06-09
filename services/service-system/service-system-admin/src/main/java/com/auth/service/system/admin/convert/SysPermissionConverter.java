package com.auth.service.system.admin.convert;

import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.form.permission.SysPermissionForm;
import com.auth.service.system.admin.model.po.permission.SysPermissionBoundMenuPO;
import com.auth.service.system.admin.model.po.permission.SysPermissionBoundRolePO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionBoundMenuItemVO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionBoundRoleItemVO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionDetailVO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionPageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 权限转换器
 *
 * @author Bunny
 */
@Mapper
public interface SysPermissionConverter {

	SysPermissionConverter INSTANCE = Mappers.getMapper(SysPermissionConverter.class);

	/**
	 * 新增表单 → 实体
	 * @param form 表单
	 * @return 实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createTime", ignore = true)
	@Mapping(target = "updateTime", ignore = true)
	@Mapping(target = "createUser", ignore = true)
	@Mapping(target = "updateUser", ignore = true)
	@Mapping(target = "version", ignore = true)
	SysPermissionEntity toEntity(SysPermissionForm form);

	/**
	 * 更新表单字段合并到已有实体
	 * @param form 更新表单
	 * @param entity 已有实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "permissionType", ignore = true)
	@Mapping(target = "createTime", ignore = true)
	@Mapping(target = "updateTime", ignore = true)
	@Mapping(target = "createUser", ignore = true)
	@Mapping(target = "updateUser", ignore = true)
	@Mapping(target = "version", ignore = true)
	void applyUpdateForm(SysPermissionForm form, @MappingTarget SysPermissionEntity entity);

	/**
	 * 实体 → 详情 VO
	 * @param entity 权限实体
	 * @return 详情 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	@Mapping(target = "boundRoles", ignore = true)
	@Mapping(target = "boundMenus", ignore = true)
	SysPermissionDetailVO toDetailVo(SysPermissionEntity entity);

	/**
	 * 已绑定角色 PO 列表 → 详情项 VO 列表
	 * @param poList 持久层投影列表
	 * @return 详情项 VO 列表
	 */
	List<SysPermissionBoundRoleItemVO> toBoundRoleItemList(List<SysPermissionBoundRolePO> poList);

	/**
	 * 已绑定菜单 PO 列表 → 详情项 VO 列表
	 * @param poList 持久层投影列表
	 * @return 详情项 VO 列表
	 */
	List<SysPermissionBoundMenuItemVO> toBoundMenuItemList(List<SysPermissionBoundMenuPO> poList);

	/**
	 * Entity → 分页行 VO
	 * @param entity 权限实体
	 * @return 分页行 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	SysPermissionPageResponse toPageResponse(SysPermissionEntity entity);

}
