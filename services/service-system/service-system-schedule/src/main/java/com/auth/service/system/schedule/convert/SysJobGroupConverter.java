package com.auth.service.system.schedule.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.schedule.model.entity.SysJobGroupEntity;
import com.auth.service.system.schedule.model.form.SysJobGroupForm;
import com.auth.service.system.schedule.model.form.SysJobGroupUpdateForm;
import com.auth.service.system.schedule.model.po.SysJobGroupPageRowPO;
import com.auth.service.system.schedule.model.vo.SysJobGroupDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobGroupPageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * 任务分组转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface SysJobGroupConverter {

	SysJobGroupConverter INSTANCE = Mappers.getMapper(SysJobGroupConverter.class);

	/**
	 * 新增表单 → 实体
	 * @param form 新增表单
	 * @return 任务分组实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createTime", ignore = true)
	@Mapping(target = "updateTime", ignore = true)
	@Mapping(target = "createUser", ignore = true)
	@Mapping(target = "updateUser", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "remark", ignore = true)
	@Mapping(target = "isSystem", ignore = true)
	SysJobGroupEntity toEntity(SysJobGroupForm form);

	/**
	 * 更新表单 → 覆盖已有实体（仅覆盖允许更新的字段）
	 * @param form 更新表单
	 * @param entity 数据库已有实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "groupCode", ignore = true)
	@Mapping(target = "isSystem", ignore = true)
	@Mapping(target = "createTime", ignore = true)
	@Mapping(target = "updateTime", ignore = true)
	@Mapping(target = "createUser", ignore = true)
	@Mapping(target = "updateUser", ignore = true)
	@Mapping(target = "remark", ignore = true)
	@Mapping(target = "version", ignore = true)
	void updateEntity(SysJobGroupUpdateForm form, @MappingTarget SysJobGroupEntity entity);

	/**
	 * 任务分组分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	SysJobGroupPageResponse toPageResponse(SysJobGroupPageRowPO po);

	/**
	 * 任务分组实体 → 详情 VO
	 * @param entity 任务分组实体
	 * @return 详情返回对象
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	SysJobGroupDetailVO toDetailVo(SysJobGroupEntity entity);

}
