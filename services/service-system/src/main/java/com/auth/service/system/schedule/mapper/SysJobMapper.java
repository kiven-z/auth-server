package com.auth.service.system.schedule.mapper;

import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.query.SysJobQuery;
import com.auth.service.system.schedule.model.vo.SysJobPageResponse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 定时任务 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysJobMapper extends BaseMapper<SysJobEntity> {

	/**
	 * 分页查询任务
	 * @param page 分页
	 * @param query 条件
	 * @return 分页结果
	 */
	IPage<SysJobPageResponse> selectListByPage(@Param("page") Page<SysJobEntity> page,
			@Param("query") SysJobQuery query);

}
