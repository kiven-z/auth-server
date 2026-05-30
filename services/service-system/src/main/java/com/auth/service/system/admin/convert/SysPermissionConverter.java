package com.auth.service.system.admin.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.form.permission.SysPermissionCreateForm;
import com.auth.service.system.admin.model.vo.permission.SysPermissionPageResponse;

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
	SysPermissionEntity toEntity(SysPermissionCreateForm form);

	/**
	 * Entity → 分页行 VO
	 * @param entity 权限实体
	 * @return 分页行 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	SysPermissionPageResponse toPageResponse(SysPermissionEntity entity);

}
