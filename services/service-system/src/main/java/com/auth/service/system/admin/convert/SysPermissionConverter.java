package com.auth.service.system.admin.convert;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.auth.service.system.admin.model.entity.SysPermissionEntity;
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
	 * Entity → 分页行 VO
	 * @param entity 权限实体
	 * @return 分页行 VO
	 */
	SysPermissionPageResponse toPageResponse(SysPermissionEntity entity);

}
