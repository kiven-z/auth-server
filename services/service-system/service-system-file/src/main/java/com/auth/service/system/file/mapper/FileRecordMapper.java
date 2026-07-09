package com.auth.service.system.file.mapper;

import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.model.po.FileRecordPageRowPO;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件元数据 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface FileRecordMapper extends BaseMapper<FileRecordEntity> {

	/**
	 * 分页查询文件记录
	 * @param page 分页参数
	 * @param query 筛选条件
	 * @param isDeleted 逻辑删除状态
	 * @return 分页结果
	 */
	IPage<FileRecordPageRowPO> selectListByPage(@Param("page") Page<FileRecordEntity> page,
			@Param("query") FileRecordPageQuery query, @Param("isDeleted") Boolean isDeleted);

	/**
	 * 按主键查询文件记录详情
	 * @param id 文件记录主键
	 * @param ownerUserId 归属用户ID
	 * @param isPrivate 是否私有文件
	 * @param expectedDeleted 是否预期删除
	 * @param deleteSources 删除来源集合，非空时按 IN 过滤
	 * @return 文件记录详情
	 */
	FileRecordEntity selectDetailById(@Param("id") Long id, @Param("ownerUserId") Long ownerUserId,
			@Param("isPrivate") Boolean isPrivate, @Param("expectedDeleted") Boolean expectedDeleted,
			@Param("deleteSources") List<String> deleteSources);

	/**
	 * 按主键批量查询已逻辑删除的文件记录
	 * @param ids 文件记录主键集合
	 * @param ownerUserId 归属用户ID
	 * @param isPrivate 是否私有文件
	 * @param expectedDeleted 期望的逻辑删除状态
	 * @param deleteSources 删除来源集合，非空时按 IN 过滤
	 * @return 已删除文件记录
	 */
	List<FileRecordEntity> selectIsDeletedByIds(@Param("ids") List<Long> ids, @Param("ownerUserId") Long ownerUserId,
			@Param("isPrivate") Boolean isPrivate, @Param("expectedDeleted") Boolean expectedDeleted,
			@Param("deleteSources") List<String> deleteSources);

	/**
	 * 恢复已逻辑删除的文件记录
	 * @param ids 文件记录主键集合
	 * @param updateUser 更新人
	 * @return 影响行数
	 */
	int restoreByIds(@Param("ids") List<Long> ids, @Param("updateUser") Long updateUser);

	/**
	 * 按逻辑删除状态物理删除文件记录
	 * @param ids 文件记录主键集合
	 * @param expectedDeleted 期望的逻辑删除状态
	 * @return 影响行数
	 */
	int deletePhysicallyByIds(@Param("ids") List<Long> ids, @Param("expectedDeleted") Boolean expectedDeleted);

	/**
	 * 按 URL 查询活跃文件记录
	 * @param url 访问 URL
	 * @param objectKey 对象键
	 * @param ownerUserId 归属用户 ID
	 * @param bizType 业务类型（可选）
	 * @return 文件记录
	 */
	FileRecordEntity selectActiveByUrlAndOwner(@Param("url") String url, @Param("objectKey") String objectKey,
			@Param("ownerUserId") Long ownerUserId, @Param("bizType") String bizType);

}
