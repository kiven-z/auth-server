package com.auth.service.system.admin.convert.sys;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.model.po.user.UserSearchItemPO;
import com.auth.service.system.admin.model.vo.user.SysUserProfileVO;
import com.auth.service.system.admin.model.vo.user.SysUserSearchItemVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用户实体与档案 VO 转换
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface SysUserProfileConverter {

	SysUserProfileConverter INSTANCE = Mappers.getMapper(SysUserProfileConverter.class);

	/**
	 * 将用户实体转换为档案基本信息 VO
	 * @param user 用户实体
	 * @return 档案 VO
	 */
	@Mapping(target = "deptList", ignore = true)
	@Mapping(target = "postList", ignore = true)
	SysUserProfileVO toProfileVo(UserEntity user);

	/**
	 * 将用户搜索档案列表转换为 VO 列表
	 * @param list 用户搜索档案列表
	 * @return 用户搜索档案 VO 列表
	 */
	List<SysUserSearchItemVO> toSearchItemVoList(List<UserSearchItemPO> list);

}
