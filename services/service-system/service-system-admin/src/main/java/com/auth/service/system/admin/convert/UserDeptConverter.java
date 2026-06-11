package com.auth.service.system.admin.convert;

import com.auth.service.system.admin.model.entity.UserDeptEntity;
import com.auth.service.system.admin.model.form.userdept.UserDeptAssignForm;
import com.auth.service.system.admin.model.po.userdept.UserDeptPageRowPO;
import com.auth.service.system.admin.model.vo.userdept.UserDeptPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

	/**
	 * 表单转实体（userId 由服务层填充）
	 * @param form 关联表单
	 * @return 待持久化实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "userId", ignore = true)
	@Mapping(target = "createTime", ignore = true)
	@Mapping(target = "updateTime", ignore = true)
	@Mapping(target = "createUser", ignore = true)
	@Mapping(target = "updateUser", ignore = true)
	@Mapping(target = "version", ignore = true)
	UserDeptEntity toEntity(UserDeptAssignForm form);

}
