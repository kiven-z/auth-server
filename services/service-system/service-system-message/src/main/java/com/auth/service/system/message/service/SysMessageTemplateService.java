package com.auth.service.system.message.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.query.MessageTemplateQuery;
import com.auth.service.system.message.model.vo.MessageTemplateDetailVO;
import com.auth.service.system.message.model.vo.MessageTemplatePageResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 消息模板读服务（分页、详情等跨渠道公共查询）
 *
 * @author Bunny
 */
public interface SysMessageTemplateService {

	/**
	 * 按渠道分页查询消息模板
	 * @param pageParams 分页参数
	 * @param query 查询条件（channel 必填）
	 * @return 消息模板分页数据
	 */
	PageResponse<MessageTemplatePageResponse> getMessageTemplatePage(Page<MessageTemplateEntity> pageParams,
			MessageTemplateQuery query);

	/**
	 * 按主键与渠道查询消息模板详情
	 * @param id 模板主键
	 * @param channel 消息渠道（须与库中一致）
	 * @return 模板详情
	 */
	MessageTemplateDetailVO getMessageTemplateById(Long id, String channel);

}
