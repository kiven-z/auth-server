package com.auth.service.system.log.mapper;

import com.auth.service.system.log.model.entity.LogUserPasswordHistoryEntity;
import com.auth.service.system.log.model.query.LogUserPasswordHistoryQuery;
import com.auth.service.system.log.model.vo.LogUserPasswordHistoryDetailVO;
import com.auth.service.system.log.model.vo.LogUserPasswordHistoryPageVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 密码历史日志 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface LogUserPasswordHistoryMapper extends BaseMapper<LogUserPasswordHistoryEntity> {

	/**
	 * 分页查询密码历史日志
	 * @param page 分页参数
	 * @param query 查询条件
	 * @return 分页结果
	 */
	IPage<LogUserPasswordHistoryPageVO> selectListByPage(@Param("page") Page<LogUserPasswordHistoryEntity> page,
			@Param("query") LogUserPasswordHistoryQuery query);

	/**
	 * 根据ID查询密码历史日志详情
	 * @param id ID
	 * @return 密码历史日志详情
	 */
	LogUserPasswordHistoryDetailVO selectDetailById(@Param("id") Long id);

}
