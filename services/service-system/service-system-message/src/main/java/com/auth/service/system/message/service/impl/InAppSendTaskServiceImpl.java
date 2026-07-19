package com.auth.service.system.message.service.impl;

import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.message.convert.InAppEntityAssembler;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.InAppMessageMapper;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.enums.*;
import com.auth.service.system.message.model.form.inapp.InAppComposeForm;
import com.auth.service.system.message.model.value.recipient.RecipientScope;
import com.auth.service.system.message.model.vo.inapp.InAppComposeResultVO;
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
	 * 校验任务可补发：存在、管理端编排、写扩散、状态可恢复
	 * @param taskId 任务 ID（任务不存在时用于异常参数）
	 */
	private void requireRetryable(Long taskId) {
		InAppMessageEntity task = super.getById(taskId);
		if (task == null) {
			throw new MessageException(IN_APP_SEND_TASK_NOT_FOUND, taskId);
		}

		InAppMessageStatus status = InAppMessageStatus.from(task.getStatus());
		boolean retryAllowed = MessageSendSourceType.ADMIN_COMPOSE.name().equals(task.getSourceType())
				&& !isPullDelivery(task) && status != null && status.isRetryable();
		if (!retryAllowed) {
			throw new MessageException(IN_APP_SEND_TASK_RETRY_NOT_ALLOWED, task.getId(), task.getStatus());
		}
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
			return InAppComposeResultVO.builder()
				.taskId(task.getId())
				.totalCount(0)
				.successCount(0)
				.status(InAppMessageStatus.SUCCESS.name())
				.build();
		}

		// 非公开写入用户收件表
		dispatchTrigger.dispatchAfterCommit(task.getId());
		return InAppComposeResultVO.builder()
			.taskId(task.getId())
			.totalCount(0)
			.successCount(0)
			.status(InAppMessageStatus.PENDING.name())
			.build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public InAppComposeResultVO retry(Long taskId) {
		requireRetryable(taskId);

		if (baseMapper.resetForRetry(taskId) <= 0) {
			InAppMessageEntity latest = super.getById(taskId);
			if (latest == null) {
				throw new MessageException(IN_APP_SEND_TASK_NOT_FOUND, taskId);
			}
			// CAS 失败：即使状态仍“看起来可重试”，也按最新快照拒绝，避免并发双发
			throw new MessageException(IN_APP_SEND_TASK_RETRY_NOT_ALLOWED, taskId, latest.getStatus());
		}

		dispatchTrigger.dispatchAfterCommit(taskId);
		return InAppComposeResultVO.builder()
			.taskId(taskId)
			.totalCount(0)
			.successCount(0)
			.status(InAppMessageStatus.PENDING.name())
			.build();
	}

}
