package com.auth.service.system.file.mapper;

import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.po.FileExportTaskAdminPageRowPO;
import com.auth.service.system.file.model.po.FileExportTaskExpireRowPO;
import com.auth.service.system.file.model.po.FileExportTaskPageRowPO;
import com.auth.service.system.file.model.query.FileExportTaskAdminPageQuery;
import com.auth.service.system.file.model.query.FileExportTaskPageQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

/**
 * 文件异步导出任务 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface FileExportTaskMapper extends BaseMapper<FileExportTaskEntity> {

	/**
	 * 按创建人分页查询导出任务
	 * @param page 分页参数
	 * @param query 查询条件
	 * @param createdBy 创建人ID
	 * @return 分页结果
	 */
	IPage<FileExportTaskPageRowPO> selectPageByCreator(@Param("page") Page<FileExportTaskEntity> page,
			@Param("query") FileExportTaskPageQuery query, @Param("createdBy") Long createdBy);

	/**
	 * 管理端跨用户分页查询导出任务
	 * @param page 分页参数
	 * @param query 查询条件
	 * @return 分页结果
	 */
	IPage<FileExportTaskAdminPageRowPO> selectPageAdmin(@Param("page") Page<FileExportTaskEntity> page,
			@Param("query") FileExportTaskAdminPageQuery query);

	/**
	 * 查询待领取任务ID
	 * @param now 当前时间
	 * @param limit 数量上限
	 * @return 任务ID列表
	 */
	List<Long> selectClaimCandidateIds(@Param("now") Instant now, @Param("limit") int limit);

	/**
	 * 抢占任务租约
	 * @param id 任务ID
	 * @param workerId 实例标识
	 * @param leaseUntil 租约到期时间
	 * @param now 当前时间
	 * @return 影响行数
	 */
	int claimTask(@Param("id") Long id, @Param("workerId") String workerId, @Param("leaseUntil") Instant leaseUntil,
			@Param("now") Instant now);

	/**
	 * 续约任务租约
	 * @param id 任务ID
	 * @param workerId 实例标识
	 * @param leaseUntil 新租约到期时间
	 * @return 影响行数
	 */
	int renewLease(@Param("id") Long id, @Param("workerId") String workerId, @Param("leaseUntil") Instant leaseUntil);

	/**
	 * 将待处理任务取消
	 * @param id 任务ID
	 * @param createdBy 创建人（为空则不校验所有权）
	 * @param now 当前时间
	 * @return 影响行数
	 */
	int cancelPendingTask(@Param("id") Long id, @Param("createdBy") Long createdBy, @Param("now") Instant now);

	/**
	 * 标记运行中任务为取消中
	 * @param id 任务ID
	 * @param createdBy 创建人（为空则不校验所有权）
	 * @return 影响行数
	 */
	int requestCancelRunningTask(@Param("id") Long id, @Param("createdBy") Long createdBy);

	/**
	 * 重置失败任务为待处理（受 max_retry 约束）
	 * @param id 任务ID
	 * @param createdBy 创建人（为空则不校验所有权）
	 * @param now 当前时间
	 * @return 影响行数
	 */
	int retryTask(@Param("id") Long id, @Param("createdBy") Long createdBy, @Param("now") Instant now);

	/**
	 * 管理端强制重置失败任务为待处理（不受 max_retry 约束）
	 * @param id 任务ID
	 * @param createdBy 创建人（为空则不校验所有权）
	 * @param now 当前时间
	 * @return 影响行数
	 */
	int forceRetryTask(@Param("id") Long id, @Param("createdBy") Long createdBy, @Param("now") Instant now);

	/**
	 * 更新导出进度
	 * @param id 任务ID
	 * @param workerId Worker标识
	 * @param progressPercent 进度百分比
	 * @param processedRows 已处理行数
	 * @param totalRows 总行数
	 * @return 影响行数
	 */
	int updateProgress(@Param("id") Long id, @Param("workerId") String workerId,
			@Param("progressPercent") int progressPercent, @Param("processedRows") long processedRows,
			@Param("totalRows") Long totalRows);

	/**
	 * 标记任务成功
	 * @param id 任务ID
	 * @param workerId Worker标识
	 * @param fileRecordId 文件记录ID
	 * @param expireAt 过期时间
	 * @param now 当前时间
	 * @return 影响行数
	 */
	int markSuccess(@Param("id") Long id, @Param("workerId") String workerId, @Param("fileRecordId") Long fileRecordId,
			@Param("expireAt") Instant expireAt, @Param("now") Instant now);

	/**
	 * 标记任务失败
	 * @param id 任务ID
	 * @param workerId Worker标识
	 * @param errorCode 错误码
	 * @param errorMessage 错误信息
	 * @param now 当前时间
	 * @return 影响行数
	 */
	int markFailed(@Param("id") Long id, @Param("workerId") String workerId, @Param("errorCode") String errorCode,
			@Param("errorMessage") String errorMessage, @Param("now") Instant now);

	/**
	 * 标记任务失败并按退避策略回到待处理
	 * @param id 任务ID
	 * @param workerId Worker标识
	 * @param errorCode 错误码
	 * @param errorMessage 错误信息
	 * @param nextRetryAt 下次重试时间
	 * @param now 当前时间
	 * @return 影响行数
	 */
	int markRetryPending(@Param("id") Long id, @Param("workerId") String workerId, @Param("errorCode") String errorCode,
			@Param("errorMessage") String errorMessage, @Param("nextRetryAt") Instant nextRetryAt,
			@Param("now") Instant now);

	/**
	 * 标记任务取消
	 * @param id 任务ID
	 * @param workerId Worker标识
	 * @param now 当前时间
	 * @return 影响行数
	 */
	int markCancelled(@Param("id") Long id, @Param("workerId") String workerId, @Param("now") Instant now);

	/**
	 * 查询已过期且成功的任务
	 * @param now 当前时间
	 * @param limit 数量上限
	 * @return 过期任务
	 */
	List<FileExportTaskExpireRowPO> selectExpiredSuccessTasks(@Param("now") Instant now, @Param("limit") int limit);

	/**
	 * 批量标记任务为已过期
	 * @param tasks 待过期任务（含 id 与 fileRecordId）
	 * @param now 当前时间
	 * @return 影响行数
	 */
	int markExpiredTasks(@Param("tasks") List<FileExportTaskExpireRowPO> tasks, @Param("now") Instant now);

}
