package com.auth.service.system.log.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.auth.service.system.log.model.entity.LogLoginLogEntity;
import com.auth.service.system.log.model.query.LogLoginLogQuery;
import com.auth.service.system.log.model.vo.LogLoginLogDetailVO;
import com.auth.service.system.log.model.vo.LogLoginLogPageVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 登录日志 Mapper（system-service 查询与删除）
 *
 * @author Bunny
 */
@Mapper
public interface LogLoginLogMapper extends BaseMapper<LogLoginLogEntity> {

	/**
	 * 分页查询登录日志
	 * @param page 分页参数
	 * @param query 查询条件
	 * @return 分页结果
	 */
	IPage<LogLoginLogPageVO> selectListByPage(@Param("page") Page<LogLoginLogEntity> page,
			@Param("query") LogLoginLogQuery query);

	/**
	 * 根据 ID 查询登录日志详情
	 * @param id 主键
	 * @return 登录日志详情
	 */
	LogLoginLogDetailVO selectDetailById(@Param("id") Long id);

}
