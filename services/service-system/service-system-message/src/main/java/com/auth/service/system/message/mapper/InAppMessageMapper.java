package com.auth.service.system.message.mapper;

import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 消息发送任务 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface InAppMessageMapper extends BaseMapper<InAppMessageEntity> {

	/**
	 * 状态 CAS 更新（仅当当前状态等于 expected 时切换）
	 * @param id 任务 ID
	 * @param expectedStatus 期望当前状态
	 * @param newStatus 目标状态
	 * @return 影响行数
	 */
	@Update("UPDATE in_app_message SET status = #{newStatus} WHERE id = #{id} AND status = #{expectedStatus}")
	int updateStatusCas(@Param("id") Long id, @Param("expectedStatus") String expectedStatus,
			@Param("newStatus") String newStatus);

	/**
	 * 写入终态并同步成功数/总数（以收件箱实投为准）
	 * @param id 任务 ID
	 * @param status 终态
	 * @param deliveredCount 已投递数
	 * @return 影响行数
	 */
	int finishTask(@Param("id") Long id, @Param("status") String status, @Param("deliveredCount") int deliveredCount);

	/**
	 * 将可恢复任务重置为 PENDING，供补发重新抢占
	 * @param id 任务 ID
	 * @return 影响行数
	 */
	int resetForRetry(@Param("id") Long id);

}
