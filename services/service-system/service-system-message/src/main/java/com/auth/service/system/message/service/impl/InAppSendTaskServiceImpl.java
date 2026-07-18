package com.auth.service.system.message.service.impl;

import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.message.convert.InAppEntityAssembler;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.InAppMessageMapper;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.enums.*;
import com.auth.service.system.message.model.form.inapp.InAppComposeForm;
import com.auth.service.system.message.model.value.recipient.RecipientScope;
import com.auth.service.system.message.model.vo.InAppComposeResultVO;
import com.auth.service.system.message.service.InAppSendTaskService;
import com.auth.service.system.message.support.inapp.InAppComposeDispatchTrigger;
import com.auth.service.system.message.support.inapp.InAppComposeFormValidation;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_SEND_TASK_NOT_FOUND;
import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_SEND_TASK_RETRY_NOT_ALLOWED;

/**
 * 管理端站内信按范围发送
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class InAppSendTaskServiceImpl extends ServiceImpl<InAppMessageMapper, InAppMessageEntity>
		implements InAppSendTaskService {

	private final InAppComposeDispatchTrigger dispatchTrigger;

	/**
	 * 是否读扩散任务
	 * @param task 任务实体
	 * @return 是否读扩散任务
	 */
	private static boolean isPullDelivery(InAppMessageEntity task) {
		DeliveryMode mode = DeliveryMode.from(task.getDeliveryMode());
		if (mode != null) {
			return mode.isPull();
		}
		RecipientScopeType scopeType = RecipientScopeType.from(task.getRecipientScopeType());
		return scopeType != null && scopeType.deliveryMode().isPull();
	}

	/**
	 * 统一受理结果
	 * @param taskId 任务 ID
	 * @param status 任务状态
	 * @return 受理结果
	 */
	private static InAppComposeResultVO accepted(Long taskId, InAppMessageStatus status) {
		return InAppComposeResultVO.builder()
			.taskId(taskId)
			.totalCount(0)
			.successCount(0)
			.status(status.name())
			.build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public InAppComposeResultVO send(InAppComposeForm form) {
		RecipientScopeType type = InAppComposeFormValidation.validate(form);
		RecipientScope scope = RecipientScope.builder()
			.type(type)
			.ids(form.getRecipientScopeIds())
			.includeChildren(form.getIncludeChildren())
			.build();
		InAppContentType contentType = InAppContentType.from(form.getContentType());

		Long senderUserId = SecurityUserUtils.getUserId();
		InAppMessageEntity task = InAppEntityAssembler.toAdminComposeTask(form, scope, contentType, senderUserId);
		super.save(task);

		// 公开直接做读扩散，不写用户收件表
		if (type.deliveryMode().isPull()) {
			return accepted(task.getId(), InAppMessageStatus.SUCCESS);
		}

		// 非公开写入用户收件表
		dispatchTrigger.dispatchAfterCommit(task.getId());
		return accepted(task.getId(), InAppMessageStatus.PENDING);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public InAppComposeResultVO retry(Long taskId) {
		InAppMessageEntity task = super.getById(taskId);
		if (task == null) {
			throw new MessageException(IN_APP_SEND_TASK_NOT_FOUND, taskId);
		}
		if (!MessageSendSourceType.ADMIN_COMPOSE.name().equals(task.getSourceType())) {
			throw new MessageException(IN_APP_SEND_TASK_RETRY_NOT_ALLOWED, task.getId(), task.getStatus());
		}
		if (isPullDelivery(task)) {
			throw new MessageException(IN_APP_SEND_TASK_RETRY_NOT_ALLOWED, task.getId(), task.getStatus());
		}
		String status = task.getStatus();
		if (InAppMessageStatus.SUCCESS.name().equals(status) || InAppMessageStatus.RECALLED.name().equals(status)) {
			throw new MessageException(IN_APP_SEND_TASK_RETRY_NOT_ALLOWED, task.getId(), status);
		}

		boolean reset = baseMapper.resetForRetry(taskId) > 0;
		if (!reset) {
			InAppMessageEntity latest = super.getById(taskId);
			if (latest == null) {
				throw new MessageException(IN_APP_SEND_TASK_NOT_FOUND, taskId);
			}
			throw new MessageException(IN_APP_SEND_TASK_RETRY_NOT_ALLOWED, taskId, latest.getStatus());
		}

		dispatchTrigger.dispatchAfterCommit(taskId);
		return accepted(taskId, InAppMessageStatus.PENDING);
	}

}
