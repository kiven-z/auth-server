package com.auth.service.system.admin.convert;

import com.auth.service.system.admin.model.po.user.SysUserPageRowPO;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 用户转换器
 *
 * @author Bunny
 */
@Mapper
public interface SysUserConverter {

	SysUserConverter INSTANCE = Mappers.getMapper(SysUserConverter.class);

	/**
	 * 分页行 PO → 分页 VO
	 * @param po 持久层投影
	 * @return 分页行
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	SysUserPageVO toPageVo(SysUserPageRowPO po);

}
