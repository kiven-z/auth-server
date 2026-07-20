package com.auth.service.system.message.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.message.model.query.InAppInboxQuery;
import com.auth.service.system.message.model.vo.inapp.InAppInboxPageResponse;
import com.auth.service.system.message.model.vo.inapp.InAppInboxUnreadCountVO;

import java.util.List;

/**
 * 用户侧站内信收件箱
 *
 * @author Bunny
 */
public interface InAppInboxService {

	/**
	 * 分页查询当前用户收件箱
	 * @param query 查询条件
	 * @return 收件箱分页数据
	 */
	PageResponse<InAppInboxPageResponse> getInboxPage(InAppInboxQuery query);

	/**
	 * 查询当前用户未读角标（公开 + 定向）
	 * @return 未读统计
	 */
	InAppInboxUnreadCountVO getUnreadCount();

	/**
	 * 将指定站内信标为已读
	 * @param messageIds 站内信 ID 列表
	 */
	void markRead(List<Long> messageIds);

}
