package com.auth.service.system.admin.convert.sys;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.auth.service.system.admin.model.po.user.SysUserPageRowPO;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * 用户转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
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

	/**
	 * 新增表单 → 实体
	 * @param form 表单
	 * @return 实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "avatar", ignore = true)
	@Mapping(target = "age", ignore = true)
	@Mapping(target = "permVersion", ignore = true)
	@Mapping(target = "createTime", ignore = true)
	@Mapping(target = "updateTime", ignore = true)
	@Mapping(target = "createUser", ignore = true)
	@Mapping(target = "updateUser", ignore = true)
	@Mapping(target = "version", ignore = true)
	UserEntity toEntity(SysUserForm form);

	/**
	 * 更新表单字段合并到已有实体
	 * @param form 更新表单
	 * @param entity 已有实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "avatar", ignore = true)
	@Mapping(target = "age", ignore = true)
	@Mapping(target = "permVersion", ignore = true)
	@Mapping(target = "createTime", ignore = true)
	@Mapping(target = "updateTime", ignore = true)
	@Mapping(target = "createUser", ignore = true)
	@Mapping(target = "updateUser", ignore = true)
	@Mapping(target = "version", ignore = true)
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void applyUpdateForm(SysUserForm form, @org.mapstruct.MappingTarget UserEntity entity);

}
