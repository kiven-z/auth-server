package com.auth.service.system.schedule.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.auth.service.system.schedule.model.entity.SysJobGroupEntity;
import com.auth.service.system.schedule.model.form.SysJobGroupForm;
import com.auth.service.system.schedule.model.form.SysJobGroupUpdateForm;

/**
 * 任务分组转换器
 *
 * @author Bunny
 */
@Mapper
public interface SysJobGroupConverter {

	SysJobGroupConverter INSTANCE = Mappers.getMapper(SysJobGroupConverter.class);

	/**
	 * 新增表单 → 实体
	 * @param form 新增表单
	 * @return 任务分组实体
	 */
	SysJobGroupEntity toEntity(SysJobGroupForm form);

	/**
	 * 更新表单 → 覆盖已有实体（仅覆盖允许更新的字段）
	 * @param form 更新表单
	 * @param entity 数据库已有实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "groupCode", ignore = true)
	@Mapping(target = "isSystem", ignore = true)
	void updateEntity(SysJobGroupUpdateForm form, @MappingTarget SysJobGroupEntity entity);

}
