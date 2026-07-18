package com.auth.service.system.message.service;

import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.form.inapp.InAppComposeForm;
import com.auth.service.system.message.model.vo.InAppComposeResultVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 管理端站内信发送任务
 *
 * @author Bunny
 */
public interface InAppSendTaskService extends IService<InAppMessageEntity> {

	/**
	 * 按接收范围提交站内信：ALL 读扩散直接 SUCCESS；定向写扩散创建 PENDING 并异步派发
	 * @param form 发送表单（title/body/contentType 为定稿，不二次渲染模板）
	 * @return 任务受理结果
	 */
	InAppComposeResultVO send(InAppComposeForm form);

	/**
	 * 按任务 ID 补发：仅 PUSH 定向任务可重置为 PENDING 后重新派发
	 * @param taskId 任务 ID
	 * @return 受理结果
	 */
	InAppComposeResultVO retry(Long taskId);

}
