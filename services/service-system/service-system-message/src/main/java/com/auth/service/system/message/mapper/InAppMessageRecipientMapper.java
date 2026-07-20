package com.auth.service.system.message.mapper;

import com.auth.service.system.message.model.entity.InAppMessageRecipientEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 站内信收件箱 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface InAppMessageRecipientMapper extends BaseMapper<InAppMessageRecipientEntity> {

	/**
	 * 批量 INSERT IGNORE，撞唯一键时跳过
	 * @param rows 待插入行
	 * @return 实际插入行数
	 */
	int insertIgnoreBatch(@Param("rows") List<InAppMessageRecipientEntity> rows);

	/**
	 * 将当前用户可见且未读的收件行标为已读
	 * @param userId 当前用户 ID
	 * @param messageIds 站内信 ID 列表
	 * @param readTime 已读时间
	 * @return 影响行数
	 */
	int markRead(@Param("userId") Long userId, @Param("messageIds") List<Long> messageIds,
			@Param("readTime") LocalDateTime readTime);

}
