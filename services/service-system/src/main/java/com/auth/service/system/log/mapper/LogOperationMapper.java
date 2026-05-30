package com.auth.service.system.log.mapper;

import com.auth.service.system.log.model.entity.LogOperationEntity;
import com.auth.service.system.log.model.po.LogOperationPageRowPO;
import com.auth.service.system.log.model.query.LogOperationQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 操作日志 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface LogOperationMapper extends BaseMapper<LogOperationEntity> {

	/**
	 * 分页查询操作日志
	 * @param page 分页参数
	 * @param query 筛选条件
	 * @return 分页结果
	 */
	IPage<LogOperationPageRowPO> selectListByPage(@Param("page") Page<LogOperationEntity> page,
			@Param("query") LogOperationQuery query);

}
