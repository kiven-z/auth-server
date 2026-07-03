package com.auth.service.system.file.mapper;

import com.auth.service.system.file.model.entity.SysFileRecordEntity;
import com.auth.service.system.file.model.po.SysFileRecordPageRowPO;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 文件元数据 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysFileRecordMapper extends BaseMapper<SysFileRecordEntity> {

	/**
	 * 分页查询文件记录
	 * @param page 分页参数
	 * @param query 筛选条件
	 * @return 分页结果
	 */
	IPage<SysFileRecordPageRowPO> selectListByPage(@Param("page") Page<SysFileRecordEntity> page,
			@Param("query") FileRecordPageQuery query);

	/**
	 * 分页查询回收站文件记录
	 * @param page 分页参数
	 * @param query 筛选条件
	 * @return 分页结果
	 */
	IPage<SysFileRecordPageRowPO> selectRecycleListByPage(@Param("page") Page<SysFileRecordEntity> page,
			@Param("query") FileRecordPageQuery query);

}
