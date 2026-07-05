package com.auth.service.system.file.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.auth.service.system.file.model.entity.SysFileRecordEntity;
import com.auth.service.system.file.model.po.SysFileRecordPageRowPO;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

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

	/**
	 * 按主键批量查询已逻辑删除的文件记录
	 * @param ids 文件记录主键集合
	 * @param expectedDeleted 期望的逻辑删除状态
	 * @return 已删除文件记录
	 */
	List<SysFileRecordEntity> selectIsDeletedByIds(@Param("ids") List<Long> ids,
			@Param("expectedDeleted") Boolean expectedDeleted);

	/**
	 * 恢复已逻辑删除的文件记录
	 * @param ids 文件记录主键集合
	 * @param updateUser 更新人，为 {@code null} 时保持原值
	 * @return 影响行数
	 */
	int restoreByIds(@Param("ids") List<Long> ids, @Param("updateUser") Long updateUser);

	/**
	 * 物理删除已逻辑删除的文件记录
	 * @param ids 文件记录主键集合
	 * @return 影响行数
	 */
	int deletePhysicallyByIds(@Param("ids") List<Long> ids);

}
