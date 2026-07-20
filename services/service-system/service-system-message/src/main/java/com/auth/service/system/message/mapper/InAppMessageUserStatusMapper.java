package com.auth.service.system.message.mapper;

import com.auth.service.system.message.model.entity.InAppMessageUserStatusEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 站内信公开消息用户状态 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface InAppMessageUserStatusMapper extends BaseMapper<InAppMessageUserStatusEntity> {

	/**
	 * 批量 upsert 已读状态（撞 uk 时仅补已读字段）
	 * @param rows 待写入状态行
	 * @return 影响行数
	 */
	int upsertReadBatch(@Param("rows") List<InAppMessageUserStatusEntity> rows);

}
