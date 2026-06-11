package com.auth.service.system.admin.convert;

import com.auth.service.system.admin.model.po.userdept.UserDeptPageRowPO;
import com.auth.service.system.admin.model.vo.userdept.UserDeptPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 用户部门关联转换
 *
 * @author Bunny
 */
@Mapper
public interface UserDeptConverter {

	/**
	 * 单例
	 */
	UserDeptConverter INSTANCE = Mappers.getMapper(UserDeptConverter.class);

	/**
	 * 分页行 PO 转 VO
	 * @param row 分页行
	 * @return VO
	 */
	UserDeptPageVO toPageVo(UserDeptPageRowPO row);

}
