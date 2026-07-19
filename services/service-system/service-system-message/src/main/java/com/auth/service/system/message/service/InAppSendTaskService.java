package com.auth.service.system.message.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.form.inapp.InAppComposeForm;
import com.auth.service.system.message.model.query.InAppSendTaskQuery;
import com.auth.service.system.message.model.vo.inapp.InAppComposeResultVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskPageResponse;
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
	PageResponse<InAppSendTaskPageResponse> getSendTaskPage(InAppSendTaskQuery query);

	/**
	 * 按任务 ID 查询站内信发送任务详情（含正文与范围快照解析）
	 * @param taskId 任务 ID
	 * @return 详情
	 */
	InAppSendTaskDetailVO getSendTaskById(Long taskId);

	/**
	 * 按接收范围提交站内信：ALL 读扩散直接 SUCCESS；定向写扩散创建 PENDING 并异步派发
	 * @param form 发送表单（title/body/contentType 为定稿，不二次渲染模板）
	 * @return 任务受理结果
	 */
	InAppComposeResultVO send(InAppComposeForm form);

	/**
	 * 按任务 ID 补发：仅 PUSH 定向任务可重置为 PENDING 后重新派发
	 * @param taskId 任务 ID
	 */
	void retry(Long taskId);

	/**
	 * 按任务 ID 撤回
	 * @param taskId 任务 ID
	 */
	void recall(Long taskId);

	/**
	 * 批量物理删除发送任务：仅终态可删；收件箱随外键 CASCADE 清理
	 * @param ids 任务 ID 列表
	 */
	void batchDelete(List<Long> ids);

}
