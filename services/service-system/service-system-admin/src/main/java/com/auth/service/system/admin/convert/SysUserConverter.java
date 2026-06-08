package com.auth.service.system.admin.convert;

import com.auth.common.data.model.enums.GenderStatus;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.auth.service.system.admin.model.po.user.SysUserPageRowPO;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.Period;

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

	/**
	 * 新增表单 → 实体（密码、头像、权限版本等由 Service 填充）
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
	@Mapping(target = "isDeleted", ignore = true)
	UserEntity toEntity(SysUserForm form);

	/**
	 * 未传性别时默认为未知；有出生日期时计算年龄
	 */
	@AfterMapping
	default void fillCreateDerivedFields(@MappingTarget UserEntity entity, SysUserForm form) {
		if (entity.getGender() == null) {
			entity.setGender(GenderStatus.UNKNOWN.getCode());
		}
		if (form.getBirthday() != null) {
			entity.setAge(Period.between(form.getBirthday(), LocalDate.now()).getYears());
		}
	}

}
