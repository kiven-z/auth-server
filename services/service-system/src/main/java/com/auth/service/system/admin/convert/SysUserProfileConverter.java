package com.auth.service.system.admin.convert;

import com.auth.module.security.starter.model.UserEntity;
import com.auth.service.system.common.model.vo.SysUserProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 用户实体与档案 VO 转换
 *
 * @author Bunny
 */
@Mapper()
public interface SysUserProfileConverter {

	SysUserProfileConverter INSTANCE = Mappers.getMapper(SysUserProfileConverter.class);

	/**
	 * 将用户实体转换为档案基本信息 VO
	 * @param user 用户实体
	 * @return 档案 VO
	 */
	SysUserProfileResponse toProfileResponse(UserEntity user);

}
