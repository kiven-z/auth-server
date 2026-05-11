package com.auth.service.system.schedule.mapper;

import com.auth.service.system.schedule.model.entity.JobLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务日志 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysJobLogMapper extends BaseMapper<JobLogEntity> {

	/**
	 * 按任务取最近若干条（按时间倒序），用于熔断判断
	 * @param jobName 任务名
	 * @param jobGroup 分组
	 * @param limit 条数
	 * @return 日志列表
	 */
	List<JobLogEntity> selectRecentByJobNameAndGroup(@Param("jobName") String jobName,
			@Param("jobGroup") String jobGroup, @Param("limit") int limit);

	/**
	 * 按任务键物理删除所有日志（用于删除任务时级联清理）
	 * @param jobName 任务名
	 * @param jobGroup 分组
	 * @return 删除条数
	 */
	int deleteByJobNameAndGroup(@Param("jobName") String jobName, @Param("jobGroup") String jobGroup);

}
