package com.auth.service.system.admin.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.po.userpost.UserPostPageRowPO;
import com.auth.service.system.admin.model.vo.userpost.UserPostPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 用户岗位关联转换
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface UserPostConverter {

	/**
	 * 单例
	 */
	UserPostConverter INSTANCE = Mappers.getMapper(UserPostConverter.class);

	/**
	 * 分页行 PO 转 VO
	 * @param row 分页行
	 * @return VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	UserPostPageVO toPageVo(UserPostPageRowPO row);

}
