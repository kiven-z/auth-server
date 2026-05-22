package com.auth.service.system.log.convert;

import com.auth.service.system.log.model.entity.JobLogEntity;
import com.auth.service.system.log.model.po.SysJobLogPageRowPO;
import com.auth.service.system.log.model.vo.LogJobDetailVO;
import com.auth.service.system.log.model.vo.LogJobPageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

/**
 * 任务日志转换器
 *
 * @author Bunny
 */
@Mapper
public interface SysJobLogConverter {

	SysJobLogConverter INSTANCE = Mappers.getMapper(SysJobLogConverter.class);

	/**
	 * 任务日志分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	@Mapping(target = "status", source = "status", qualifiedByName = "jobLogStatusToApi")
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	LogJobPageResponse toPageResponse(SysJobLogPageRowPO po);

	/**
	 * 实体 → 详情 VO
	 * @param entity 任务日志实体
	 * @return 详情 VO
	 */
	@Mapping(target = "status", source = "status", qualifiedByName = "jobLogStatusToApi")
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	LogJobDetailVO toDetailVo(JobLogEntity entity);

	/**
	 * 库表状态 → API 布尔（true=执行成功）
	 * @param dbStatus 库表布尔或 JDBC 整型投影
	 * @return API 语义
	 */
	@Named("jobLogStatusToApi")
	default Boolean jobLogStatusToApi(Object dbStatus) {
		if (dbStatus == null) {
			return null;
		}
		if (dbStatus instanceof Boolean boolStatus) {
			// 库表 false(0)=成功 → API true
			return !boolStatus;
		}
		if (dbStatus instanceof Integer intStatus) {
			// 库表 0=成功、1=失败 → API true/false
			return intStatus == 0;
		}
		throw new IllegalArgumentException("Unsupported job log status type: " + dbStatus.getClass().getName());
	}

}
