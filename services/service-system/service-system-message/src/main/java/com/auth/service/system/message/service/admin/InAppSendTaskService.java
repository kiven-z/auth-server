package com.auth.service.system.message.service.admin;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.form.inapp.InAppComposeForm;
import com.auth.service.system.message.model.query.InAppSendTaskQuery;
import com.auth.service.system.message.model.vo.inapp.InAppComposeResultVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskPageVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 管理端站内信发送任务
 *
 * @author Bunny
 */
public interface InAppSendTaskService extends IService<InAppMessageEntity> {

	/**
	 * 分页查询站内信发送任务
	 * @param query 查询条件
	 * @return 发送任务分页数据
	 */
	PageResponse<InAppSendTaskPageVO> getSendTaskPage(InAppSendTaskQuery query);

	/**
	 * 查询站内信发送任务详情
	 * @param taskId 任务 ID
	 * @return 详情
	 */
	InAppSendTaskDetailVO getSendTaskById(Long taskId);

	/**
	 * 提交站内信发送任务
	 * @param form 发送表单
	 * @return 任务受理结果
	 */
	InAppComposeResultVO send(InAppComposeForm form);

	/**
	 * 补发站内信发送任务
	 * @param taskId 任务 ID
	 */
	void retry(Long taskId);

	/**
	 * 撤回站内信发送任务
	 * @param taskId 任务 ID
	 */
	void recall(Long taskId);

	/**
	 * 批量删除站内信发送任务
	 * @param ids 任务 ID 列表
	 */
	void batchDelete(List<Long> ids);

}
