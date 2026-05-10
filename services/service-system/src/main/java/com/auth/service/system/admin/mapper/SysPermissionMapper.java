package com.auth.service.system.admin.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 系统权限 Mapper（供菜单分配权限等只读场景）
 *
 * @author Bunny
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermissionEntity> {

}
