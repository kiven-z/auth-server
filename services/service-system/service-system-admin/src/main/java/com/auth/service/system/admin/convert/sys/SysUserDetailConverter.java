package com.auth.service.system.admin.convert.sys;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.vo.user.SysUserDetailVO;
import com.auth.service.system.admin.model.vo.user.SysUserProfileVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 用户档案与详情 VO 转换
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface SysUserDetailConverter {

	SysUserDetailConverter INSTANCE = Mappers.getMapper(SysUserDetailConverter.class);

	/**
	 * 档案 VO → 详情 VO（生效授权由 Service 层补全）
	 * @param profile 用户档案
	 * @return 详情 VO
	 */
	@Mapping(target = "effectiveRoles", ignore = true)
	@Mapping(target = "effectivePermissions", ignore = true)
	SysUserDetailVO fromProfile(SysUserProfileVO profile);

}
