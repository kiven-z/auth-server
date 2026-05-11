package com.auth.service.system.schedule.convert;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.auth.service.system.schedule.model.po.SysJobLogPageRowPO;
import com.auth.service.system.schedule.model.vo.SysJobLogPageResponse;

/**
 * 任务日志转换器
 *
 * @author Bunny
 */
@Mapper
public interface SysJobLogConvert {

	SysJobLogConvert INSTANCE = Mappers.getMapper(SysJobLogConvert.class);

	/**
	 * 任务日志分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	SysJobLogPageResponse toPageResponse(SysJobLogPageRowPO po);

}
