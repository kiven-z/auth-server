package com.auth.service.system.admin.mapper;

import com.auth.service.system.admin.model.entity.MessageTemplateEntity;
import com.auth.service.system.admin.model.po.emailtemplate.EmailTemplatePageRowPO;
import com.auth.service.system.admin.model.query.EmailTemplateQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 统一消息模板表 Mapper（sys_message_template）。
 *
 * @author Bunny
 */
@Mapper
public interface MessageTemplateMapper extends BaseMapper<MessageTemplateEntity> {

	/**
	 * 按场景编码与渠道查询当前启用的最高优先级模板行。
	 * @param sceneCode 场景编码（发送命令 templateCode）
	 * @param channel 渠道名，如 EMAIL、SMS
	 * @return 模板行，未找到时为 null
	 */
	MessageTemplateEntity selectEnabledBySceneCodeAndChannel(@Param("sceneCode") String sceneCode,
			@Param("channel") String channel);

	/**
	 * 邮件模板分页列表（channel 固定为 EMAIL）
	 * @param pageParams 分页参数
	 * @param query 查询条件
	 * @return 分页结果
	 */
	IPage<EmailTemplatePageRowPO> selectEmailTemplatePage(@Param("page") Page<MessageTemplateEntity> pageParams,
			@Param("query") EmailTemplateQuery query);

}
