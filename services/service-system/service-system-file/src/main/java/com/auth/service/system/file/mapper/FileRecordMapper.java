package com.auth.service.system.file.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.model.po.FileRecordPageRowPO;
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
	 * @param deleteSource 删除来源
	 * @return 文件记录详情
	 */
	FileRecordEntity selectDetailById(@Param("id") Long id, @Param("ownerUserId") Long ownerUserId,
			@Param("isPrivate") Boolean isPrivate, @Param("expectedDeleted") Boolean expectedDeleted,
			@Param("deleteSource") String deleteSource);

	/**
	 * 按主键批量查询已逻辑删除的文件记录
	 * @param ids 文件记录主键集合
	 * @param expectedDeleted 期望的逻辑删除状态
	 * @return 已删除文件记录
	 */
	List<FileRecordEntity> selectIsDeletedByIds(@Param("ids") List<Long> ids,
			@Param("expectedDeleted") Boolean expectedDeleted);

	/**
	 * 恢复已逻辑删除的文件记录
	 * @param ids 文件记录主键集合
	 * @param updateUser 更新人，为 {@code null} 时保持原值
	 * @return 影响行数
	 */
	int restoreByIds(@Param("ids") List<Long> ids, @Param("updateUser") Long updateUser);

	/**
	 * 逻辑删除文件记录并写入删除审计字段
	 * @param ids 文件记录主键集合
	 * @param updateUser 更新人，为 {@code null} 时保持原值
	 * @param deletedBy 删除人，为 {@code null} 时保持原值
	 * @param deleteSource 删除来源
	 * @return 影响行数
	 */
	int markDeletedByIds(@Param("ids") List<Long> ids, @Param("updateUser") Long updateUser,
			@Param("deletedBy") Long deletedBy, @Param("deleteSource") String deleteSource);

	/**
	 * 物理删除已逻辑删除的文件记录
	 * @param ids 文件记录主键集合
	 * @return 影响行数
	 */
	int deletePhysicallyByIds(@Param("ids") List<Long> ids);

	/**
	 * 物理删除未逻辑删除的文件记录
	 * @param ids 文件记录主键集合
	 * @return 影响行数
	 */
	int deletePhysicallyActiveByIds(@Param("ids") List<Long> ids);

	/**
	 * 按主键更新文件隐私属性
	 * @param id 文件记录主键
	 * @param objectKey 新对象键
	 * @param isPrivate 目标私有性
	 * @param url 新访问地址
	 * @param updateUser 更新人
	 * @return 影响行数
	 */
	int updatePrivacyById(@Param("id") Long id, @Param("objectKey") String objectKey,
			@Param("isPrivate") boolean isPrivate, @Param("url") String url, @Param("updateUser") Long updateUser);

}
