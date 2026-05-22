package com.auth.service.system.log.convert;

import com.auth.common.mapstruct.qualifier.MapStructStringQualifiers;
import com.auth.service.system.log.model.entity.JobLogEntity;
import com.auth.service.system.log.model.po.SysJobLogPageRowPO;
import com.auth.service.system.log.model.vo.LogJobDetailVO;
import com.auth.service.system.log.model.vo.LogJobPageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 任务日志转换器（库表/API：1=成功，0=失败，与项目 Boolean 约定一致）
 *
 * @author Bunny
 */
@Mapper(uses = MapStructStringQualifiers.class)
public interface SysJobLogConverter {

	SysJobLogConverter INSTANCE = Mappers.getMapper(SysJobLogConverter.class);

	/**
	 * 任务日志分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	LogJobPageResponse toPageResponse(SysJobLogPageRowPO po);

	/**
	 * 实体 → 详情 VO（实体 Boolean 与 API 语义一致，直接映射）
	 * @param entity 任务日志实体
	 * @return 详情 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	LogJobDetailVO toDetailVo(JobLogEntity entity);

}
