package com.auth.service.system.schedule.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.common.mapstruct.qualifier.MapStructStringQualifiers;
import com.auth.service.system.schedule.model.entity.JobLogEntity;
import com.auth.service.system.schedule.model.po.SysJobLogPageRowPO;
import com.auth.service.system.schedule.model.vo.SysJobLogDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobLogPageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 任务日志转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class, uses = MapStructStringQualifiers.class)
public interface SysJobLogConverter {

	SysJobLogConverter INSTANCE = Mappers.getMapper(SysJobLogConverter.class);

	/**
	 * 任务日志分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	SysJobLogPageResponse toPageResponse(SysJobLogPageRowPO po);

	/**
	 * 实体 → 详情 VO（实体 Boolean 与 API 语义一致，直接映射）
	 * @param entity 任务日志实体
	 * @return 详情 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	SysJobLogDetailVO toDetailVo(JobLogEntity entity);

}
