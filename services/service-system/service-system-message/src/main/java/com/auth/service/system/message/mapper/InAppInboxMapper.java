package com.auth.service.system.message.mapper;

import com.auth.service.system.message.model.po.InAppInboxPageRowPO;
import com.auth.service.system.message.model.query.InAppInboxQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户侧站内信收件箱 Mapper（跨消息本体与收件关系的读模型）
 *
 * @author Bunny
 */
@Mapper
public interface InAppInboxMapper {

	/**
	 * 分页查询当前用户 PUSH 收件箱（按未读优先）
	 * @param page 分页参数
	 * @param userId 当前用户 ID
	 * @param query 查询条件
	 * @return 分页行
	 */
	IPage<InAppInboxPageRowPO> selectPushInboxPage(Page<InAppInboxPageRowPO> page, @Param("userId") Long userId,
			@Param("query") InAppInboxQuery query);

	/**
	 * 分页查询当前用户 PULL 收件箱（按未读优先）
	 * @param page 分页参数
	 * @param userId 当前用户 ID
	 * @param query 查询条件
	 * @return 分页行
	 */
	IPage<InAppInboxPageRowPO> selectPullInboxPage(Page<InAppInboxPageRowPO> page, @Param("userId") Long userId,
			@Param("query") InAppInboxQuery query);

	/**
	 * 统计当前用户 PUSH 未读数（可见且未软删）
	 * @param userId 当前用户 ID
	 * @return 未读条数；COUNT 无匹配时为 0
	 */
	long countPushUnread(@Param("userId") Long userId);

	/**
	 * 统计当前用户 PULL 未读数（无状态行或未读，且未软删）
	 * @param userId 当前用户 ID
	 * @return 未读条数；COUNT 无匹配时为 0
	 */
	long countPullUnread(@Param("userId") Long userId);

}
