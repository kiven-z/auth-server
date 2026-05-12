package com.auth.service.system.admin.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.auth.module.security.starter.model.UserEntity;
import com.auth.service.system.common.model.SysUserSearchItemResponse;
import com.auth.service.system.common.model.po.UserDeptProfilePO;
import com.auth.service.system.common.model.po.UserPostProfilePO;
import com.auth.service.system.common.model.po.UserSearchItemPO;
import com.auth.service.system.common.model.vo.SysUserDeptProfileItemResponse;
import com.auth.service.system.common.model.vo.SysUserPostProfileItemResponse;
import com.auth.service.system.common.model.vo.SysUserProfileResponse;

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
	@Mapping(target = "deptList", ignore = true)
	@Mapping(target = "postList", ignore = true)
	SysUserProfileResponse toProfileResponse(UserEntity user);

	/**
	 * 将用户部门档案列表转换为 VO 列表
	 * @param list 用户部门档案列表
	 * @return 用户部门档案 VO 列表
	 */
	List<SysUserDeptProfileItemResponse> toDeptItemResponseList(List<UserDeptProfilePO> list);

	/**
	 * 将用户岗位档案列表转换为 VO 列表
	 * @param list 用户岗位档案列表
	 * @return 用户岗位档案 VO 列表
	 */
	List<SysUserPostProfileItemResponse> toPostItemResponseList(List<UserPostProfilePO> list);

	/**
	 * 将用户搜索档案列表转换为 VO 列表
	 * @param list 用户搜索档案列表
	 * @return 用户搜索档案 VO 列表
	 */
	List<SysUserSearchItemResponse> toSearchItemResponseList(List<UserSearchItemPO> list);

}
