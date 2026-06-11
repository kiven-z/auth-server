package com.auth.service.system.admin.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.entity.UserPostEntity;
import com.auth.service.system.admin.model.form.userpost.UserPostAssignForm;
import com.auth.service.system.admin.model.form.userpost.UserPostRelationUpdateForm;
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

	/**
	 * 新增表单转实体（userId 由服务层填充）
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
	UserPostEntity toEntity(UserPostAssignForm form);

	/**
	 * 更新表单字段合并到已有实体（不覆盖主键、岗位 ID 与审计字段）
	 * @param form 更新表单
	 * @param entity 已有实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "userId", ignore = true)
	@Mapping(target = "postId", ignore = true)
	@Mapping(target = "createTime", ignore = true)
	@Mapping(target = "updateTime", ignore = true)
	@Mapping(target = "createUser", ignore = true)
	@Mapping(target = "updateUser", ignore = true)
	@Mapping(target = "version", ignore = true)
	void applyUpdateForm(UserPostRelationUpdateForm form, @org.mapstruct.MappingTarget UserPostEntity entity);

}
