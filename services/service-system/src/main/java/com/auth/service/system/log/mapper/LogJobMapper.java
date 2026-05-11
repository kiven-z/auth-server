package com.auth.service.system.log.mapper;

import com.auth.service.system.schedule.model.entity.JobLogEntity;
import com.auth.service.system.schedule.model.po.SysJobLogPageRowPO;
import com.auth.service.system.schedule.model.query.SysJobLogQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 定时任务日志Mapper
 *
 * @author Bunny
 */
@Mapper
public interface LogJobMapper extends BaseMapper<JobLogEntity> {

	/**
	 * 分页查询日志
	 * @param page 分页
	 * @param query 条件
	 * @return 分页结果
	 */
	IPage<SysJobLogPageRowPO> selectListByPage(@Param("page") Page<JobLogEntity> page,
			@Param("query") SysJobLogQuery query);

	/**
	 * 物理清空日志表（一键清空）
	 */
	void deleteAll();

}
